package com.example.firebaseauth.model

data class ChatMessage(
    val text: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val senderEmail: String = "",
    val receiverEmail: String = "",
    val timestamp: Long = System.currentTimeMillis()
)