from __future__ import annotations

from dataclasses import dataclass

from bot.config import settings
from bot.exchange.client import BybitClient, Ticker
from bot.utils.logger import logger


STABLECOIN_BASES = {"USDT", "USDC", "DAI", "TUSD", "FDUSD", "BUSD", "USDE", "PYUSD"}


@dataclass
class Candidate:
    symbol: str
    quote_volume_24h: float
    spread_bps: float
    last: float


def screen_universe(client: BybitClient, size: int | None = None) -> list[Candidate]:
    size = size or settings.universe_size
    tickers = client.fetch_tickers()
    cands: list[Candidate] = []
    for sym, t in tickers.items():
        base = sym.split("/")[0]
        if base in STABLECOIN_BASES:
            continue
        if t.quote_volume_24h <= 0 or t.last <= 0:
            continue
        cands.append(Candidate(sym, t.quote_volume_24h, t.spread_bps, t.last))
    cands.sort(key=lambda c: c.quote_volume_24h, reverse=True)
    top = cands[:size]
    logger.info(f"Screener: {len(top)} candidates (top by 24h volume)")
    return top
