package com.example.firebaseauth.pages

import AuthViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.firebaseauth.navigation.AppPages
import com.example.firebaseauth.model.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun HomePage(navController: NavController, authViewModel: AuthViewModel) {
    val currentUser = authViewModel.currentUser
    val firestore = FirebaseFirestore.getInstance()
    var chatMessages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var newMessageText by remember { mutableStateOf("") }
    var listenerRegistration: ListenerRegistration? by remember { mutableStateOf(null) }

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.navigate(AppPages.LoginPage.route) {
                popUpTo(AppPages.HomePage.route) { inclusive = true }
            }
        } else {
            listenerRegistration = firestore.collection("chatMessages")
                .orderBy("timestamp")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    snapshot?.let { chatMessages = it.toObjects(ChatMessage::class.java) }
                }
        }
    }

    DisposableEffect(Unit) {
        onDispose { listenerRegistration?.remove() }
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

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(chatMessages) { message ->
                    ChatMessageItem(message, currentUser.uid)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newMessageText,
                    onValueChange = { newMessageText = it },
                    label = { Text("Type a message") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Button(
                    onClick = {
                        if (newMessageText.isNotBlank()) {
                            val message = ChatMessage(
                                text = newMessageText,
                                senderId = currentUser.uid,
                                senderEmail = currentUser.email ?: "Anonymous"
                            )
                            firestore.collection("chatMessages").add(message)
                            newMessageText = ""
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Send")
                }
            }

            Button(
                onClick = {
                    navController.navigate(AppPages.EditProfilePage.route)
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Edit Profile")
            }

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

@Composable
fun ChatMessageItem(message: ChatMessage, currentUserId: String) {
    val isCurrentUser = message.senderId == currentUserId
    val alignment = if (isCurrentUser) Alignment.TopEnd else Alignment.TopStart
    val color = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = color)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = message.senderEmail,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
