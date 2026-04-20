from __future__ import annotations

from fastapi import APIRouter, Depends

from bot.api.auth import require_bearer
from bot.api.models import SettingsDTO
from bot.config import settings

router = APIRouter()


@router.get("/settings", response_model=SettingsDTO, dependencies=[Depends(require_bearer)])
def get_settings() -> dict:
    return {
        "mode": settings.mode,
        "testnet": settings.bybit_testnet,
        "universe_size": settings.universe_size,
        "cycle_seconds": settings.cycle_seconds,
        "max_risk_pct_per_trade": settings.max_risk_pct_per_trade,
        "max_concurrent_positions": settings.max_concurrent_positions,
        "max_leverage": settings.max_leverage,
        "daily_loss_halt_pct": settings.daily_loss_halt_pct,
        "max_drawdown_kill_pct": settings.max_drawdown_kill_pct,
    }
