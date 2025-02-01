package com.example.firebaseauth.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.firebaseauth.R
import com.example.firebaseauth.enums.StatusMessage
import com.example.firebaseauth.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MessageBox(
    markAsRead: () -> Unit,
    checkMessageUpdate: (ChatMessage) -> ChatMessage?,
    chatMessageData: ChatMessage
) {
    // Observa o estado da mensagem no ViewModel
    var message by remember {  mutableStateOf(chatMessageData)  }

    val formattedTime = remember { mutableStateOf("") }
    val status = remember { mutableStateOf(StatusMessage.WAITING) }

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(message.message.timestamp) {
        val timestamp = message.message.timestamp
        val localDateTime = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        formattedTime.value = localDateTime.format(formatter)
    }

    LaunchedEffect(message.message) {
        when {
            message.message.sent && message.message.read.size < message.usersInChat-1 -> {
                status.value = StatusMessage.SENT
            }
            message.message.sent && message.message.read.size >= message.usersInChat-1 -> {
                status.value = StatusMessage.READ
            }
            else -> {
                status.value = StatusMessage.WAITING
            }
        }
    }

    LaunchedEffect(isVisible) {
        while (isVisible) {
            withContext(Dispatchers.IO) {
                val chatMessage = checkMessageUpdate(message)
                chatMessage?.let {
                    message = it.copy()
                }
            }
            delay(500)
        }
    }

    // Se a mensagem não for de "selfOwner", chama markAsRead quando a imagem está visível
    if (!message.selfOwner) {
        LaunchedEffect(isVisible) {
            if (isVisible) {
                markAsRead()  // Marca a mensagem como lida quando visível
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .background(
                if (message.selfOwner) Color(0x80A5D6A7)
                else Color(0xFFE1F5FE), RoundedCornerShape(20.dp)
            )
            .onGloballyPositioned { coordinates ->
                val isOnScreen = coordinates.positionInWindow().y >= 0
                if (isOnScreen != isVisible) {
                    isVisible = isOnScreen
                }
            }
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
                horizontalArrangement =
                if (message.selfOwner) Arrangement.End
                else Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (message.selfOwner) {
                    Text(
                        text = message.user.name ?: "NOME",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                AsyncImage(
                    model = message.user.imageUrl ?: R.drawable.default_profile,
                    contentDescription = "image user",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                if (!message.selfOwner) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message.user.name ?: "NOME",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp)
            ) {
                Text(
                    text = message.message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formattedTime.value,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                if (message.selfOwner) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = status.value.icon,
                        contentDescription = "Status",
                        tint = status.value.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}


