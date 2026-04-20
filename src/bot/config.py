from __future__ import annotations

from pathlib import Path
from typing import Literal

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env", env_file_encoding="utf-8", extra="ignore"
    )

    # Bybit
    bybit_api_key: str = ""
    bybit_api_secret: str = ""
    bybit_testnet: bool = True

    # Trading
    quote_currency: str = "USDT"
    universe_size: int = 50
    timeframe: str = "1h"
    cycle_seconds: int = 300

    # Risk
    max_risk_pct_per_trade: float = 2.0
    max_concurrent_positions: int = 5
    max_leverage: int = 3
    daily_loss_halt_pct: float = 5.0
    max_drawdown_kill_pct: float = 15.0
    sl_cooldown_hours: int = 4
    funding_rate_gate: float = 0.001
    live_size_cap_pct: float = 0.25

    # Mode
    mode: Literal["paper", "live"] = "paper"

    # Claude
    gh_copilot_bin: str = "gh"
    claude_model: str = "claude-opus-4.7"
    claude_timeout_seconds: int = 60

    # News
    newsapi_key: str = ""
    cryptopanic_token: str = ""
    news_cache_minutes: int = 15

    # Telegram
    telegram_bot_token: str = ""
    telegram_chat_id: str = ""

    # Storage
    db_path: Path = Field(default=Path("./data/bot.sqlite"))

    # Logging
    log_level: str = "INFO"
    log_dir: Path = Field(default=Path("./logs"))


settings = Settings()
