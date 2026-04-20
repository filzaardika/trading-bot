"""Uvicorn entry point. Run with: trading-bot-api"""
from __future__ import annotations

import os

import uvicorn

from bot.api.auth import get_or_create_token
from bot.utils.logger import logger, setup_logging


def main() -> None:
    setup_logging()
    token = get_or_create_token()
    host = os.environ.get("API_HOST", "0.0.0.0")
    port = int(os.environ.get("API_PORT", "8080"))
    logger.info(f"API starting on http://{host}:{port}")
    logger.info(f"Bearer token: {token}")
    logger.info("(Store this token; pair your Android app with it.)")
    uvicorn.run("bot.api.app:app", host=host, port=port, log_level="info")


if __name__ == "__main__":
    main()
