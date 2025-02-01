package com.example.firebaseauth.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.firebaseauth.navigation.AppPages
import com.example.firebaseauth.viewmodels.SignupViewModel

@Composable
fun SignupPage(navController: NavController) {
    val signupViewModel: SignupViewModel = viewModel()

    LaunchedEffect(signupViewModel.signupSuccess) {
        if (signupViewModel.signupSuccess) {
            navController.navigate(AppPages.HomePage.route) {
                popUpTo(AppPages.SignupPage.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = signupViewModel.email,
            onValueChange = { signupViewModel.email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = signupViewModel.password,
            onValueChange = { signupViewModel.password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = signupViewModel.confirmPassword,
            onValueChange = { signupViewModel.confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )
        Button(
            onClick = { signupViewModel.signup() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }
        if (signupViewModel.errorMessage != null) {
            Text(
                text = signupViewModel.errorMessage ?: "",
                color = androidx.compose.ui.graphics.Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
