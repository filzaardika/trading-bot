"""Bearer token auth. Token is the API_TOKEN env var (or generated + stored in kv)."""
from __future__ import annotations

import os
import secrets

from fastapi import Header, HTTPException, status

from bot.storage.models import kv_get, kv_set


def get_or_create_token() -> str:
    token = os.environ.get("API_TOKEN")
    if token:
        return token
    existing = kv_get("api_token")
    if existing:
        return existing
    new = secrets.token_urlsafe(24)
    kv_set("api_token", new)
    return new


def require_bearer(authorization: str | None = Header(default=None)) -> None:
    expected = get_or_create_token()
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="missing bearer")
    provided = authorization.split(" ", 1)[1].strip()
    if not secrets.compare_digest(provided, expected):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="invalid token")


def require_query_token(token: str | None) -> None:
    expected = get_or_create_token()
    if not token or not secrets.compare_digest(token, expected):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="invalid token")
