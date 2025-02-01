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

data class ChatActionParams(
    val create: Boolean = true, // Define se vai criar ou adicionar
    val existingChat: String? = null, // ID do chat existente, se não for criar um novo
    val currentUsersInChat: List<String>? = null // Usuários já no chat, se existir
)

@Composable
fun SelectUsersDialog(
    onDismiss: () -> Unit,
    onUserSelected: (String) -> Unit,
    userNotAllowed: List<String>? = null,
    isMultipleSelection: Boolean = false,
    chatActionParams: ChatActionParams = ChatActionParams() // Novo parâmetro que agrupa a lógica de criação ou adição
) {
    val firestore = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var selectedUsers by remember { mutableStateOf<MutableList<User>>(mutableListOf()) }
    var isLoading by remember { mutableStateOf(false) }
    var existingChatUserIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(Unit) {
        firestore.collection("chats")
            .whereArrayContains("userIds", currentUserId)
            .get()
            .addOnSuccessListener { snapshot ->
                val userIds = snapshot.documents
                    .filter { document ->
                        val userIdsList = document.get("userIds") as? List<*>
                        (userIdsList?.size ?: 0) <= 2
                    }
                    .flatMap { document ->
                        (document.get("userIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    }
                    .toSet()
                existingChatUserIds = userIds
            }
            .addOnFailureListener { exception ->
                Log.e("SelectUsersDialog", "Error loading existing chats", exception)
            }

        firestore.collection("users").get()
            .addOnSuccessListener { snapshot ->
                users = snapshot.documents.mapNotNull { document ->
                    val user = document.toObject(User::class.java)?.copy(userId = document.id)
                    if (user?.userId != currentUserId) user else null
                }
                // Filtra os usuários removendo aqueles presentes em userNotAllowed, se fornecido
                users = if (userNotAllowed != null) {
                    users.filterNot { userNotAllowed.contains(it.userId) }
                } else {
                    users.filterNot { existingChatUserIds.contains(it.userId) }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("SelectUsersDialog", "Error loading users", exception)
            }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar Usuários") },
        text = {
            Column(modifier = Modifier.padding(8.dp)) {
                if (users.isEmpty()) {
                    Text("Nenhum usuário encontrado")
                } else {
                    users.forEach { user ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedUsers.contains(user),
                                onCheckedChange = { isChecked ->
                                    selectedUsers = if (isChecked) {
                                        if (isMultipleSelection) {
                                            (selectedUsers + user).toMutableList()
                                        } else {
                                            mutableListOf(user) // Permite selecionar apenas um se não for múltipla seleção
                                        }
                                    } else {
                                        if (isMultipleSelection) {
                                            (selectedUsers - user).toMutableList()
                                        } else {
                                            mutableListOf() // Desmarcar caso não seja múltipla
                                        }
                                    }
                                }
                            )
                            Text(text = user.name ?: "Desconhecido")
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

                    // Verificando se a lista de usuários no chat existente é nula
                    val usersToAdd = if (chatActionParams.currentUsersInChat == null) {
                        chatUsers // Se for nula, adicionamos todos os usuários selecionados, incluindo o atual
                    } else {
                        // Filtrando a lista para remover o próprio usuário logado, se ele já estiver no chat
                        (chatActionParams.currentUsersInChat + chatUsers).distinct()
                    }

                    if (chatActionParams.create) {
                        // Criar um novo chat
                        val chatData = hashMapOf(
                            "userIds" to usersToAdd,
                            "messages" to emptyList<Map<String, Any>>(),
                            "userTyping" to emptyList<Map<String, Any>>()
                        )

                        firestore.collection("chats")
                            .add(chatData)
                            .addOnSuccessListener { documentReference ->
                                val newChatId = documentReference.id
                                onUserSelected(newChatId)
                                onDismiss()
                            }
                            .addOnFailureListener { exception ->
                                Log.e("SelectUsersDialog", "Error creating chat", exception)
                            }
                            .addOnCompleteListener {
                                isLoading = false
                            }
                    } else {
                        // Adicionar ao chat existente
                        val existingChatId = chatActionParams.existingChat
                        if (existingChatId != null) {
                            firestore.collection("chats").document(existingChatId)
                                .update("userIds", usersToAdd)
                                .addOnSuccessListener {
                                    onUserSelected("")
                                    onDismiss()
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("SelectUsersDialog", "Error updating chat", exception)
                                }
                                .addOnCompleteListener {
                                    isLoading = false
                                }
                        }
                    }
                },
                enabled = selectedUsers.isNotEmpty() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text(if (chatActionParams.create) "Criar Chat" else "Adicionar ao Chat")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
