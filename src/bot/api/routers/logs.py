from __future__ import annotations

from fastapi import APIRouter, Depends, Query

from bot.api.auth import require_bearer
from bot.api.state import STATE

router = APIRouter()


@router.get("/logs/tail", dependencies=[Depends(require_bearer)])
def tail(limit: int = Query(default=200, ge=1, le=500)) -> dict:
    return {"lines": STATE.snapshot_logs(limit)}
