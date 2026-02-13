package com.example.dms.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.dms.R
import com.example.dms.models.ResetPasswordRequest
import com.example.dms.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.launch
import com.example.dms.utils.SessionManager

class ProfileFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var profileName: MaterialTextView
    private lateinit var profileId: MaterialTextView
    private lateinit var profileEmail: TextInputEditText
    private lateinit var profilePhone: TextInputEditText
    private lateinit var passwordEditIcon: ImageView
    private lateinit var passwordRow: View
    private lateinit var saveProfileButton: MaterialButton
    private lateinit var logoutButton: MaterialButton

    private val PREFS_NAME = "user_prefs"
    private val KEY_TOKEN = "token" // токен хранится в SharedPreferences

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = result.data?.data
                if (imageUri != null) {
                    grantUriPermission(imageUri)
                    saveProfileImageUri(imageUri)
                    loadImage(imageUri)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        initViews(view)
        loadSavedImage()

        // после загрузки аватара подгружаем профиль с API
        loadUserDataFromApi()

        profileImage.setOnClickListener { openImagePicker() }

        passwordEditIcon.setOnClickListener { showResetPasswordDialog() }
        passwordRow.setOnClickListener { showResetPasswordDialog() }

        saveProfileButton.setOnClickListener {
            Toast.makeText(requireContext(), "Изменения сохранены", Toast.LENGTH_SHORT).show()
        }

        logoutButton.setOnClickListener { performLogout() }

        return view
    }

    private fun initViews(view: View) {
        profileImage = view.findViewById(R.id.profileImage)
        profileName = view.findViewById(R.id.profileName)
        profileId = view.findViewById(R.id.profileId)
        profileEmail = view.findViewById(R.id.profileEmail)
        profilePhone = view.findViewById(R.id.profilePhone)
        passwordEditIcon = view.findViewById(R.id.passwordEditIcon)
        passwordRow = view.findViewById(R.id.passwordRow)
        saveProfileButton = view.findViewById(R.id.saveProfileButton)
        logoutButton = view.findViewById(R.id.logoutButton)
    }

    private fun performLogout() {
        SessionManager(requireContext()).logout()
        startActivity(Intent(requireContext(), com.example.dms.LoginActivity::class.java))
        requireActivity().finish()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        pickImageLauncher.launch(intent)
    }

    private fun loadImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .into(profileImage)
    }

    private fun saveProfileImageUri(uri: Uri) {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val session = SessionManager(requireContext())
        val userId = session.getUserId() ?: "default"
        val key = "profile_image_$userId"
        prefs.edit().putString(key, uri.toString()).apply()
    }

    private fun grantUriPermission(uri: Uri) {
        try {
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {}
    }

    private fun loadSavedImage() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val session = SessionManager(requireContext())
        val userId = session.getUserId() ?: "default"
        val key = "profile_image_$userId"
        val savedUri = prefs.getString(key, null)
        if (!savedUri.isNullOrEmpty()) {
            loadImage(Uri.parse(savedUri))
        }
    }

    // ✅ Загружаем данные пользователя с API с использованием токена
    private fun loadUserDataFromApi() {
        lifecycleScope.launch {
            try {
                // Получаем токен из SessionManager
                val sessionManager = SessionManager(requireContext())
                val token = sessionManager.getToken() ?: return@launch

                // Создаём ApiService с токеном
                val api = RetrofitClient.getInstance(token)
                val res = api.getProfile() // suspend fun
                val user = res.data

                // Обновляем UI
                if (user == null) {
                    Toast.makeText(requireContext(), "Профиль пустой", Toast.LENGTH_SHORT).show()
                    Log.e("ProfileFragment", "Profile is null. apiMessage=${res.message}")
                    return@launch
                }

                profileName.text = "${user.lastname} ${user.name} ${user.middlename}"
                profileId.text = user.uni_id
                profileEmail.setText(user.email)
                profilePhone.setText(user.phone_number)

            } catch (e: Exception) {
                Log.e("ProfileFragment", "loadUserDataFromApi failed", e)
                Toast.makeText(requireContext(), "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showResetPasswordDialog() {
        val dialogView = layoutInflater.inflate(
            R.layout.dialog_change_password,
            null
        )

        val oldPass = dialogView.findViewById<TextInputEditText>(R.id.oldPassword)
        val newPass = dialogView.findViewById<TextInputEditText>(R.id.newPassword)
        val confirmPass = dialogView.findViewById<TextInputEditText>(R.id.confirmPassword)
        val btnSave = dialogView.findViewById<MaterialButton>(R.id.btnSavePassword)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnSave.setOnClickListener {
            val old = oldPass.text.toString().trim()
            val new = newPass.text.toString().trim()
            val confirm = confirmPass.text.toString().trim()

            when {
                old.isEmpty() || new.isEmpty() || confirm.isEmpty() -> {
                    Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                }
                new != confirm -> {
                    Toast.makeText(requireContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    resetPassword(old, new, confirm)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }


    private fun resetPassword(oldPassword: String, newPassword: String, confirmPassword: String) {
        val token = SessionManager(requireContext()).getToken() ?: return
        val api = RetrofitClient.getInstance(token)
        val request = ResetPasswordRequest(
            old_password = oldPassword,
            new_password = newPassword,
            confirm_password = confirmPassword
        )
        api.resetPassword(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (!isAdded) return
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Пароль изменён", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Ошибка: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                if (!isAdded) return
                Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
