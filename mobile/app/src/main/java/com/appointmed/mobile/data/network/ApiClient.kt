package com.appointmed.mobile.data.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

import com.appointmed.mobile.BuildConfig

object ApiClient {
    val BASE_URL = if (BuildConfig.DEBUG) {
        "http://10.0.2.2:8080/api/"
    } else {
        "https://appointmed-backend-sdjk.onrender.com/api/"
    }

    val IMAGE_BASE_URL = if (BuildConfig.DEBUG) {
        "http://10.0.2.2:8080"
    } else {
        "https://appointmed-backend-sdjk.onrender.com"
    }

    fun create(context: Context): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
