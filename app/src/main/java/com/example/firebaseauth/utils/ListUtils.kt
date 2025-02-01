package com.example.firebaseauth.utils

fun List<String>.addToCopyNoDuplicates(newElement: String): List<String> {
    if(newElement in this)
        throw IllegalArgumentException("Duplicate element");
    val newList: List<String> = this + newElement
    return newList;
}