package com.filzaardika.tradingbot.data

import kotlinx.serialization.Serializable

@Serializable
data class HealthDto(val ok: Boolean, val version: String = "")

@Serializable
data class PairReq(val token: String)

@Serializable
data class PairResp(
    val ok: Boolean,
    val bot_name: String = "",
    val mode: String = "paper",
    val testnet: Boolean = true
)

@Serializable
data class DashboardDto(
    val equity: Double = 0.0,
    val starting_equity: Double = 0.0,
    val pnl_today: Double = 0.0,
    val pnl_today_pct: Double = 0.0,
    val open_positions_count: Int = 0,
    val mode: String = "paper",
    val testnet: Boolean = true,
    val bot_status: String = "running",
    val kill_switch: Boolean = false,
    val cycle_progress: Double = 0.0,
    val seconds_to_next_cycle: Int = 0,
    val last_cycle_ts: Long = 0L,
    val last_cycle_error: String = ""
)

@Serializable
data class PositionDto(
    val id: String,
    val symbol: String,
    val side: String,
    val size: Double,
    val entry: Double,
    val mark: Double,
    val leverage: Double,
    val unrealized_pnl: Double,
    val unrealized_pnl_pct: Double,
    val stop_loss: Double? = null,
    val take_profit: Double? = null,
    val opened_at: Long
)

@Serializable
data class TradeDto(
    val id: Int,
    val signal_id: String? = null,
    val order_id: String? = null,
    val ts: Long,
    val symbol: String,
    val side: String,
    val amount: Double,
    val price: Double? = null,
    val status: String? = null,
    val pnl_usdt: Double? = null
)

@Serializable
data class SignalDto(
    val signal_id: String,
    val ts: Long,
    val symbol: String,
    val action: String,
    val size_pct_equity: Double? = null,
    val entry_type: String? = null,
    val limit_price: Double? = null,
    val stop_loss: Double? = null,
    val take_profit: String? = null,
    val confidence: Double? = null,
    val rationale: String? = null
)

@Serializable
data class SettingsDto(
    val mode: String,
    val testnet: Boolean,
    val universe_size: Int,
    val cycle_seconds: Int,
    val max_risk_pct_per_trade: Double,
    val max_concurrent_positions: Int,
    val max_leverage: Int,
    val daily_loss_halt_pct: Double,
    val max_drawdown_kill_pct: Double
)

@Serializable
data class ControlResp(val ok: Boolean, val message: String)

@Serializable
data class LogTailResp(val lines: List<String>)
