package com.filzaardika.tradingbot.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureStore {
    private const val PREFS = "trading_bot_secure"
    private const val K_HOST = "host"
    private const val K_TOKEN = "token"

    @Volatile private var prefs: SharedPreferences? = null

    private fun prefs(ctx: Context): SharedPreferences {
        prefs?.let { return it }
        synchronized(this) {
            prefs?.let { return it }
            val key = MasterKey.Builder(ctx)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val p = EncryptedSharedPreferences.create(
                ctx,
                PREFS,
                key,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            prefs = p
            return p
        }
    }

    fun saveCredentials(ctx: Context, host: String, token: String) {
        prefs(ctx).edit().putString(K_HOST, host).putString(K_TOKEN, token).apply()
    }

    fun clear(ctx: Context) {
        prefs(ctx).edit().clear().apply()
    }

    fun host(ctx: Context): String? = prefs(ctx).getString(K_HOST, null)
    fun token(ctx: Context): String? = prefs(ctx).getString(K_TOKEN, null)
    fun isPaired(ctx: Context): Boolean = !host(ctx).isNullOrBlank() && !token(ctx).isNullOrBlank()
}
