package com.filzaardika.tradingbot.data

import android.content.Context

sealed class ApiResult<out T> {
    data class Ok<T>(val value: T) : ApiResult<T>()
    data class Err(val message: String) : ApiResult<Nothing>()
}

class BotRepo(private val ctx: Context) {
    private fun api(): BotApi = ApiFactory.create(ctx)

    suspend fun <T> safe(block: suspend BotApi.() -> T): ApiResult<T> = try {
        ApiResult.Ok(block(api()))
    } catch (e: Exception) {
        ApiResult.Err(e.message ?: e::class.java.simpleName)
    }

    suspend fun ping(host: String): ApiResult<HealthDto> = try {
        val api = ApiFactory.create(ctx, hostOverride = host, tokenOverride = "")
        ApiResult.Ok(api.health())
    } catch (e: Exception) {
        ApiResult.Err(e.message ?: "unreachable")
    }

    suspend fun pair(host: String, token: String): ApiResult<PairResp> = try {
        val api = ApiFactory.create(ctx, hostOverride = host, tokenOverride = token)
        val r = api.pair(PairReq(token))
        if (r.ok) SecureStore.saveCredentials(ctx, host, token)
        ApiResult.Ok(r)
    } catch (e: Exception) {
        ApiResult.Err(e.message ?: "pair failed")
    }

    suspend fun dashboard() = safe { dashboard() }
    suspend fun positions() = safe { positions() }
    suspend fun trades() = safe { trades() }
    suspend fun signals() = safe { signals() }
    suspend fun settings() = safe { settings() }
    suspend fun logsTail() = safe { logsTail() }

    suspend fun pause() = safe { pause() }
    suspend fun resume() = safe { resume() }
    suspend fun cycleNow() = safe { cycleNow() }
    suspend fun flatten() = safe { flatten() }
    suspend fun kill() = safe { kill() }
    suspend fun killReset() = safe { killReset() }
}
