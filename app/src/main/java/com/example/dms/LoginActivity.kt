package com.example.dms

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dms.models.LoginRequest
import com.example.dms.models.LoginResponse
import com.example.dms.network.RetrofitClient
import com.example.dms.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        // Если токен есть → на главную
        if (!sessionManager.getToken().isNullOrEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val email = findViewById<EditText>(R.id.inputEmail)
        val pass = findViewById<EditText>(R.id.inputPassword)
        val btn = findViewById<Button>(R.id.btnLogin)
        val forgotPassword = findViewById<android.widget.TextView>(R.id.forgotPassword)

        forgotPassword?.setOnClickListener {
            Toast.makeText(this, "Функция восстановления пароля", Toast.LENGTH_SHORT).show()
        }

        btn.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passText = pass.text.toString().trim()

            if (emailText.isEmpty() || passText.isEmpty()) {
                Toast.makeText(this, "Введите email и пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Показываем индикатор загрузки
            btn.isEnabled = false
            btn.text = "Signing in..."

            val request = LoginRequest(emailText, passText)

            RetrofitClient.getInstance().login(request)
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        // Восстанавливаем кнопку
                        btn.isEnabled = true
                        btn.text = "Sign in"
                        
                        val body = response.body()
                        val token = body?.getTokenOrAccessToken()
                        val user = body?.getUserOrFromData()

                        if (response.isSuccessful && !token.isNullOrBlank() && user != null) {
                            Log.d("LoginActivity", "User object from server: $user")
                            Log.d("LoginActivity", "User role from server: '${user.role}'")

                            sessionManager.saveToken(token)
                            sessionManager.saveUserName(user.name ?: "")
                            sessionManager.saveUserEmail(user.email ?: "")
                            sessionManager.saveUserId(user.id?.toString() ?: "")
                            sessionManager.saveUserRole(user.role ?: "")

                            Log.d("LoginActivity", "Saved role: '${sessionManager.getUserRole()}'")

                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Log.e("LoginActivity", "Ошибка входа: code=${response.code()}, body=$body, tokenNull=${token == null}, userNull=${user == null}")
                            Toast.makeText(this@LoginActivity, "Ошибка входа", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        // Восстанавливаем кнопку
                        btn.isEnabled = true
                        btn.text = "Sign in"
                        
                        Log.e("LoginActivity", "Сетевая ошибка: ${t.message}")
                        Toast.makeText(this@LoginActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
