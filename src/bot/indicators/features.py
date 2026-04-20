from __future__ import annotations

from typing import Any

import pandas as pd


def extract_snapshot(df: pd.DataFrame) -> dict[str, Any]:
    """Return latest indicator snapshot as plain dict (for Claude prompt)."""
    if df.empty:
        return {}
    last = df.iloc[-1]
    prev = df.iloc[-2] if len(df) > 1 else last

    def f(v: Any) -> float | None:
        try:
            fv = float(v)
            if pd.isna(fv):
                return None
            return round(fv, 8)
        except Exception:
            return None

    return {
        "close": f(last["close"]),
        "close_prev": f(prev["close"]),
        "ema_20": f(last.get("ema_20")),
        "ema_50": f(last.get("ema_50")),
        "ema_200": f(last.get("ema_200")),
        "rsi_14": f(last.get("rsi_14")),
        "atr_14": f(last.get("atr_14")),
        "macd": f(last.get("macd")),
        "macd_signal": f(last.get("macd_signal")),
        "macd_hist": f(last.get("macd_hist")),
        "bb_upper": f(last.get("bb_upper")),
        "bb_lower": f(last.get("bb_lower")),
        "bb_mid": f(last.get("bb_mid")),
    }


def ohlcv_tail(df: pd.DataFrame, n: int = 30) -> list[list[float]]:
    tail = df.tail(n)
    rows = []
    for ts, r in tail.iterrows():
        rows.append(
            [
                int(ts.value // 1_000_000),
                float(r["open"]),
                float(r["high"]),
                float(r["low"]),
                float(r["close"]),
                float(r["volume"]),
            ]
        )
    return rows
