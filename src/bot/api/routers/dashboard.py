from __future__ import annotations

from fastapi import APIRouter, Depends

from bot.api.auth import require_bearer
from bot.api.models import DashboardResp
from bot.api.snapshot import build_dashboard

router = APIRouter()


@router.get("/dashboard", response_model=DashboardResp, dependencies=[Depends(require_bearer)])
def dashboard() -> dict:
    return build_dashboard()
