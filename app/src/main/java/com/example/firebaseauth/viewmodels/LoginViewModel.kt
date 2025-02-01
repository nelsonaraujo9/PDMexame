package com.example.firebaseauth.viewmodels

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var errorMessage by mutableStateOf<String?>(null)
    var loginSuccess by mutableStateOf(false)
    var isLoading by mutableStateOf(false)

    fun login() {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            isLoading = true
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        loginSuccess = true
                        errorMessage = null
                    } else {
                        errorMessage = task.exception?.message ?: "Login failed"
                    }
                }
        } else {
            errorMessage = "Please fill all fields"
        }
    }
    fun loginWithGoogle(idToken: String, onComplete: (Boolean) -> Unit) {
        isLoading = true
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    loginSuccess = true
                    errorMessage = null
                    onComplete(true)
                } else {
                    errorMessage = task.exception?.message ?: "Google login failed"
                    onComplete(false)
                }
            }
    }
}
