from __future__ import annotations

from fastapi import APIRouter, Depends

from bot.api.auth import require_bearer
from bot.api.models import ControlResp
from bot.api.state import STATE
from bot.storage.models import kv_set

router = APIRouter()


@router.post("/control/pause", response_model=ControlResp, dependencies=[Depends(require_bearer)])
def pause() -> dict:
    STATE.paused = True
    STATE.push_log("[api] bot paused")
    return {"ok": True, "message": "paused"}


@router.post("/control/resume", response_model=ControlResp, dependencies=[Depends(require_bearer)])
def resume() -> dict:
    STATE.paused = False
    STATE.push_log("[api] bot resumed")
    return {"ok": True, "message": "resumed"}


@router.post("/control/cycle-now", response_model=ControlResp, dependencies=[Depends(require_bearer)])
def cycle_now() -> dict:
    STATE.force_cycle_request = True
    STATE.push_log("[api] force cycle requested")
    return {"ok": True, "message": "cycle requested"}


@router.post("/control/flatten", response_model=ControlResp, dependencies=[Depends(require_bearer)])
def flatten() -> dict:
    STATE.flatten_request = True
    STATE.push_log("[api] FLATTEN ALL requested")
    return {"ok": True, "message": "flatten requested; bot will close all positions on next tick"}


@router.post("/control/kill", response_model=ControlResp, dependencies=[Depends(require_bearer)])
def kill() -> dict:
    kv_set("kill_switch", "1")
    STATE.push_log("[api] KILL SWITCH activated")
    return {"ok": True, "message": "kill switch active"}


@router.post("/control/kill/reset", response_model=ControlResp, dependencies=[Depends(require_bearer)])
def kill_reset() -> dict:
    kv_set("kill_switch", "0")
    STATE.push_log("[api] kill switch reset")
    return {"ok": True, "message": "kill switch reset"}
