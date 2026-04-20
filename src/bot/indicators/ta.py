from __future__ import annotations

import pandas as pd
import pandas_ta as ta


def ohlcv_to_df(ohlcv: list[list[float]]) -> pd.DataFrame:
    df = pd.DataFrame(ohlcv, columns=["ts", "open", "high", "low", "close", "volume"])
    df["ts"] = pd.to_datetime(df["ts"], unit="ms", utc=True)
    df = df.set_index("ts").astype(float)
    return df


def add_indicators(df: pd.DataFrame) -> pd.DataFrame:
    out = df.copy()
    out["ema_20"] = ta.ema(out["close"], length=20)
    out["ema_50"] = ta.ema(out["close"], length=50)
    out["ema_200"] = ta.ema(out["close"], length=200)
    out["rsi_14"] = ta.rsi(out["close"], length=14)
    out["atr_14"] = ta.atr(out["high"], out["low"], out["close"], length=14)

    macd = ta.macd(out["close"])
    if macd is not None:
        out["macd"] = macd.iloc[:, 0]
        out["macd_signal"] = macd.iloc[:, 1]
        out["macd_hist"] = macd.iloc[:, 2]

    bb = ta.bbands(out["close"], length=20, std=2)
    if bb is not None:
        out["bb_lower"] = bb.iloc[:, 0]
        out["bb_mid"] = bb.iloc[:, 1]
        out["bb_upper"] = bb.iloc[:, 2]

    return out
