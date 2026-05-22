package com.appointmed.mobile.data.network

import android.content.Context
import com.appointmed.mobile.data.local.Prefs
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()

        // Skip adding token for login and register
        val path = request.url.encodedPath
        if (!path.contains("login") && !path.contains("register")) {
            val token = Prefs(context).getToken()
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
        }

        requestBuilder.addHeader("Accept", "application/json")
        requestBuilder.addHeader("Content-Type", "application/json")
        requestBuilder.addHeader("Cache-Control", "no-cache")
        return chain.proceed(requestBuilder.build())
    }
}
