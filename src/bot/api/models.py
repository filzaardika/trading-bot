"""Pydantic DTOs shared by routers."""
from __future__ import annotations

from typing import Literal, Optional

from pydantic import BaseModel


class HealthResp(BaseModel):
    ok: bool = True
    version: str = "0.1.0"


class PairReq(BaseModel):
    token: str


class PairResp(BaseModel):
    ok: bool
    bot_name: str = "trading-bot"
    mode: str = "paper"
    testnet: bool = True


class DashboardResp(BaseModel):
    equity: float
    starting_equity: float
    pnl_today: float
    pnl_today_pct: float
    open_positions_count: int
    mode: Literal["paper", "live"]
    testnet: bool
    bot_status: Literal["running", "paused", "stopped", "error"]
    kill_switch: bool
    cycle_progress: float
    seconds_to_next_cycle: int
    last_cycle_ts: int
    last_cycle_error: str


class PositionDTO(BaseModel):
    id: str
    symbol: str
    side: Literal["long", "short"]
    size: float
    entry: float
    mark: float
    leverage: float
    unrealized_pnl: float
    unrealized_pnl_pct: float
    stop_loss: Optional[float] = None
    take_profit: Optional[float] = None
    opened_at: int


class TradeDTO(BaseModel):
    id: int
    signal_id: Optional[str]
    order_id: Optional[str]
    ts: int
    symbol: str
    side: str
    amount: float
    price: Optional[float]
    status: Optional[str]
    pnl_usdt: Optional[float]


class SignalDTO(BaseModel):
    signal_id: str
    ts: int
    symbol: str
    action: str
    size_pct_equity: Optional[float]
    entry_type: Optional[str]
    limit_price: Optional[float]
    stop_loss: Optional[float]
    take_profit: Optional[str]
    confidence: Optional[float]
    rationale: Optional[str]


class NewsItem(BaseModel):
    id: int
    source: str
    url: Optional[str]
    title: str
    published_at: Optional[int]
    sentiment: Optional[float]


class SettingsDTO(BaseModel):
    mode: str
    testnet: bool
    universe_size: int
    cycle_seconds: int
    max_risk_pct_per_trade: float
    max_concurrent_positions: int
    max_leverage: int
    daily_loss_halt_pct: float
    max_drawdown_kill_pct: float


class ControlResp(BaseModel):
    ok: bool
    message: str
