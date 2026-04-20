from __future__ import annotations


def position_amount(
    equity_usdt: float,
    size_pct: float,
    entry_price: float,
    stop_loss: float,
    leverage: int,
) -> float:
    """
    Compute contract amount given risk%.

    size_pct = % of equity at risk (distance to SL).
    amount (base) = (equity * size_pct/100) / |entry - SL|
    Leverage enforces margin sufficiency separately.
    """
    if entry_price <= 0 or stop_loss <= 0:
        return 0.0
    risk_dist = abs(entry_price - stop_loss)
    if risk_dist <= 0:
        return 0.0
    risk_usdt = equity_usdt * (size_pct / 100.0)
    amount_base = risk_usdt / risk_dist
    # Cap by available margin given leverage:
    max_notional = equity_usdt * leverage
    max_amount_by_margin = max_notional / entry_price
    return max(0.0, min(amount_base, max_amount_by_margin))
