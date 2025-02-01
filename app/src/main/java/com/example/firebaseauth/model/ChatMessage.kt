package com.example.firebaseauth.model

data class Message(
    val messageId: String = "",
    val text: String = "",
    val senderId: String = "",
    val sent: Boolean = false,
    val read: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
)

data class Chat(
    val chatId: String = "",
    val userIds: List<String> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val userTyping: List<String> = emptyList()
)

data class ChatMessage(
    val message: Message,
    val user: User,
    val selfOwner: Boolean,
    val usersInChat: Int = 0
)