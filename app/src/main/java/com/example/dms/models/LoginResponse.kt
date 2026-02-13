package com.example.dms.models

import com.google.gson.annotations.SerializedName

/**
 * Ответ Laravel при логине в формате:
 *
 * {
 *   "status_code": 200,
 *   "message": "Вход выполнен успешно",
 *   "data": {
 *     "token": {
 *       "access_token": "...",
 *       "type": "Bearer"
 *     },
 *     "user": { ... }
 *   }
 * }
 */
data class LoginResponse(
    @SerializedName("status_code") val statusCode: Int? = null,
    val message: String? = null,
    val data: LoginData? = null
) {
    /** Удобный метод для получения access_token из вложенного объекта. */
    fun getTokenOrAccessToken(): String? = data?.token?.accessToken

    /** Удобный метод для получения пользователя. */
    fun getUserOrFromData(): AuthUser? = data?.user
}

data class LoginData(
    val token: TokenData? = null,
    val user: AuthUser? = null
)

data class TokenData(
    @SerializedName("access_token") val accessToken: String? = null,
    val type: String? = null
)

data class AuthUser(
    val id: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val role: String? = null
)
