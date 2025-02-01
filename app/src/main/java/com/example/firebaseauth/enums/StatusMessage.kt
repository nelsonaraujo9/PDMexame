package com.example.firebaseauth.enums

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class StatusMessage(
    val icon: ImageVector,
    val color: Color
) {
    WAITING(Icons.Default.AccessTime, Color.Gray),
    SENT(Icons.Default.Check, Color.Gray),
    READ(Icons.Default.Check, Color.Green)
}