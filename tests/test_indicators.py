import numpy as np
import pandas as pd

from bot.indicators.features import extract_snapshot, ohlcv_tail
from bot.indicators.ta import add_indicators, ohlcv_to_df


def _make_ohlcv(n: int = 300):
    rng = np.random.default_rng(42)
    base_ts = int(pd.Timestamp("2025-01-01", tz="UTC").timestamp() * 1000)
    closes = 100 + np.cumsum(rng.standard_normal(n))
    rows = []
    for i in range(n):
        c = closes[i]
        rows.append([base_ts + i * 3_600_000, c, c + 0.5, c - 0.5, c + 0.1, 1000 + i])
    return rows


def test_ohlcv_to_df_shape():
    df = ohlcv_to_df(_make_ohlcv(50))
    assert len(df) == 50
    assert {"open", "high", "low", "close", "volume"}.issubset(df.columns)


def test_add_indicators_produces_expected_columns():
    df = add_indicators(ohlcv_to_df(_make_ohlcv(300)))
    for col in ("ema_20", "ema_50", "rsi_14", "atr_14", "macd", "bb_upper"):
        assert col in df.columns


def test_extract_snapshot_non_null_latest():
    df = add_indicators(ohlcv_to_df(_make_ohlcv(300)))
    snap = extract_snapshot(df)
    assert snap["close"] is not None
    assert snap["rsi_14"] is not None


def test_ohlcv_tail_len():
    df = add_indicators(ohlcv_to_df(_make_ohlcv(100)))
    assert len(ohlcv_tail(df, 20)) == 20
