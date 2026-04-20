from __future__ import annotations

import time
from dataclasses import dataclass, field
from typing import Any

import feedparser

from bot.utils.logger import logger


RSS_FEEDS = [
    ("CoinDesk", "https://www.coindesk.com/arc/outboundfeeds/rss/"),
    ("CoinTelegraph", "https://cointelegraph.com/rss"),
    ("Decrypt", "https://decrypt.co/feed"),
]


@dataclass
class NewsItem:
    source: str
    title: str
    url: str
    published_at: int
    body: str = ""
    sentiment: float | None = None
    meta: dict[str, Any] = field(default_factory=dict)


def fetch_rss(limit_per_feed: int = 20) -> list[NewsItem]:
    items: list[NewsItem] = []
    for src, url in RSS_FEEDS:
        try:
            feed = feedparser.parse(url)
            for e in feed.entries[:limit_per_feed]:
                published = 0
                if getattr(e, "published_parsed", None):
                    published = int(time.mktime(e.published_parsed))
                items.append(
                    NewsItem(
                        source=src,
                        title=e.get("title", ""),
                        url=e.get("link", ""),
                        published_at=published,
                        body=e.get("summary", "")[:500],
                    )
                )
        except Exception as ex:
            logger.warning(f"RSS {src} fail: {ex}")
    return items
