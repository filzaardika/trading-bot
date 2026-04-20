from __future__ import annotations

import json
import re
import subprocess
import uuid
from typing import Any

from pydantic import ValidationError

from bot.brain.prompts import SYSTEM_PROMPT, build_user_prompt
from bot.brain.schema import Decision
from bot.config import settings
from bot.utils.logger import logger


class BrainError(Exception):
    pass


def _extract_json(text: str) -> dict[str, Any] | None:
    text = text.strip()
    # direct
    try:
        return json.loads(text)
    except Exception:
        pass
    # fenced
    m = re.search(r"```(?:json)?\s*(\{.*?\})\s*```", text, re.DOTALL)
    if m:
        try:
            return json.loads(m.group(1))
        except Exception:
            pass
    # first { ... last }
    i, j = text.find("{"), text.rfind("}")
    if i >= 0 and j > i:
        try:
            return json.loads(text[i : j + 1])
        except Exception:
            return None
    return None


def _run_gh_copilot(system: str, user: str) -> str:
    """
    Invoke gh copilot CLI as a subprocess. Uses `gh copilot suggest -t shell` is not
    appropriate; instead we pipe the prompt via stdin to `gh copilot explain` which
    accepts free-form input. If unavailable, fall back to env-provided mock.

    We concatenate system+user into one prompt string for CLI context.
    """
    prompt = f"{system}\n\n---\n\n{user}"
    cmd = [settings.gh_copilot_bin, "copilot", "explain", "--", prompt]
    try:
        proc = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=settings.claude_timeout_seconds,
            check=False,
        )
    except FileNotFoundError as e:
        raise BrainError(f"gh binary not found: {e}") from e
    except subprocess.TimeoutExpired as e:
        raise BrainError("gh copilot timeout") from e
    if proc.returncode != 0:
        raise BrainError(f"gh copilot exit {proc.returncode}: {proc.stderr[:500]}")
    return proc.stdout


def decide(payload: dict[str, Any]) -> tuple[Decision | None, str]:
    """Ask Claude via gh copilot. Returns (Decision|None, raw_text)."""
    user_prompt = build_user_prompt(payload)
    try:
        raw = _run_gh_copilot(SYSTEM_PROMPT, user_prompt)
    except BrainError as e:
        logger.error(f"brain call fail: {e}")
        return None, str(e)

    data = _extract_json(raw)
    if data is None:
        logger.warning(f"brain response not JSON-parseable; raw[:300]={raw[:300]!r}")
        return None, raw

    # Inject symbol guard
    data.setdefault("symbol", payload.get("symbol"))
    try:
        decision = Decision.model_validate(data)
    except ValidationError as e:
        logger.warning(f"brain schema invalid: {e}")
        return None, raw
    return decision, raw


def new_signal_id(symbol: str) -> str:
    return f"{symbol.replace('/', '').replace(':','').lower()}-{uuid.uuid4().hex[:12]}"


def decision_to_signal(d: Decision, signal_id: str) -> dict[str, Any]:
    return {
        "signal_id": signal_id,
        "symbol": d.symbol,
        "action": d.action,
        "size_pct_equity": d.size_pct_equity,
        "entry_type": d.entry_type,
        "limit_price": d.limit_price,
        "stop_loss": d.stop_loss,
        "take_profit": d.take_profit,
        "leverage": d.leverage,
        "confidence": d.confidence,
        "rationale": d.rationale,
        "ttl_seconds": d.ttl_seconds,
    }
