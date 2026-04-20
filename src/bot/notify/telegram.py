from __future__ import annotations

import asyncio

import httpx

from bot.config import settings
from bot.utils.logger import logger


def send(text: str) -> None:
    if not settings.telegram_bot_token or not settings.telegram_chat_id:
        return
    url = f"https://api.telegram.org/bot{settings.telegram_bot_token}/sendMessage"
    try:
        r = httpx.post(
            url,
            data={
                "chat_id": settings.telegram_chat_id,
                "text": text[:4000],
                "parse_mode": "HTML",
                "disable_web_page_preview": "true",
            },
            timeout=10,
        )
        if r.status_code >= 300:
            logger.warning(f"telegram send failed {r.status_code}: {r.text[:200]}")
    except Exception as e:
        logger.warning(f"telegram exception: {e}")


async def send_async(text: str) -> None:
    await asyncio.to_thread(send, text)
