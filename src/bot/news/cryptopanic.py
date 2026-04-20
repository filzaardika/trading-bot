from __future__ import annotations

import time

import httpx

from bot.config import settings
from bot.news.rss import NewsItem
from bot.utils.logger import logger


def fetch_cryptopanic(limit: int = 50) -> list[NewsItem]:
    if not settings.cryptopanic_token:
        return []
    url = "https://cryptopanic.com/api/v1/posts/"
    params = {"auth_token": settings.cryptopanic_token, "public": "true", "kind": "news"}
    try:
        r = httpx.get(url, params=params, timeout=10)
        r.raise_for_status()
        data = r.json()
    except Exception as e:
        logger.warning(f"CryptoPanic fail: {e}")
        return []
    out: list[NewsItem] = []
    for p in (data.get("results") or [])[:limit]:
        ts = 0
        if p.get("published_at"):
            try:
                ts = int(time.mktime(time.strptime(p["published_at"][:19], "%Y-%m-%dT%H:%M:%S")))
            except Exception:
                ts = 0
        votes = p.get("votes") or {}
        pos, neg = int(votes.get("positive") or 0), int(votes.get("negative") or 0)
        sent = None
        if pos + neg > 0:
            sent = (pos - neg) / (pos + neg)
        out.append(
            NewsItem(
                source=f"CryptoPanic/{p.get('source',{}).get('title','?')}",
                title=p.get("title") or "",
                url=p.get("url") or "",
                published_at=ts,
                body="",
                sentiment=sent,
                meta={"currencies": [c.get("code") for c in (p.get("currencies") or [])]},
            )
        )
    return out
