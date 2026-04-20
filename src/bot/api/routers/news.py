from __future__ import annotations

from fastapi import APIRouter, Depends, Query

from bot.api.auth import require_bearer
from bot.api.models import NewsItem
from bot.storage.db import connect

router = APIRouter()


@router.get("/news", response_model=list[NewsItem], dependencies=[Depends(require_bearer)])
def news(limit: int = Query(default=50, ge=1, le=200)) -> list[dict]:
    with connect() as c:
        rows = c.execute(
            "SELECT id, source, url, title, published_at, sentiment FROM news_cache "
            "ORDER BY COALESCE(published_at, fetched_at) DESC LIMIT ?",
            (limit,),
        ).fetchall()
    return [dict(r) for r in rows]
