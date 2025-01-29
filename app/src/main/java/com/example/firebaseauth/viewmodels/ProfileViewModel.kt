package com.example.firebaseauth.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseauth.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user

    // Método para buscar os dados do usuário no Firestore
    fun fetchUserData() {
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid

            if (!userId.isNullOrEmpty()) {
                firestore.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val userData = document.toObject(User::class.java)
                            _user.value = userData ?: User(userId = userId) // Garante um valor padrão
                        } else {
                            Log.e("ProfileViewModel", "User document not found")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("ProfileViewModel", "Error fetching user data: $exception")
                    }
            } else {
                Log.e("ProfileViewModel", "User ID is empty")
            }
        }
    }


    // Método para atualizar os dados do usuário no Firestore
    fun updateUserData(name: String, address: String) {
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid

            if (!userId.isNullOrEmpty()) {
                val userRef = firestore.collection("users").document(userId)

                val userData = hashMapOf(
                    "name" to name,
                    "address" to address
                )

                userRef.set(userData)
                    .addOnSuccessListener {
                        Log.d("ProfileViewModel", "User data saved successfully")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("ProfileViewModel", "Error saving user data: $exception")
                    }
            } else {
                Log.e("ProfileViewModel", "User ID is empty")
            }
        }
    }
}