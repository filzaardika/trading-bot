"""Shared runtime state exposed to API + consumed by bot main loop."""
from __future__ import annotations

import threading
import time
from collections import deque
from dataclasses import dataclass, field
from typing import Deque


@dataclass
class RuntimeState:
    started_at: float = field(default_factory=time.time)
    last_cycle_ts: float = 0.0
    last_cycle_duration_ms: float = 0.0
    last_cycle_error: str = ""
    cycle_progress: float = 0.0
    paused: bool = False
    force_cycle_request: bool = False
    flatten_request: bool = False
    recent_logs: Deque[str] = field(default_factory=lambda: deque(maxlen=500))
    lock: threading.Lock = field(default_factory=threading.Lock)

    def snapshot_logs(self, limit: int = 200) -> list[str]:
        with self.lock:
            return list(self.recent_logs)[-limit:]

    def push_log(self, line: str) -> None:
        with self.lock:
            self.recent_logs.append(line)


STATE = RuntimeState()
