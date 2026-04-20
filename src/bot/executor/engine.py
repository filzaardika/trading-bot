from __future__ import annotations

import time
from typing import Any

from bot.brain.schema import Decision
from bot.config import settings
from bot.exchange.client import BybitClient
from bot.exchange.orders import OrderManager
from bot.notify.telegram import send as tg_send
from bot.risk.guards import RiskDecision
from bot.risk.sizing import position_amount
from bot.storage.models import save_signal, save_trade, set_sl_cooldown
from bot.utils.logger import logger


class Executor:
    def __init__(self, client: BybitClient, orders: OrderManager) -> None:
        self.client = client
        self.orders = orders

    def execute(
        self,
        decision: Decision,
        signal_id: str,
        risk: RiskDecision,
        equity: float,
        current_price: float,
        raw_response: str,
    ) -> dict[str, Any] | None:
        payload = {
            "signal_id": signal_id,
            "symbol": decision.symbol,
            "action": decision.action,
            "size_pct_equity": risk.clamped_size_pct or decision.size_pct_equity,
            "entry_type": decision.entry_type,
            "limit_price": decision.limit_price,
            "stop_loss": decision.stop_loss,
            "take_profit": decision.take_profit,
            "leverage": risk.clamped_leverage or decision.leverage,
            "confidence": decision.confidence,
            "rationale": decision.rationale,
            "ttl_seconds": decision.ttl_seconds,
        }
        save_signal(payload, raw_response)

        if decision.action == "hold":
            logger.info(f"HOLD {decision.symbol}: {decision.rationale}")
            return None

        if decision.action == "close":
            return self._close(decision, signal_id)

        if decision.stop_loss is None:
            logger.warning(f"skip {decision.symbol}: no SL provided")
            return None

        entry = decision.limit_price if decision.entry_type == "limit" and decision.limit_price else current_price
        size_pct = risk.clamped_size_pct or decision.size_pct_equity
        leverage = risk.clamped_leverage or settings.max_leverage
        amount = position_amount(equity, size_pct, entry, decision.stop_loss, leverage)
        if amount <= 0:
            logger.warning(f"skip {decision.symbol}: computed amount 0")
            return None

        # Round to exchange precision
        market = self.client.raw.market(decision.symbol)
        amount_str = self.client.raw.amount_to_precision(decision.symbol, amount)
        amount = float(amount_str)
        if amount < (market.get("limits", {}).get("amount", {}).get("min") or 0):
            logger.warning(f"skip {decision.symbol}: amount {amount} below min")
            return None

        self.orders.set_margin_mode(decision.symbol, "isolated")
        self.orders.set_leverage(decision.symbol, leverage)

        tp0 = decision.take_profit[0] if decision.take_profit else None
        side = "buy" if decision.action == "buy" else "sell"

        if settings.mode == "paper":
            logger.info(
                f"[PAPER] {side} {decision.symbol} amt={amount} px={entry} "
                f"SL={decision.stop_loss} TP={tp0} lev={leverage}"
            )
            tg_send(
                f"📝 <b>PAPER</b> {side.upper()} {decision.symbol}\n"
                f"amt={amount} entry={entry} SL={decision.stop_loss} TP={tp0} lev={leverage}\n"
                f"{decision.rationale}"
            )
            save_trade(
                {
                    "signal_id": signal_id,
                    "client_order_id": signal_id,
                    "symbol": decision.symbol,
                    "side": side,
                    "amount": amount,
                    "price": entry,
                    "status": "paper",
                    "meta": {"leverage": leverage, "sl": decision.stop_loss, "tp": tp0},
                }
            )
            return {"paper": True}

        # LIVE
        try:
            if decision.entry_type == "limit" and decision.limit_price:
                order = self.orders.limit_order(
                    decision.symbol,
                    side,
                    amount,
                    decision.limit_price,
                    stop_loss=decision.stop_loss,
                    take_profit=tp0,
                    client_order_id=signal_id,
                )
            else:
                order = self.orders.market_order(
                    decision.symbol,
                    side,
                    amount,
                    stop_loss=decision.stop_loss,
                    take_profit=tp0,
                    client_order_id=signal_id,
                )
        except Exception as e:
            logger.error(f"LIVE order fail {decision.symbol}: {e}")
            tg_send(f"❌ ORDER FAIL {decision.symbol}: {e}")
            return None

        save_trade(
            {
                "signal_id": signal_id,
                "order_id": order.get("id"),
                "client_order_id": signal_id,
                "symbol": decision.symbol,
                "side": side,
                "amount": amount,
                "price": entry,
                "status": order.get("status"),
                "meta": {"leverage": leverage, "sl": decision.stop_loss, "tp": tp0},
            }
        )
        tg_send(
            f"✅ <b>LIVE</b> {side.upper()} {decision.symbol}\n"
            f"amt={amount} entry={entry} SL={decision.stop_loss} TP={tp0} lev={leverage}\n"
            f"{decision.rationale}"
        )
        # Schedule SL cooldown if SL later triggers (tracked externally)
        set_sl_cooldown(
            decision.symbol,
            int(time.time()) + settings.sl_cooldown_hours * 3600,
        )
        return order

    def _close(self, decision: Decision, signal_id: str) -> dict[str, Any] | None:
        try:
            positions = self.client.fetch_positions([decision.symbol])
        except Exception as e:
            logger.warning(f"close fetch positions fail: {e}")
            return None
        for p in positions:
            contracts = float(p.get("contracts") or 0)
            if contracts <= 0:
                continue
            pos_side = p.get("side") or ("long" if float(p.get("contractSize") or 0) > 0 else "short")
            opposite = "sell" if pos_side.lower().startswith("long") else "buy"
            if settings.mode == "paper":
                logger.info(f"[PAPER] CLOSE {decision.symbol} amt={contracts}")
                tg_send(f"📝 PAPER CLOSE {decision.symbol} amt={contracts}")
                return {"paper": True, "closed": contracts}
            try:
                order = self.orders.close_position(decision.symbol, opposite, contracts)
                tg_send(f"✅ LIVE CLOSE {decision.symbol} amt={contracts}")
                save_trade(
                    {
                        "signal_id": signal_id,
                        "order_id": order.get("id"),
                        "client_order_id": signal_id + "-close",
                        "symbol": decision.symbol,
                        "side": opposite,
                        "amount": contracts,
                        "status": order.get("status"),
                        "meta": {"close": True},
                    }
                )
                return order
            except Exception as e:
                logger.error(f"close fail {decision.symbol}: {e}")
                return None
        return None
