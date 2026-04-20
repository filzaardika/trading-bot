from bot.screener.universe import STABLECOIN_BASES


def test_stablecoin_list():
    for s in ("USDT", "USDC", "DAI"):
        assert s in STABLECOIN_BASES
