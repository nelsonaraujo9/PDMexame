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
                            _user.value = userData ?: User(userId = userId)
                        } else {
                        }
                    }
                    .addOnFailureListener { exception ->
                    }
            } else {
            }
        }
    }


    fun updateUserData(name: String, address: String, imgURL: String) {
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid

            if (!userId.isNullOrEmpty()) {
                val userRef = firestore.collection("users").document(userId)

                val userData = hashMapOf(
                    "name" to name,
                    "address" to address,
                    "imageUrl" to imgURL
                )

                userRef.set(userData)
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener { exception ->
                    }
            } else {
            }
        }
    }
}