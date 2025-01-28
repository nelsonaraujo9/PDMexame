package com.example.firebaseauth.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class SignupViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var errorMessage by mutableStateOf<String?>(null)
    var signupSuccess by mutableStateOf(false)

    fun signup() {
        if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
            if (password == confirmPassword) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            signupSuccess = true
                            errorMessage = null
                        } else {
                            errorMessage = task.exception?.message ?: "Signup failed"
                        }
                    }
            } else {
                errorMessage = "Passwords do not match"
            }
        } else {
            errorMessage = "Please fill all fields"
        }
    }
}
