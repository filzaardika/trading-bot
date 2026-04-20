from __future__ import annotations

import json
import time
from typing import Any

from bot.storage.db import connect


def save_signal(signal: dict[str, Any], raw_response: str) -> None:
    tp = signal.get("take_profit")
    tp_str = json.dumps(tp) if isinstance(tp, list) else (str(tp) if tp is not None else None)
    with connect() as c:
        c.execute(
            """INSERT OR REPLACE INTO signals
               (signal_id, ts, symbol, action, size_pct_equity, entry_type, limit_price,
                stop_loss, take_profit, confidence, rationale, ttl_seconds, raw_response)
               VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)""",
            (
                signal["signal_id"],
                int(time.time()),
                signal["symbol"],
                signal["action"],
                signal.get("size_pct_equity"),
                signal.get("entry_type"),
                signal.get("limit_price"),
                signal.get("stop_loss"),
                tp_str,
                signal.get("confidence"),
                signal.get("rationale"),
                signal.get("ttl_seconds"),
                raw_response,
            ),
        )


def save_trade(trade: dict[str, Any]) -> None:
    with connect() as c:
        c.execute(
            """INSERT OR IGNORE INTO trades
               (signal_id, order_id, client_order_id, ts, symbol, side, amount, price,
                status, pnl_usdt, meta)
               VALUES (?,?,?,?,?,?,?,?,?,?,?)""",
            (
                trade.get("signal_id"),
                trade.get("order_id"),
                trade.get("client_order_id"),
                int(time.time()),
                trade["symbol"],
                trade["side"],
                trade["amount"],
                trade.get("price"),
                trade.get("status"),
                trade.get("pnl_usdt"),
                json.dumps(trade.get("meta", {})),
            ),
        )


def set_sl_cooldown(symbol: str, until_ts: int) -> None:
    with connect() as c:
        c.execute(
            "INSERT OR REPLACE INTO sl_cooldowns (symbol, until_ts) VALUES (?, ?)",
            (symbol, until_ts),
        )


def in_sl_cooldown(symbol: str) -> bool:
    now = int(time.time())
    with connect() as c:
        row = c.execute(
            "SELECT until_ts FROM sl_cooldowns WHERE symbol = ?", (symbol,)
        ).fetchone()
    return bool(row and row["until_ts"] > now)


def kv_get(key: str) -> str | None:
    with connect() as c:
        row = c.execute("SELECT value FROM kv WHERE key = ?", (key,)).fetchone()
    return row["value"] if row else None


def kv_set(key: str, value: str) -> None:
    with connect() as c:
        c.execute(
            "INSERT OR REPLACE INTO kv (key, value, updated_at) VALUES (?, ?, ?)",
            (key, value, int(time.time())),
        )
