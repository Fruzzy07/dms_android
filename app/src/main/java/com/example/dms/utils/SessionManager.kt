package com.example.dms.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("dms_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TOKEN_KEY = "token"
        private const val USER_ID_KEY = "user_id"
        private const val USER_EMAIL_KEY = "user_email"
        private const val USER_PHONE_KEY = "user_phone"
        private const val USER_PASSWORD_KEY = "user_password"
        private const val USER_NAME_KEY = "user_name"
        private const val USER_PHOTO_KEY = "user_photo"
        private const val USER_ROLE_KEY = "user_role"
        private const val RESIDENCE_CHANGE_MODE_KEY = "residence_change_mode"
        private const val RESIDENCE_LAST_ACCEPTED_REQUEST_ID_KEY = "residence_last_accepted_request_id"
        private const val RESIDENCE_CHANGE_BASE_ACCEPTED_REQUEST_ID_KEY = "residence_change_base_accepted_request_id"
    }

    // --- Token ---
    fun saveToken(token: String) {
        sharedPreferences.edit().putString(TOKEN_KEY, token).apply()
    }

    fun getToken(): String? = sharedPreferences.getString(TOKEN_KEY, null)

    fun clearToken() {
        sharedPreferences.edit().remove(TOKEN_KEY).apply()
    }

    /** Полная очистка сессии при выходе из аккаунта */
    fun logout() {
        sharedPreferences.edit()
            .remove(TOKEN_KEY)
            .remove(USER_ID_KEY)
            .remove(USER_EMAIL_KEY)
            .remove(USER_PHONE_KEY)
            .remove(USER_PASSWORD_KEY)
            .remove(USER_NAME_KEY)
            .remove(USER_PHOTO_KEY)
            .remove(USER_ROLE_KEY)
            .apply()
    }

    // --- User ID ---
    fun saveUserId(id: String) {
        sharedPreferences.edit().putString(USER_ID_KEY, id).apply()
    }

    fun getUserId(): String? = sharedPreferences.getString(USER_ID_KEY, null)

    // --- Email ---
    fun saveUserEmail(email: String) {
        sharedPreferences.edit().putString(USER_EMAIL_KEY, email).apply()
    }

    fun getUserEmail(): String? = sharedPreferences.getString(USER_EMAIL_KEY, null)

    // --- Phone ---
    fun saveUserPhone(phone: String) {
        sharedPreferences.edit().putString(USER_PHONE_KEY, phone).apply()
    }

    fun getUserPhone(): String? = sharedPreferences.getString(USER_PHONE_KEY, null)

    // --- Password ---
    fun saveUserPassword(password: String) {
        sharedPreferences.edit().putString(USER_PASSWORD_KEY, password).apply()
    }

    fun getUserPassword(): String? = sharedPreferences.getString(USER_PASSWORD_KEY, null)

    // --- User Name ---
    fun saveUserName(name: String) {
        sharedPreferences.edit().putString(USER_NAME_KEY, name).apply()
    }

    fun getUserName(): String? = sharedPreferences.getString(USER_NAME_KEY, null)

    // --- User Photo URL ---
    fun saveUserPhoto(url: String) {
        sharedPreferences.edit().putString(USER_PHOTO_KEY, url).apply()
    }

    fun getUserPhoto(): String? = sharedPreferences.getString(USER_PHOTO_KEY, null)

    // --- User Role ---
    fun saveUserRole(role: String) {
        sharedPreferences.edit().putString(USER_ROLE_KEY, role).apply()
    }

    fun getUserRole(): String? = sharedPreferences.getString(USER_ROLE_KEY, null)

    // --- Residence UI mode (when user is changing room) ---
    fun setResidenceChangeMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(RESIDENCE_CHANGE_MODE_KEY, enabled).apply()
    }

    fun isResidenceChangeMode(): Boolean = sharedPreferences.getBoolean(RESIDENCE_CHANGE_MODE_KEY, false)

    fun setResidenceLastAcceptedRequestId(id: Int) {
        sharedPreferences.edit().putInt(RESIDENCE_LAST_ACCEPTED_REQUEST_ID_KEY, id).apply()
    }

    fun getResidenceLastAcceptedRequestId(): Int =
        sharedPreferences.getInt(RESIDENCE_LAST_ACCEPTED_REQUEST_ID_KEY, 0)

    fun setResidenceChangeBaseAcceptedRequestId(id: Int) {
        sharedPreferences.edit().putInt(RESIDENCE_CHANGE_BASE_ACCEPTED_REQUEST_ID_KEY, id).apply()
    }

    fun getResidenceChangeBaseAcceptedRequestId(): Int =
        sharedPreferences.getInt(RESIDENCE_CHANGE_BASE_ACCEPTED_REQUEST_ID_KEY, 0)
}
