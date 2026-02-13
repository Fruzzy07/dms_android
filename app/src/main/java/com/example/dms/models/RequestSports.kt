package com.example.dms.models

import com.google.gson.annotations.SerializedName

data class RequestSports(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    val sport: String,
    val teacher: String,
    val time: String,
    val status: String, // pending, accepted, rejected
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class RequestSportsCreate(
    val sport: String,
    val teacher: String,
    val time: String
)

