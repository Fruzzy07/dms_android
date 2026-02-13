package com.example.dms.models

import com.google.gson.annotations.SerializedName

/**
 * Универсальная обёртка ответа бекенда (Laravel):
 *
 * {
 *   "status_code": 200,
 *   "message": "....",
 *   "data": <T>
 * }
 */
data class ApiResponse<T>(
    @SerializedName("status_code") val statusCode: Int? = null,
    val message: String? = null,
    val data: T? = null
)

