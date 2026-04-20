from __future__ import annotations

import asyncio

from fastapi import APIRouter, Query, WebSocket, WebSocketDisconnect

from bot.api.auth import require_query_token
from bot.api.snapshot import build_dashboard
from bot.api.routers.positions import _latest_positions_from_log

router = APIRouter()


@router.websocket("/ws")
async def ws(websocket: WebSocket, token: str = Query(default=None)):
    try:
        require_query_token(token)
    except Exception:
        await websocket.close(code=4401)
        return
    await websocket.accept()
    try:
        while True:
            payload = {
                "type": "snapshot",
                "dashboard": build_dashboard(),
                "positions": _latest_positions_from_log(),
            }
            await websocket.send_json(payload)
            await asyncio.sleep(2.0)
    except WebSocketDisconnect:
        return
    except Exception:
        return
