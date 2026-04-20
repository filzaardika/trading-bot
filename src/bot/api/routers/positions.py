from __future__ import annotations

import time

from fastapi import APIRouter, Depends, HTTPException

from bot.api.auth import require_bearer
from bot.api.models import ControlResp, PositionDTO
from bot.storage.db import connect

router = APIRouter()


def _latest_positions_from_log(limit: int = 20) -> list[dict]:
    """Get the most recent row per symbol from positions_log."""
    with connect() as c:
        rows = c.execute(
            """
            SELECT p.* FROM positions_log p
            JOIN (
                SELECT symbol, MAX(ts) AS mts FROM positions_log GROUP BY symbol
            ) m ON p.symbol = m.symbol AND p.ts = m.mts
            WHERE p.amount > 0
            ORDER BY p.ts DESC
            LIMIT ?
            """,
            (limit,),
        ).fetchall()
    out: list[dict] = []
    for r in rows:
        entry = r["entry"] or 0.0
        mark = r["mark"] or entry
        amount = r["amount"] or 0.0
        lev = r["leverage"] or 1.0
        side = (r["side"] or "long").lower()
        direction = 1 if side == "long" else -1
        upnl = direction * (mark - entry) * amount
        upnl_pct = direction * ((mark - entry) / entry * 100.0 * lev) if entry else 0.0
        out.append(
            {
                "id": f"{r['symbol']}:{r['ts']}",
                "symbol": r["symbol"],
                "side": side,
                "size": amount,
                "entry": entry,
                "mark": mark,
                "leverage": lev,
                "unrealized_pnl": round(upnl, 2),
                "unrealized_pnl_pct": round(upnl_pct, 3),
                "stop_loss": None,
                "take_profit": None,
                "opened_at": int(r["ts"]),
            }
        )
    return out


@router.get("/positions", response_model=list[PositionDTO], dependencies=[Depends(require_bearer)])
def positions() -> list[dict]:
    return _latest_positions_from_log()


@router.post(
    "/positions/{pos_id}/close",
    response_model=ControlResp,
    dependencies=[Depends(require_bearer)],
)
def close_position(pos_id: str) -> dict:
    from bot.api.state import STATE

    STATE.push_log(f"[api] close position requested: {pos_id}")
    # Flag is consumed by bot main loop which performs the actual close via ccxt.
    # For MVP we just record intent; full-flatten endpoint is more useful.
    raise HTTPException(status_code=501, detail="per-position close not implemented in MVP; use /control/flatten")
