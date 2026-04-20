from bot.risk.sizing import position_amount


def test_position_amount_basic():
    # equity 10000, 2% risk, entry 100, SL 95 -> risk_usdt=200, risk_dist=5 -> amt=40
    amt = position_amount(equity_usdt=10_000, size_pct=2.0, entry_price=100, stop_loss=95, leverage=3)
    # capped by margin: max_notional=30000, max_amt=300. 40 < 300 -> 40
    assert round(amt, 4) == 40.0


def test_position_amount_zero_when_no_sl_distance():
    assert position_amount(10_000, 2.0, 100, 100, 3) == 0.0


def test_position_amount_margin_cap():
    # tiny SL distance -> huge amount; margin cap activates
    amt = position_amount(1000, 2.0, 100, 99.99, 2)
    # unrestricted: 20 / 0.01 = 2000; margin cap: 1000*2/100 = 20
    assert amt == 20.0
