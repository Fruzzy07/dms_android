package com.example.dms.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dms.R
import com.example.dms.models.News
import com.example.dms.models.NewsRequest
import com.example.dms.network.RetrofitClient
import com.example.dms.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddEditNewsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var newsId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_news)

        sessionManager = SessionManager(this)

        val titleEt = findViewById<EditText>(R.id.etNewsTitle)
        val descEt = findViewById<EditText>(R.id.etNewsDescription)
        val saveBtn = findViewById<Button>(R.id.btnSaveNews)

        // Если передан объект новости → редактирование
        newsId = intent.getIntExtra("news_id", -1)
        if (newsId != -1) {
            titleEt.setText(intent.getStringExtra("news_title"))
            descEt.setText(intent.getStringExtra("news_description"))
        }

        saveBtn.setOnClickListener {
            val title = titleEt.text.toString().trim()
            val desc = descEt.text.toString().trim()

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val token = sessionManager.getToken() ?: return@setOnClickListener
            val api = RetrofitClient.getInstance(token)
            val request = NewsRequest(title, desc)

            if (newsId != -1) {
                // --- Обновление ---
                api.updateNews(newsId!!, request).enqueue(object : Callback<News> {
                    override fun onResponse(call: Call<News>, response: Response<News>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@AddEditNewsActivity, "Новость обновлена", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@AddEditNewsActivity, "Ошибка обновления", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<News>, t: Throwable) {
                        Toast.makeText(this@AddEditNewsActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                // --- Создание ---
                api.createNews(request).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@AddEditNewsActivity, "Новость добавлена", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@AddEditNewsActivity, "Ошибка создания", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@AddEditNewsActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}
