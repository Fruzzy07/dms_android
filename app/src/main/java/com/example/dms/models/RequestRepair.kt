package com.example.dms.models

import com.google.gson.annotations.SerializedName

data class RequestRepair(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    val description: String,
    val status: String, // pending, accepted, rejected
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class RequestRepairCreate(
    val description: String
)

