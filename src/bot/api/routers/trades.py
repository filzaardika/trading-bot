from __future__ import annotations

from fastapi import APIRouter, Depends, Query

from bot.api.auth import require_bearer
from bot.api.models import TradeDTO
from bot.storage.db import connect

router = APIRouter()


@router.get("/trades", response_model=list[TradeDTO], dependencies=[Depends(require_bearer)])
def trades(limit: int = Query(default=100, ge=1, le=500)) -> list[dict]:
    with connect() as c:
        rows = c.execute(
            "SELECT id, signal_id, order_id, ts, symbol, side, amount, price, status, pnl_usdt "
            "FROM trades ORDER BY ts DESC LIMIT ?",
            (limit,),
        ).fetchall()
    return [dict(r) for r in rows]
