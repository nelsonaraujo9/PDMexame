package com.example.firebaseauth.ui

import AuthViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.firebaseauth.viewmodel.ProfileViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.firebaseauth.navigation.AppPages

@Composable
fun EditProfilePage(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val profileViewModel: ProfileViewModel = viewModel()

    val currentUser = authViewModel.currentUser

    var name = remember { mutableStateOf("") }
    var address = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        profileViewModel.fetchUserData()
    }

    LaunchedEffect(profileViewModel.user.value) {
        profileViewModel.user.value?.let {
            name.value = it.name ?: ""
            address.value = it.address ?: ""
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Edit Profile", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)

        Text(text = "Name", modifier = Modifier.padding(top = 16.dp))
        TextField(
            value = name.value,
            onValueChange = { name.value = it },
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
            singleLine = true,
            label = { Text("Enter your name") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions.Default
        )

        Text(text = "Email", modifier = Modifier.padding(top = 16.dp))
        TextField(
            value = currentUser?.email ?: "",
            onValueChange = {},
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
            singleLine = true,
            label = { Text("Email (cannot be changed)") },
            enabled = false
        )

        Text(text = "Address", modifier = Modifier.padding(top = 16.dp))
        TextField(
            value = address.value,
            onValueChange = { address.value = it },
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
            singleLine = true,
            label = { Text("Enter your address") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions.Default
        )

        Button(
            onClick = {
                profileViewModel.updateUserData(name.value, address.value)
                navController.navigate(AppPages.HomePage.route) // Redireciona para a HomePage
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Save")
        }
    }
}

@Preview
@Composable
fun PreviewEditProfile() {
    EditProfilePage(navController = rememberNavController(), authViewModel = AuthViewModel())
}
