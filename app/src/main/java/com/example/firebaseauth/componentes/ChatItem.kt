package com.example.firebaseauth.componentes

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.firebaseauth.model.Chat
import com.example.firebaseauth.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun ChatItem(chat: Chat, onClick: (String) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isGroup by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }
    var chatImageUrl by remember { mutableStateOf("https://example.com/group-icon.png") } // Default group icon

    val chatId = chat.chatId

    LaunchedEffect(chat) {
        val usersList = mutableListOf<User>()
        for (userId in chat.userIds) {
            val userDocument = firestore.collection("users").document(userId).get().await()
            userDocument.toObject(User::class.java)?.let { usersList.add(it) }
        }
        users = usersList
        isGroup = chat.userIds.size > 2
        groupName = if (isGroup) {
            "Grupo com ${users.joinToString(", ") { it.name ?: "Desconhecido" }}"
        } else {
            users.firstOrNull()?.name ?: "Desconhecido"
        }
        if (isGroup) {
            chatImageUrl = "https://example.com/group-icon.png"
        } else {
            chatImageUrl = users.firstOrNull()?.imageUrl ?: "https://example.com/default-avatar.png" // Imagem do primeiro usuário
        }
        Log.d("ChatItem", "Usuários carregados: ${users.size}, Grupo: $isGroup, Nome do grupo: $groupName")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onClick(chatId) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(chatImageUrl),
                contentDescription = "Chat Image",
                modifier = Modifier.size(50.dp)
                    .clip(CircleShape),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = groupName,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
