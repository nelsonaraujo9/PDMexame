package com.example.firebaseauth.RetroFit

class ImgurAuthManager {

    private val clientId = "1e5595e7d403e80"
    private val clientSecret = "5eb3e068bc4dbdb512c733ed8f9641132636314c"

    fun getAccessToken(): String {
        return clientId
    }
    fun getClientSecret(): String {
        return clientSecret
    }
}
