package com.example.firebaseauth.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.firebaseauth.model.User
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun UserSelectionPage(navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    var users by remember { mutableStateOf<List<User>>(emptyList()) }

    LaunchedEffect(Unit) {
        firestore.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                users = snapshot.map { document ->
                    User(
                        userId = document.id,
                        name = document.getString("name"),
                        email = document.getString("email"),
                        address = document.getString("address"),
                        imageUrl = document.getString("imageUrl")
                    )
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Select a user to chat with", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(users) { user ->
                UserItem(user) {
                    val userId = user.userId
                    if (userId != null) {
                        navController.navigate("chat/$userId")
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(user: User, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = user.name ?: "Unknown",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
