from __future__ import annotations

from typing import Any, Literal

from bot.exchange.client import BybitClient
from bot.utils.logger import logger
from bot.utils.retry import default_retry

Side = Literal["buy", "sell"]


class OrderManager:
    """Place/cancel orders, SL/TP via Bybit stop/take-profit orders."""

    def __init__(self, client: BybitClient) -> None:
        self.client = client

    @default_retry
    def set_leverage(self, symbol: str, leverage: int) -> None:
        try:
            self.client.raw.set_leverage(leverage, symbol)
            logger.info(f"Leverage set {symbol} x{leverage}")
        except Exception as e:
            # Bybit throws if already same leverage; ignore that
            if "leverage not modified" in str(e).lower():
                return
            raise

    @default_retry
    def set_margin_mode(self, symbol: str, mode: str = "isolated") -> None:
        try:
            self.client.raw.set_margin_mode(mode, symbol)
        except Exception as e:
            if "not modified" in str(e).lower():
                return
            logger.warning(f"set_margin_mode {symbol} {mode} failed: {e}")

    @default_retry
    def market_order(
        self,
        symbol: str,
        side: Side,
        amount: float,
        stop_loss: float | None = None,
        take_profit: float | None = None,
        reduce_only: bool = False,
        client_order_id: str | None = None,
    ) -> dict[str, Any]:
        params: dict[str, Any] = {}
        if stop_loss is not None:
            params["stopLoss"] = stop_loss
        if take_profit is not None:
            params["takeProfit"] = take_profit
        if reduce_only:
            params["reduceOnly"] = True
        if client_order_id:
            params["clientOrderId"] = client_order_id
        logger.info(
            f"ORDER market {side} {symbol} amt={amount} SL={stop_loss} TP={take_profit} "
            f"reduce={reduce_only} cid={client_order_id}"
        )
        return self.client.raw.create_order(symbol, "market", side, amount, None, params)

    @default_retry
    def limit_order(
        self,
        symbol: str,
        side: Side,
        amount: float,
        price: float,
        stop_loss: float | None = None,
        take_profit: float | None = None,
        client_order_id: str | None = None,
        time_in_force: str = "GTC",
    ) -> dict[str, Any]:
        params: dict[str, Any] = {"timeInForce": time_in_force}
        if stop_loss is not None:
            params["stopLoss"] = stop_loss
        if take_profit is not None:
            params["takeProfit"] = take_profit
        if client_order_id:
            params["clientOrderId"] = client_order_id
        logger.info(
            f"ORDER limit {side} {symbol} amt={amount} px={price} SL={stop_loss} TP={take_profit}"
        )
        return self.client.raw.create_order(symbol, "limit", side, amount, price, params)

    @default_retry
    def close_position(self, symbol: str, side: Side, amount: float) -> dict[str, Any]:
        # side = opposite of the open position side
        return self.market_order(symbol, side, amount, reduce_only=True)

    @default_retry
    def cancel_all(self, symbol: str) -> Any:
        return self.client.raw.cancel_all_orders(symbol)

    @default_retry
    def fetch_open_orders(self, symbol: str | None = None) -> list[dict[str, Any]]:
        return self.client.raw.fetch_open_orders(symbol)
