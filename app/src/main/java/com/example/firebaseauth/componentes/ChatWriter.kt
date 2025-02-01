package com.example.firebaseauth.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChatWriter(
    senderId: String,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    startTyping: () -> Unit,
    stopTyping: () -> Unit,
    usersTyping: List<String>, // Lista de usuários que estão digitando
    getUserName: suspend (String) -> String // Função suspensa para obter o nome do usuário
) {
    var messageText by remember { mutableStateOf("") }
    // Filtra os usuários que estão digitando, exceto o próprio usuário
    val otherUsersTyping = usersTyping.filter { it != senderId }
    var userName by remember { mutableStateOf("") }

    // Use LaunchedEffect para chamar a função suspensa quando a lista mudar
    LaunchedEffect(otherUsersTyping) {
        if (otherUsersTyping.isNotEmpty()) {
            userName = getUserName(otherUsersTyping.first())
        }
    }

    // Utiliza uma Column para dispor os elementos verticalmente
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Se houver usuários digitando, exibe o texto em uma linha separada
        if (otherUsersTyping.isNotEmpty()) {
            Text(
                text = if (otherUsersTyping.size == 1) {
                    "$userName está escrevendo..."
                } else {
                    "Vários usuários estão escrevendo..."
                },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Linha contendo o campo de texto e o botão
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = messageText,
                onValueChange = { newText ->
                    messageText = newText
                    if (newText.isNotBlank()) startTyping() else stopTyping() // Inicia ou para de digitar
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escreva uma mensagem...") }
            )

            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        onSendMessage(messageText)
                        messageText = ""
                        stopTyping() // Para de digitar quando envia a mensagem
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar")
            }
        }
    }
}
