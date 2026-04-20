package com.filzaardika.tradingbot.data

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object ApiFactory {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    fun create(ctx: Context, hostOverride: String? = null, tokenOverride: String? = null): BotApi {
        val host = hostOverride ?: SecureStore.host(ctx) ?: error("not paired")
        val token = tokenOverride ?: SecureStore.token(ctx) ?: ""

        val baseUrl = normalizeBase(host)

        val auth = Interceptor { chain ->
            val req = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(req)
        }

        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(auth)
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(BotApi::class.java)
    }

    fun normalizeBase(host: String): String {
        var h = host.trim()
        if (!h.startsWith("http://") && !h.startsWith("https://")) h = "http://$h"
        if (!h.endsWith("/")) h += "/"
        return h
    }
}
