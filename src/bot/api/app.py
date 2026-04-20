"""FastAPI application factory."""
from __future__ import annotations

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from bot.api.auth import get_or_create_token
from bot.api.models import HealthResp, PairReq, PairResp
from bot.api.routers import (
    control,
    dashboard,
    logs,
    news,
    positions,
    settings as settings_router,
    signals,
    trades,
    ws,
)
from bot.config import settings as bot_settings
from bot.storage.db import init_db


def create_app() -> FastAPI:
    init_db()
    # Ensure a token exists on startup so operator can retrieve it from kv
    get_or_create_token()

    app = FastAPI(title="Trading Bot API", version="0.1.0")

    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=False,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    @app.get("/health", response_model=HealthResp)
    def health() -> dict:
        return {"ok": True, "version": "0.1.0"}

    @app.post("/auth/pair", response_model=PairResp)
    def pair(req: PairReq) -> dict:
        expected = get_or_create_token()
        import secrets as _s

        if not _s.compare_digest(req.token, expected):
            from fastapi import HTTPException

            raise HTTPException(status_code=401, detail="invalid token")
        return {
            "ok": True,
            "bot_name": "trading-bot",
            "mode": bot_settings.mode,
            "testnet": bot_settings.bybit_testnet,
        }

    app.include_router(dashboard.router)
    app.include_router(positions.router)
    app.include_router(trades.router)
    app.include_router(signals.router)
    app.include_router(news.router)
    app.include_router(control.router)
    app.include_router(settings_router.router)
    app.include_router(logs.router)
    app.include_router(ws.router)

    return app


app = create_app()
