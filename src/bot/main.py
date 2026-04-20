from __future__ import annotations

import signal as sigmod
import sys
import time

from apscheduler.schedulers.blocking import BlockingScheduler

from bot.brain.claude import decide, decision_to_signal, new_signal_id
from bot.config import settings
from bot.exchange.client import BybitClient
from bot.exchange.orders import OrderManager
from bot.executor.engine import Executor
from bot.indicators.features import extract_snapshot, ohlcv_tail
from bot.indicators.ta import add_indicators, ohlcv_to_df
from bot.news.aggregator import cache_news, fetch_all, top_news_for_prompt
from bot.notify.telegram import send as tg_send
from bot.risk.guards import (
    evaluate,
    kill_switch_active,
    record_day_start_equity,
    record_start_equity_if_missing,
)
from bot.screener.filters import apply_filters
from bot.screener.universe import screen_universe
from bot.storage.db import init_db
from bot.storage.models import save_signal
from bot.utils.logger import logger, setup_logging


class Cycle:
    def __init__(self) -> None:
        self.client = BybitClient()
        self.orders = OrderManager(self.client)
        self.executor = Executor(self.client, self.orders)
        self._news_cache: list = []
        self._news_fetched_at: float = 0.0

    def _news(self) -> list:
        now = time.time()
        ttl = settings.news_cache_minutes * 60
        if now - self._news_fetched_at > ttl or not self._news_cache:
            items = fetch_all()
            cache_news(items)
            self._news_cache = items
            self._news_fetched_at = now
        return self._news_cache

    def run_once(self) -> None:
        from bot.api.state import STATE

        t0 = time.time()
        STATE.last_cycle_error = ""
        try:
            if STATE.paused:
                logger.info("bot paused; skipping cycle")
                return
            if STATE.flatten_request:
                STATE.flatten_request = False
                self._flatten_all()
            self._run_once_inner()
        except Exception as e:
            logger.exception(f"cycle fatal: {e}")
            STATE.last_cycle_error = str(e)
            tg_send(f"⚠️ cycle fatal: {e}")
        finally:
            STATE.last_cycle_ts = time.time()
            STATE.last_cycle_duration_ms = (STATE.last_cycle_ts - t0) * 1000

    def _flatten_all(self) -> None:
        try:
            positions = self.client.fetch_positions()
        except Exception as e:
            logger.warning(f"flatten fetch_positions fail: {e}")
            return
        closed = 0
        for p in positions:
            size = float(p.get("contracts") or 0)
            if size <= 0:
                continue
            sym = p.get("symbol")
            side = "sell" if (p.get("side") or "").lower() == "long" else "buy"
            try:
                self.client.raw.create_order(sym, "market", side, size, params={"reduceOnly": True})
                closed += 1
            except Exception as e:
                logger.warning(f"flatten {sym} fail: {e}")
        logger.warning(f"FLATTEN ALL executed; closed={closed}")
        tg_send(f"🛑 Flatten all: closed {closed} position(s)")

    def _run_once_inner(self) -> None:
        if kill_switch_active():
            logger.error("kill switch active; skipping cycle")
            return

        equity = self.client.equity_usdt()
        logger.info(f"equity={equity} {settings.quote_currency}")
        record_start_equity_if_missing(equity)
        record_day_start_equity(equity)

        # Expose latest values to API dashboard
        from bot.storage.models import kv_set as _kvset
        _kvset("latest_equity", str(equity))

        candidates = screen_universe(self.client)
        candidates = apply_filters(self.client, candidates)
        if not candidates:
            logger.info("no candidates pass filters")
            return

        try:
            positions = self.client.fetch_positions()
        except Exception as e:
            logger.warning(f"fetch positions fail: {e}")
            positions = []
        open_count = sum(1 for p in positions if float(p.get("contracts") or 0) > 0)
        from bot.storage.models import kv_set as _kvset
        _kvset("open_positions_count", str(open_count))

        news_items = self._news()

        for cand in candidates:
            try:
                self._evaluate_symbol(cand.symbol, equity, open_count, news_items)
            except Exception as e:
                logger.warning(f"{cand.symbol} eval fail: {e}")

    def _evaluate_symbol(
        self, symbol: str, equity: float, open_count: int, news_items: list
    ) -> None:
        ohlcv = self.client.fetch_ohlcv(symbol, timeframe=settings.timeframe, limit=250)
        if len(ohlcv) < 60:
            return
        df = add_indicators(ohlcv_to_df(ohlcv))
        snapshot = extract_snapshot(df)
        tail = ohlcv_tail(df, 30)

        ob = self.client.fetch_order_book(symbol, limit=10)
        bids, asks = ob.get("bids") or [], ob.get("asks") or []
        spread_bps = 0.0
        if bids and asks:
            bid, ask = bids[0][0], asks[0][0]
            spread_bps = ((ask - bid) / ask * 10_000) if ask > 0 else 0.0

        try:
            funding = self.client.fetch_funding_rate(symbol)
        except Exception:
            funding = 0.0

        base = symbol.split("/")[0]
        news = top_news_for_prompt(news_items, base, n=8)

        payload = {
            "symbol": symbol,
            "timeframe": settings.timeframe,
            "indicators": snapshot,
            "ohlcv_tail": tail,
            "orderbook": {"spread_bps": round(spread_bps, 3), "best_bid": bids[0] if bids else None, "best_ask": asks[0] if asks else None},
            "funding_rate": funding,
            "news": news,
            "portfolio": {
                "equity_usdt": equity,
                "open_positions": open_count,
                "mode": settings.mode,
            },
            "constraints": {
                "max_risk_pct": settings.max_risk_pct_per_trade,
                "max_leverage": settings.max_leverage,
                "live_size_cap_pct": settings.live_size_cap_pct if settings.mode == "live" else None,
            },
        }

        decision, raw = decide(payload)
        if decision is None:
            logger.info(f"{symbol}: brain no-decision")
            return

        sig_id = new_signal_id(symbol)
        sig = decision_to_signal(decision, sig_id)
        risk = evaluate(sig, equity, open_count)
        if not risk.allow:
            logger.info(f"{symbol} reject: {risk.reason}")
            save_signal({**sig, "action": "hold", "rationale": f"risk-reject: {risk.reason}"}, raw)
            return

        current_price = snapshot.get("close") or 0.0
        self.executor.execute(decision, sig_id, risk, equity, current_price, raw)


def _install_signal_handlers(scheduler: BlockingScheduler) -> None:
    def _stop(signum, frame):
        logger.warning(f"signal {signum}; shutting down")
        scheduler.shutdown(wait=False)
        sys.exit(0)

    sigmod.signal(sigmod.SIGINT, _stop)
    sigmod.signal(sigmod.SIGTERM, _stop)


def main() -> None:
    setup_logging()
    init_db()
    logger.info(
        f"Bot start. mode={settings.mode} testnet={settings.bybit_testnet} "
        f"cycle={settings.cycle_seconds}s"
    )
    tg_send(f"🤖 Bot start. mode={settings.mode} testnet={settings.bybit_testnet}")

    cycle = Cycle()
    scheduler = BlockingScheduler(timezone="UTC")
    scheduler.add_job(cycle.run_once, "interval", seconds=settings.cycle_seconds, next_run_time=None)
    _install_signal_handlers(scheduler)

    # Run one cycle immediately
    cycle.run_once()
    scheduler.start()


if __name__ == "__main__":
    main()
