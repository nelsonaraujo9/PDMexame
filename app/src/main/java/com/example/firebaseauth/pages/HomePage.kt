package com.example.firebaseauth.pages

import AuthViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.firebaseauth.navigation.AppPages

@Composable
fun HomePage(navController: NavController, authViewModel: AuthViewModel) {

    val currentUser = authViewModel.currentUser
    //Só executa quando muda o currentUser
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.navigate(AppPages.LoginPage.route) {
                popUpTo(AppPages.HomePage.route) { inclusive = true }
            }
        }
    }

    if (currentUser != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome, ${currentUser.email ?: "User"}")

            Button(
                onClick = {
                    authViewModel.logout()
                    navController.navigate(AppPages.LoginPage.route) {
                        popUpTo(AppPages.HomePage.route) { inclusive = true }
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Logout")
            }
        }
    }
}

