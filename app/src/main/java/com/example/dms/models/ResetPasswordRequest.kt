package com.example.dms.models

data class ResetPasswordRequest(
    val old_password: String,
    val new_password: String,
    val confirm_password: String
)
