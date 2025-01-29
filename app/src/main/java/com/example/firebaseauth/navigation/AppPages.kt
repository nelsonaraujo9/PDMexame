package com.example.firebaseauth.navigation

sealed class AppPages(val route: String) {
    object LoginPage: AppPages("login_page")
    object HomePage: AppPages("home_page")
    object SignupPage: AppPages("signup_page")
    object EditProfilePage : AppPages("edit_profile")
}


