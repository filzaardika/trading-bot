from __future__ import annotations

import time
from dataclasses import dataclass

from bot.config import settings
from bot.storage.models import in_sl_cooldown, kv_get, kv_set
from bot.utils.logger import logger


@dataclass
class RiskDecision:
    allow: bool
    reason: str = ""
    clamped_size_pct: float | None = None
    clamped_leverage: int | None = None


def _today_key() -> str:
    return time.strftime("%Y-%m-%d", time.gmtime())


def record_start_equity_if_missing(equity: float) -> None:
    if kv_get("start_equity") is None:
        kv_set("start_equity", str(equity))
        logger.info(f"Recorded start equity: {equity}")


def record_day_start_equity(equity: float) -> None:
    key = f"day_start_equity:{_today_key()}"
    if kv_get(key) is None:
        kv_set(key, str(equity))


def daily_drawdown_pct(current_equity: float) -> float:
    key = f"day_start_equity:{_today_key()}"
    v = kv_get(key)
    if not v:
        return 0.0
    start = float(v)
    if start <= 0:
        return 0.0
    return (current_equity - start) / start * 100.0


def total_drawdown_pct(current_equity: float) -> float:
    v = kv_get("start_equity")
    if not v:
        return 0.0
    start = float(v)
    if start <= 0:
        return 0.0
    return (current_equity - start) / start * 100.0


def kill_switch_active() -> bool:
    return kv_get("kill_switch") == "1"


def activate_kill_switch(reason: str) -> None:
    kv_set("kill_switch", "1")
    kv_set("kill_switch_reason", reason)
    logger.error(f"KILL SWITCH ACTIVATED: {reason}")


def evaluate(
    signal: dict,
    current_equity: float,
    open_positions_count: int,
) -> RiskDecision:
    action = signal.get("action")
    if action not in ("buy", "sell", "close", "hold"):
        return RiskDecision(False, f"unknown action {action}")

    if action in ("hold", "close"):
        return RiskDecision(True)

    if kill_switch_active():
        return RiskDecision(False, "kill switch active")

    # Daily loss halt
    dd_day = daily_drawdown_pct(current_equity)
    if dd_day <= -abs(settings.daily_loss_halt_pct):
        return RiskDecision(False, f"daily loss halt {dd_day:.2f}%")

    # Total drawdown kill
    dd_total = total_drawdown_pct(current_equity)
    if dd_total <= -abs(settings.max_drawdown_kill_pct):
        activate_kill_switch(f"drawdown {dd_total:.2f}%")
        return RiskDecision(False, f"drawdown kill {dd_total:.2f}%")

    # Positions cap
    if open_positions_count >= settings.max_concurrent_positions:
        return RiskDecision(False, "max positions reached")

    # Cooldown
    if in_sl_cooldown(signal["symbol"]):
        return RiskDecision(False, f"SL cooldown {signal['symbol']}")

    # Size clamp
    requested = float(signal.get("size_pct_equity") or 0.0)
    if requested <= 0:
        return RiskDecision(False, "size <= 0")
    hard_cap = settings.max_risk_pct_per_trade
    if settings.mode == "live":
        hard_cap = min(hard_cap, settings.live_size_cap_pct)
    clamped = min(requested, hard_cap)

    return RiskDecision(
        True,
        reason=f"clamped size {requested}->{clamped}",
        clamped_size_pct=clamped,
        clamped_leverage=min(
            int(signal.get("leverage") or settings.max_leverage), settings.max_leverage
        ),
    )
