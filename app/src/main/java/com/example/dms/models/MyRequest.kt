package com.example.dms.models

data class MyRequest(
    val id: Int,
    val type: RequestType, // LIVE, REPAIR, SPORTS
    val title: String,
    val description: String,
    val status: String, // pending, accepted, rejected
    val createdAt: String? = null
)

enum class RequestType {
    LIVE,      // Запрос на проживание
    REPAIR,    // Запрос на ремонт
    SPORTS     // Запись на физкультуру
}

