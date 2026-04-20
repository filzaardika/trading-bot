from __future__ import annotations

from dataclasses import dataclass
from typing import Any

import ccxt

from bot.config import settings
from bot.utils.logger import logger
from bot.utils.retry import default_retry


@dataclass
class Ticker:
    symbol: str
    last: float
    bid: float
    ask: float
    spread_bps: float
    quote_volume_24h: float


class BybitClient:
    """ccxt wrapper for Bybit USDT Perpetual (linear swap)."""

    def __init__(self) -> None:
        self._exchange = ccxt.bybit(
            {
                "apiKey": settings.bybit_api_key,
                "secret": settings.bybit_api_secret,
                "enableRateLimit": True,
                "options": {
                    "defaultType": "swap",
                    "defaultSubType": "linear",
                    "recvWindow": 10_000,
                },
            }
        )
        if settings.bybit_testnet:
            self._exchange.set_sandbox_mode(True)
            logger.warning("Bybit TESTNET mode enabled")
        self._markets_loaded = False

    @property
    def raw(self) -> ccxt.bybit:
        return self._exchange

    @default_retry
    def load_markets(self, reload: bool = False) -> dict[str, Any]:
        markets = self._exchange.load_markets(reload=reload)
        self._markets_loaded = True
        return markets

    def ensure_markets(self) -> None:
        if not self._markets_loaded:
            self.load_markets()

    @default_retry
    def fetch_tickers(self) -> dict[str, Ticker]:
        self.ensure_markets()
        raw = self._exchange.fetch_tickers()
        out: dict[str, Ticker] = {}
        for sym, t in raw.items():
            market = self._exchange.markets.get(sym)
            if not market or not market.get("swap") or not market.get("linear"):
                continue
            if market.get("quote") != settings.quote_currency:
                continue
            bid = float(t.get("bid") or 0.0)
            ask = float(t.get("ask") or 0.0)
            last = float(t.get("last") or 0.0)
            spread_bps = ((ask - bid) / ask * 10_000) if ask > 0 else 0.0
            qv = float(t.get("quoteVolume") or 0.0)
            out[sym] = Ticker(sym, last, bid, ask, spread_bps, qv)
        return out

    @default_retry
    def fetch_ohlcv(
        self, symbol: str, timeframe: str = "1h", limit: int = 200
    ) -> list[list[float]]:
        self.ensure_markets()
        return self._exchange.fetch_ohlcv(symbol, timeframe=timeframe, limit=limit)

    @default_retry
    def fetch_order_book(self, symbol: str, limit: int = 25) -> dict[str, Any]:
        self.ensure_markets()
        return self._exchange.fetch_order_book(symbol, limit=limit)

    @default_retry
    def fetch_funding_rate(self, symbol: str) -> float:
        self.ensure_markets()
        fr = self._exchange.fetch_funding_rate(symbol)
        return float(fr.get("fundingRate") or 0.0)

    @default_retry
    def fetch_balance(self) -> dict[str, Any]:
        return self._exchange.fetch_balance({"type": "swap"})

    @default_retry
    def fetch_positions(self, symbols: list[str] | None = None) -> list[dict[str, Any]]:
        return self._exchange.fetch_positions(symbols=symbols)

    def equity_usdt(self) -> float:
        bal = self.fetch_balance()
        total = bal.get("total", {}).get(settings.quote_currency, 0.0)
        return float(total or 0.0)
