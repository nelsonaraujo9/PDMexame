package com.example.firebaseauth.ui

import AuthViewModel
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.firebaseauth.viewmodel.ProfileViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.firebaseauth.navigation.AppPages

@Composable
fun EditProfilePage(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val profileViewModel: ProfileViewModel = viewModel()

    // Acessando o usuário logado através do AuthViewModel
    val currentUser = authViewModel.currentUser

    // Usando o email do usuário logado no campo
    var name = remember { mutableStateOf("") }
    var address = remember { mutableStateOf("") }
    var imageURL = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        profileViewModel.fetchUserData()
    }
    LaunchedEffect(profileViewModel.user.value) {
        profileViewModel.user.value?.let {
            name.value = it.name.toString()
            address.value = it.address.toString()
            imageURL.value = it.imageUrl ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        ImagePickerComponent(
            onUrlSaved = { uri ->
                Log.d("EditProfilePage", "Image URL saved: $uri")
                imageURL.value = uri
            },
            imageURL
        )

        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Edit Profile", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Name", modifier = Modifier.padding(top = 16.dp))
        TextField(
            value = name.value,
            onValueChange = { name.value = it },
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            singleLine = true,
            label = { Text("Enter your name") }
        )

        Text(text = "Email", modifier = Modifier.padding(top = 16.dp))
        TextField(
            value = currentUser?.email ?: "",
            onValueChange = {},
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            singleLine = true,
            label = { Text("Email (cannot be changed)") },
            enabled = false
        )

        Text(text = "Address", modifier = Modifier.padding(top = 16.dp))
        TextField(
            value = address.value,
            onValueChange = { address.value = it },
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            singleLine = true,
            label = { Text("Enter your address") }
        )

        Button(
            onClick = {
                profileViewModel.updateUserData(name.value, address.value, imageURL.value)
                navController.navigate(AppPages.HomePage.route) {
                    popUpTo(AppPages.EditProfilePage.route) { inclusive = true }
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Save")
        }
    }
}
