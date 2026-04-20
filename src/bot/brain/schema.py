from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, Field, field_validator

Action = Literal["buy", "sell", "hold", "close"]
EntryType = Literal["market", "limit"]


class Decision(BaseModel):
    action: Action
    symbol: str
    size_pct_equity: float = Field(default=0.0, ge=0.0, le=100.0)
    entry_type: EntryType = "market"
    limit_price: float | None = None
    stop_loss: float | None = None
    take_profit: list[float] = Field(default_factory=list)
    leverage: int | None = Field(default=None, ge=1, le=125)
    confidence: float = Field(default=0.0, ge=0.0, le=1.0)
    rationale: str = ""
    ttl_seconds: int = Field(default=900, ge=60, le=86400)

    @field_validator("take_profit", mode="before")
    @classmethod
    def _normalize_tp(cls, v):
        if v is None:
            return []
        if isinstance(v, (int, float)):
            return [float(v)]
        return v
