package com.example.firebaseauth.model

data class ImgurUploadResponse(
    val data: UploadedImageData?,
    val success: Boolean,
    val status: Int
)

data class UploadedImageData(
    val id: String,
    val link: String
)