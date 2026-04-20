from __future__ import annotations

from fastapi import APIRouter, Depends, Query

from bot.api.auth import require_bearer
from bot.api.models import SignalDTO
from bot.storage.db import connect

router = APIRouter()


@router.get("/signals", response_model=list[SignalDTO], dependencies=[Depends(require_bearer)])
def signals(limit: int = Query(default=100, ge=1, le=500)) -> list[dict]:
    with connect() as c:
        rows = c.execute(
            "SELECT signal_id, ts, symbol, action, size_pct_equity, entry_type, limit_price, "
            "stop_loss, take_profit, confidence, rationale FROM signals "
            "ORDER BY ts DESC LIMIT ?",
            (limit,),
        ).fetchall()
    return [dict(r) for r in rows]
