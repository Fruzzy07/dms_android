package com.example.dms.models

data class NewsRequest(
    val title: String,
    val description: String,
    val photo: String? = null
)
