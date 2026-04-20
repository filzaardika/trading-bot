"""Read live snapshot from bot state + storage."""
from __future__ import annotations

import time

from bot.api.state import STATE
from bot.config import settings
from bot.storage.models import kv_get


def _start_equity() -> float:
    v = kv_get("start_equity")
    try:
        return float(v) if v else 0.0
    except ValueError:
        return 0.0


def _day_start_equity() -> float:
    today = time.strftime("%Y-%m-%d", time.gmtime())
    v = kv_get(f"day_start_equity:{today}")
    try:
        return float(v) if v else 0.0
    except ValueError:
        return 0.0


def _latest_equity() -> float:
    v = kv_get("latest_equity")
    if v:
        try:
            return float(v)
        except ValueError:
            pass
    return _start_equity()


def _latest_open_positions_count() -> int:
    v = kv_get("open_positions_count")
    if v:
        try:
            return int(v)
        except ValueError:
            pass
    return 0


def build_dashboard() -> dict:
    equity = _latest_equity()
    day_start = _day_start_equity() or _start_equity() or equity or 1.0
    pnl_today = equity - day_start
    pnl_today_pct = (pnl_today / day_start) * 100.0 if day_start else 0.0

    kill = kv_get("kill_switch") == "1"
    if kill:
        status = "error"
    elif STATE.paused:
        status = "paused"
    elif STATE.last_cycle_error:
        status = "error"
    else:
        status = "running"

    now = time.time()
    next_in = max(
        0,
        int(settings.cycle_seconds - ((now - STATE.last_cycle_ts) if STATE.last_cycle_ts else 0)),
    )
    progress = (
        1.0 - (next_in / settings.cycle_seconds) if settings.cycle_seconds else 0.0
    )

    return {
        "equity": round(equity, 2),
        "starting_equity": round(_start_equity(), 2),
        "pnl_today": round(pnl_today, 2),
        "pnl_today_pct": round(pnl_today_pct, 3),
        "open_positions_count": _latest_open_positions_count(),
        "mode": settings.mode,
        "testnet": settings.bybit_testnet,
        "bot_status": status,
        "kill_switch": kill,
        "cycle_progress": round(max(0.0, min(1.0, progress)), 3),
        "seconds_to_next_cycle": next_in,
        "last_cycle_ts": int(STATE.last_cycle_ts),
        "last_cycle_error": STATE.last_cycle_error,
    }
