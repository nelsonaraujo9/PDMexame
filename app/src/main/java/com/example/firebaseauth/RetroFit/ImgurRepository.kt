package com.example.firebaseauth.RetroFit

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ImgurRepository(private val authManager: ImgurAuthManager) {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.imgur.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ImgurApiService::class.java)

    suspend fun uploadImage(imageBytes: ByteArray): String? {
        return try {
            val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), imageBytes)
            val imagePart = MultipartBody.Part.createFormData("image", "upload.jpg", requestBody)

            val accessToken = authManager.getAccessToken()
            val response = apiService.uploadImage("Client-ID $accessToken", imagePart)

            if (response.success) {
                response.data?.link
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}