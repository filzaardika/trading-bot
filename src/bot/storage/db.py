from __future__ import annotations

import sqlite3
from contextlib import contextmanager
from pathlib import Path
from typing import Iterator

from bot.config import settings


SCHEMA = """
CREATE TABLE IF NOT EXISTS signals (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    signal_id TEXT UNIQUE NOT NULL,
    ts INTEGER NOT NULL,
    symbol TEXT NOT NULL,
    action TEXT NOT NULL,
    size_pct_equity REAL,
    entry_type TEXT,
    limit_price REAL,
    stop_loss REAL,
    take_profit TEXT,
    confidence REAL,
    rationale TEXT,
    ttl_seconds INTEGER,
    raw_response TEXT
);

CREATE TABLE IF NOT EXISTS trades (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    signal_id TEXT,
    order_id TEXT,
    client_order_id TEXT UNIQUE,
    ts INTEGER NOT NULL,
    symbol TEXT NOT NULL,
    side TEXT NOT NULL,
    amount REAL NOT NULL,
    price REAL,
    status TEXT,
    pnl_usdt REAL,
    meta TEXT
);

CREATE TABLE IF NOT EXISTS positions_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ts INTEGER NOT NULL,
    symbol TEXT NOT NULL,
    side TEXT NOT NULL,
    amount REAL NOT NULL,
    entry REAL,
    mark REAL,
    unrealized_pnl REAL,
    leverage REAL
);

CREATE TABLE IF NOT EXISTS news_cache (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fetched_at INTEGER NOT NULL,
    source TEXT NOT NULL,
    url TEXT UNIQUE,
    title TEXT NOT NULL,
    published_at INTEGER,
    body TEXT,
    sentiment REAL
);

CREATE TABLE IF NOT EXISTS kv (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    updated_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS sl_cooldowns (
    symbol TEXT PRIMARY KEY,
    until_ts INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_signals_symbol_ts ON signals(symbol, ts);
CREATE INDEX IF NOT EXISTS idx_trades_symbol_ts ON trades(symbol, ts);
CREATE INDEX IF NOT EXISTS idx_news_published ON news_cache(published_at);
"""


def _ensure_parent(p: Path) -> None:
    p.parent.mkdir(parents=True, exist_ok=True)


def init_db(path: Path | None = None) -> None:
    path = path or settings.db_path
    _ensure_parent(path)
    with sqlite3.connect(path) as conn:
        conn.executescript(SCHEMA)


@contextmanager
def connect(path: Path | None = None) -> Iterator[sqlite3.Connection]:
    path = path or settings.db_path
    _ensure_parent(path)
    conn = sqlite3.connect(path, isolation_level=None, timeout=10)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA journal_mode=WAL;")
    conn.execute("PRAGMA synchronous=NORMAL;")
    try:
        yield conn
    finally:
        conn.close()
