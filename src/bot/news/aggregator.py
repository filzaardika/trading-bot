from __future__ import annotations

import time

from bot.news.rss import NewsItem, fetch_rss
from bot.news.newsapi import fetch_newsapi
from bot.news.cryptopanic import fetch_cryptopanic
from bot.storage.db import connect
from bot.utils.logger import logger


def _dedupe(items: list[NewsItem]) -> list[NewsItem]:
    seen: set[str] = set()
    out: list[NewsItem] = []
    for it in items:
        key = (it.url or it.title).strip().lower()
        if not key or key in seen:
            continue
        seen.add(key)
        out.append(it)
    return out


def fetch_all() -> list[NewsItem]:
    items = fetch_rss() + fetch_newsapi() + fetch_cryptopanic()
    items = _dedupe(items)
    items.sort(key=lambda x: x.published_at, reverse=True)
    logger.info(f"News aggregate: {len(items)} items")
    return items


def cache_news(items: list[NewsItem]) -> None:
    now = int(time.time())
    with connect() as c:
        for it in items:
            try:
                c.execute(
                    """INSERT OR IGNORE INTO news_cache
                       (fetched_at, source, url, title, published_at, body, sentiment)
                       VALUES (?,?,?,?,?,?,?)""",
                    (
                        now,
                        it.source,
                        it.url or None,
                        it.title,
                        it.published_at,
                        it.body,
                        it.sentiment,
                    ),
                )
            except Exception as e:
                logger.debug(f"cache news fail: {e}")


def top_news_for_prompt(items: list[NewsItem], symbol_base: str, n: int = 10) -> list[dict]:
    """Return top-N news relevant to symbol (base coin ticker) for Claude prompt."""
    sym_l = symbol_base.lower()
    ranked: list[tuple[float, NewsItem]] = []
    for it in items:
        score = 0.0
        hay = (it.title + " " + it.body).lower()
        if sym_l in hay:
            score += 2.0
        # Generic market movers
        for kw in ("sec", "fed", "etf", "hack", "exploit", "regulation"):
            if kw in hay:
                score += 0.5
        # Recency bonus
        age_h = max(1, (int(time.time()) - (it.published_at or 0)) / 3600)
        score += 1.0 / age_h
        ranked.append((score, it))
    ranked.sort(key=lambda x: x[0], reverse=True)
    top = [it for _, it in ranked[:n]]
    return [
        {
            "ts": it.published_at,
            "source": it.source,
            "title": it.title,
            "sentiment": it.sentiment,
        }
        for it in top
    ]
