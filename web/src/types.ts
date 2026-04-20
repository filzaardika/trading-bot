export type BotStatus = "running" | "paused" | "stopped" | "error";
export type Mode = "paper" | "live";

export interface Dashboard {
  equity: number;
  starting_equity: number;
  pnl_today: number;
  pnl_today_pct: number;
  open_positions_count: number;
  mode: Mode;
  testnet: boolean;
  bot_status: BotStatus;
  kill_switch: boolean;
  cycle_progress: number;
  seconds_to_next_cycle: number;
  last_cycle_ts: number;
  last_cycle_error: string;
}

export interface Position {
  id: string;
  symbol: string;
  side: "long" | "short";
  size: number;
  entry: number;
  mark: number;
  leverage: number;
  unrealized_pnl: number;
  unrealized_pnl_pct: number;
  stop_loss: number | null;
  take_profit: number | null;
  opened_at: number;
}

export interface Settings {
  mode: string;
  testnet: boolean;
  universe_size: number;
  cycle_seconds: number;
  max_risk_pct_per_trade: number;
  max_concurrent_positions: number;
  max_leverage: number;
  daily_loss_halt_pct: number;
  max_drawdown_kill_pct: number;
}

export interface ControlResp {
  ok: boolean;
  message: string;
}
