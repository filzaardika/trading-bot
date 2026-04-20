from __future__ import annotations

from dataclasses import dataclass

from bot.exchange.client import BybitClient
from bot.screener.universe import Candidate
from bot.utils.logger import logger


@dataclass
class FilterConfig:
    min_quote_volume_24h: float = 5_000_000.0  # USDT
    max_spread_bps: float = 5.0
    max_funding_abs: float = 0.001  # 0.1%


def apply_filters(
    client: BybitClient,
    candidates: list[Candidate],
    cfg: FilterConfig | None = None,
) -> list[Candidate]:
    cfg = cfg or FilterConfig()
    out: list[Candidate] = []
    for c in candidates:
        if c.quote_volume_24h < cfg.min_quote_volume_24h:
            continue
        if c.spread_bps > cfg.max_spread_bps:
            continue
        try:
            fr = client.fetch_funding_rate(c.symbol)
        except Exception as e:
            logger.warning(f"funding fetch {c.symbol} fail: {e}")
            continue
        if abs(fr) > cfg.max_funding_abs:
            continue
        out.append(c)
    logger.info(f"Filters: {len(out)}/{len(candidates)} passed")
    return out
