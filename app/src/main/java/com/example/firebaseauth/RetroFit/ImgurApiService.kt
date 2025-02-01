package com.example.firebaseauth.RetroFit

import com.example.firebaseauth.model.ImgurUploadResponse
import okhttp3.MultipartBody
import retrofit2.http.*

interface ImgurApiService {

    @Multipart
    @POST("3/image")
    suspend fun uploadImage(
        @Header("Authorization") authorization: String,
        @Part image: MultipartBody.Part
    ): ImgurUploadResponse
}