package com.example.firebaseauth.pages

import AuthViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.firebaseauth.componentes.ChatItem
import com.example.firebaseauth.componentes.SelectUsersDialog
import com.example.firebaseauth.model.Chat
import com.example.firebaseauth.navigation.AppPages
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomePage(navController: NavController, authViewModel: AuthViewModel) {
    val currentUser = authViewModel.currentUser
    val firestore = FirebaseFirestore.getInstance()
    var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }
    var profileImageUrl by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var showUserPicker by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.navigate(AppPages.LoginPage.route) {
                popUpTo(AppPages.HomePage.route) { inclusive = true }
            }
        } else {
            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    profileImageUrl = document.getString("imageUrl") ?: ""
                    userName = document.getString("name") ?: "User"
                }
            firestore.collection("chats")
                .whereArrayContains("userIds", currentUser.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }

                    snapshot?.let {
                        val updatedChats = it.documents.map { document ->
                            val chatId = document.id
                            val chatData = document.toObject(Chat::class.java)
                            chatData?.copy(chatId = chatId)
                        }.filterNotNull()


                        chats = updatedChats
                    }
                }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().padding(15.dp),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showUserPicker = true },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Chat")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (profileImageUrl.isNotBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(profileImageUrl),
                            contentDescription = "Profile Image",
                            modifier = Modifier.size(50.dp).clickable {
                                navController.navigate(AppPages.EditProfilePage.route)
                            }
                                .clip(CircleShape),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Default Profile Icon",
                            modifier = Modifier.size(50.dp).clickable {
                                navController.navigate(AppPages.EditProfilePage.route)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Welcome, $userName")
                }
                IconButton(onClick = {
                    authViewModel.logout()
                    navController.navigate(AppPages.LoginPage.route) {
                        popUpTo(AppPages.HomePage.route) { inclusive = true }
                    }
                }) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(chats) { chat ->
                    ChatItem(chat) { chatId ->
                        navController.navigate(AppPages.ChatPage.route.replace("{chatId}", chatId))
                    }
                }
            }
        }

        if (showUserPicker) {
            SelectUsersDialog(
                onDismiss = { showUserPicker = false },
                onUserSelected = { newChatId ->
                    showUserPicker = false
                    if(newChatId.isNotEmpty()){
                        navController.navigate(AppPages.ChatPage.route.replace("{chatId}", newChatId))
                    }
                }
            )
        }
    }
}