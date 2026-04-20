from __future__ import annotations

import time

import httpx

from bot.config import settings
from bot.news.rss import NewsItem
from bot.utils.logger import logger


def fetch_newsapi(query: str = "crypto OR bitcoin OR ethereum", page_size: int = 30) -> list[NewsItem]:
    if not settings.newsapi_key:
        return []
    url = "https://newsapi.org/v2/everything"
    params = {
        "q": query,
        "sortBy": "publishedAt",
        "pageSize": page_size,
        "language": "en",
        "apiKey": settings.newsapi_key,
    }
    try:
        r = httpx.get(url, params=params, timeout=10)
        r.raise_for_status()
        data = r.json()
    except Exception as e:
        logger.warning(f"NewsAPI fail: {e}")
        return []
    out: list[NewsItem] = []
    for a in data.get("articles", []):
        ts = 0
        if a.get("publishedAt"):
            try:
                ts = int(time.mktime(time.strptime(a["publishedAt"], "%Y-%m-%dT%H:%M:%SZ")))
            except Exception:
                ts = 0
        out.append(
            NewsItem(
                source=f"NewsAPI/{a.get('source', {}).get('name', '?')}",
                title=a.get("title") or "",
                url=a.get("url") or "",
                published_at=ts,
                body=(a.get("description") or "")[:500],
            )
        )
    return out
