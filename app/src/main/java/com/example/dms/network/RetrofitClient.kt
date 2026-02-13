package com.example.dms.network

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // 10.0.2.2 — это localhost хоста с эмулятора. На реальном устройстве укажите IP компьютера в той же Wi‑Fi сети, например "http://192.168.1.100:8000/"
    private const val BASE_URL = "http://10.0.2.2:8000/"

    private const val CONNECT_TIMEOUT_SEC = 30L
    private const val READ_TIMEOUT_SEC = 30L
    private const val WRITE_TIMEOUT_SEC = 30L

    fun getInstance(token: String? = null): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            // В debug это сильно помогает понять: 401/500/connection refused/не тот URL
            level = HttpLoggingInterceptor.Level.BODY
        }

        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
            .addInterceptor(logging)

        token?.let {
            clientBuilder.addInterceptor(Interceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(newRequest)
            })
        }

        val gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
