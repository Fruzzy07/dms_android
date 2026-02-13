package com.example.dms.models

data class UserProfile(
    val id: Int,
    val name: String,
    val email: String,
    val role: String?,
    val lastname: String,
    val middlename: String,
    val phone_number: String,
    val uni_id: String
)

