package com.example.firebaseauth.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow

// Função para verificar se o item está visível na tela
fun LayoutCoordinates.positionInWindow(): Offset {
    return this.positionInWindow()
}