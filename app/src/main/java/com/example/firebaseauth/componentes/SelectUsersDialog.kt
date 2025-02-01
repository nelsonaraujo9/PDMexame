package com.example.firebaseauth.componentes

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.firebaseauth.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SelectUsersDialog(
    onDismiss: () -> Unit,
    onUserSelected: (List<User>) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var selectedUsers by remember { mutableStateOf<MutableList<User>>(mutableListOf()) }
    var isLoading by remember { mutableStateOf(false) }
    var existingChatUserIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(Unit) {
        firestore.collection("users").get()
            .addOnSuccessListener { snapshot ->
                users = snapshot.documents.mapNotNull { document ->
                    val user = document.toObject(User::class.java)?.copy(userId = document.id)
                    if (user?.userId != currentUserId) user else null
                }
            }
            .addOnFailureListener { exception ->
                Log.e("SelectUsersDialog", "Error loading users", exception)
            }

        firestore.collection("chats")
            .whereArrayContains("userIds", currentUserId)
            .get()
            .addOnSuccessListener { snapshot ->
                val userIds = snapshot.documents.flatMap { document ->
                    (document.get("userIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                }.toSet()
                existingChatUserIds = userIds
            }
            .addOnFailureListener { exception ->
                Log.e("SelectUsersDialog", "Error loading existing chats", exception)
            }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Users") },
        text = {
            Column(modifier = Modifier.padding(8.dp)) {
                if (users.isEmpty()) {
                    Text("No users found")
                } else {
                    users.forEach { user ->
                        if (user.userId !in existingChatUserIds) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedUsers.contains(user),
                                    onCheckedChange = { isChecked ->
                                        selectedUsers = if (isChecked) {
                                            (selectedUsers + user).toMutableList()
                                        } else {
                                            (selectedUsers - user).toMutableList()
                                        }
                                    }
                                )
                                Text(text = user.name ?: "Unknown")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedUsers.isEmpty()) return@TextButton

                    isLoading = true
                    val chatUsers = selectedUsers.map { it.userId!! }.toMutableList().apply {
                        add(currentUserId)
                    }

                    val chatData = hashMapOf(
                        "userIds" to chatUsers,
                        "messages" to emptyList<Map<String, Any>>(),
                        "userTyping" to emptyList<Map<String, Any>>()
                    )

                    firestore.collection("chats")
                        .add(chatData)
                        .addOnSuccessListener {
                            onUserSelected(selectedUsers)
                            onDismiss()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("SelectUsersDialog", "Error creating chat", exception)
                        }
                        .addOnCompleteListener {
                            isLoading = false
                        }
                },
                enabled = selectedUsers.isNotEmpty() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Create Chat")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}