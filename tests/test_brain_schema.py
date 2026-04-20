import pytest
from pydantic import ValidationError

from bot.brain.claude import _extract_json
from bot.brain.schema import Decision


def test_decision_valid():
    d = Decision.model_validate(
        {
            "action": "buy",
            "symbol": "BTC/USDT:USDT",
            "size_pct_equity": 1.5,
            "entry_type": "market",
            "stop_loss": 61200.0,
            "take_profit": [63500.0, 64800.0],
            "leverage": 3,
            "confidence": 0.7,
            "rationale": "test",
            "ttl_seconds": 900,
        }
    )
    assert d.action == "buy"
    assert d.take_profit == [63500.0, 64800.0]


def test_decision_rejects_bad_size():
    with pytest.raises(ValidationError):
        Decision.model_validate({"action": "buy", "symbol": "X", "size_pct_equity": 200})


def test_decision_tp_scalar_coerced():
    d = Decision.model_validate(
        {"action": "sell", "symbol": "X", "size_pct_equity": 1.0, "take_profit": 100.0}
    )
    assert d.take_profit == [100.0]


def test_extract_json_plain():
    assert _extract_json('{"a": 1}') == {"a": 1}


def test_extract_json_fenced():
    assert _extract_json('```json\n{"a": 2}\n```') == {"a": 2}


def test_extract_json_with_prose():
    assert _extract_json('Here is the decision:\n{"a": 3}\nDone.') == {"a": 3}


def test_extract_json_garbage():
    assert _extract_json("no json here") is None
