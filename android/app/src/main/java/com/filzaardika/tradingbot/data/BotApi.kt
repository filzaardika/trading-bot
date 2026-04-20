package com.filzaardika.tradingbot.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BotApi {
    @GET("health") suspend fun health(): HealthDto

    @POST("auth/pair") suspend fun pair(@Body req: PairReq): PairResp

    @GET("dashboard") suspend fun dashboard(): DashboardDto
    @GET("positions") suspend fun positions(): List<PositionDto>
    @GET("trades") suspend fun trades(@Query("limit") limit: Int = 100): List<TradeDto>
    @GET("signals") suspend fun signals(@Query("limit") limit: Int = 100): List<SignalDto>
    @GET("settings") suspend fun settings(): SettingsDto
    @GET("logs/tail") suspend fun logsTail(@Query("limit") limit: Int = 200): LogTailResp

    @POST("control/pause") suspend fun pause(): ControlResp
    @POST("control/resume") suspend fun resume(): ControlResp
    @POST("control/cycle-now") suspend fun cycleNow(): ControlResp
    @POST("control/flatten") suspend fun flatten(): ControlResp
    @POST("control/kill") suspend fun kill(): ControlResp
    @POST("control/kill/reset") suspend fun killReset(): ControlResp
}
