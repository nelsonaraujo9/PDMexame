package com.example.firebaseauth.navigation

import AuthViewModel
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firebaseauth.pages.HomePage
import com.example.firebaseauth.pages.LoginPage
import com.example.firebaseauth.pages.SignupPage
import com.example.firebaseauth.ui.EditProfilePage

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = if (authViewModel.isAuthenticated) AppPages.HomePage.route else AppPages.LoginPage.route
    ) {
        composable(
            route = AppPages.LoginPage.route
        ) {
            LoginPage(navController = navController)
        }
        composable(
            route = AppPages.SignupPage.route
        ) {
            SignupPage(navController = navController)
        }
        composable(
            route = AppPages.HomePage.route
        ) {
            HomePage(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable(
            route = AppPages.EditProfilePage.route
        ) {
            EditProfilePage(
            navController = navController,
            authViewModel = authViewModel
            )

        }
    }
}
