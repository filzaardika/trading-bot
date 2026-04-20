import { useEffect, useState } from "react";
import {
  Wallet,
  TrendingUp,
  TrendingDown,
  Timer,
  Power,
  Activity,
  AlertTriangle,
  RefreshCw,
} from "lucide-react";
import {
  Button,
  Card,
  GradientCard,
  KvRow,
  MetricTile,
  PnlChip,
  SectionHeader,
  StatusPill,
  cn,
  fmtPct,
  fmtUsd,
  pnlColor,
} from "../components/ui";
import { Page } from "../components/TopBar";
import { api } from "../api";
import type { Session } from "../auth";
import type { Dashboard } from "../types";

export function DashboardScreen({ session }: { session: Session }) {
  const [data, setData] = useState<Dashboard | null>(null);
  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  async function refresh() {
    try {
      const d = await api.dashboard(session);
      setData(d);
      setErr(null);
    } catch (e) {
      setErr(e instanceof Error ? e.message : String(e));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    refresh();
    const t = setInterval(refresh, 5000);
    return () => clearInterval(t);
  }, []);

  return (
    <Page>
      {err && (
        <Card className="p-3 mb-3 border-accent-red/30 bg-accent-red/5 flex items-center gap-2 text-accent-red text-xs">
          <AlertTriangle size={14} /> {err}
        </Card>
      )}

      <GradientCard className="p-5">
        <div className="flex items-start justify-between">
          <div>
            <div className="text-white/70 text-xs uppercase tracking-widest font-semibold">
              Account Equity
            </div>
            <div className="num text-4xl font-extrabold text-white mt-1.5">
              {data ? fmtUsd(data.equity) : "—"}
            </div>
            <div className="mt-2">
              {data && <PnlChip pnl={data.pnl_today} pct={data.pnl_today_pct} />}
            </div>
          </div>
          <div className="text-right">
            <div className="text-white/70 text-[10px] uppercase tracking-widest font-semibold">
              Start
            </div>
            <div className="num text-sm font-semibold text-white/90 mt-1">
              {data ? fmtUsd(data.starting_equity) : "—"}
            </div>
          </div>
        </div>
      </GradientCard>

      <div className="grid grid-cols-2 gap-3 mt-4">
        <MetricTile
          label="Open Positions"
          value={data?.open_positions_count ?? "—"}
          icon={<Wallet size={14} />}
          accent="text-accent-indigo"
        />
        <MetricTile
          label="Bot Status"
          value={
            <span className={cn(
              data?.bot_status === "running" && "text-accent-teal",
              data?.bot_status === "paused" && "text-accent-amber",
              data?.bot_status === "error" && "text-accent-red",
            )}>
              {(data?.bot_status ?? "—").toUpperCase()}
            </span>
          }
          icon={<Activity size={14} />}
          accent="text-accent-teal"
        />
      </div>

      <SectionHeader title="Cycle" action={loading ? <RefreshCw size={14} className="animate-spin text-text-muted" /> : null} />
      <Card className="p-4">
        <div className="flex items-center justify-between mb-2">
          <span className="text-xs text-text-secondary">Next cycle</span>
          <span className="num text-sm font-semibold">
            <Timer size={12} className="inline -mt-0.5 mr-1" />
            {data ? `${data.seconds_to_next_cycle}s` : "—"}
          </span>
        </div>
        <div className="h-1.5 rounded-full bg-bg-elev overflow-hidden">
          <div
            className="h-full bg-gradient-to-r from-accent-indigo to-accent-teal transition-all"
            style={{ width: `${Math.round((data?.cycle_progress ?? 0) * 100)}%` }}
          />
        </div>
        <div className="mt-3 space-y-0">
          <KvRow
            label="Last cycle"
            value={
              data?.last_cycle_ts
                ? new Date(data.last_cycle_ts * 1000).toLocaleTimeString()
                : "—"
            }
          />
          {data?.last_cycle_error && (
            <KvRow label="Last error" value={<span className="text-accent-red">{data.last_cycle_error}</span>} />
          )}
        </div>
      </Card>

      <div className="mt-6">
        <Button
          variant={data?.kill_switch ? "secondary" : "danger"}
          className="w-full py-5 text-base"
          onClick={async () => {
            if (!data) return;
            if (data.kill_switch) await api.killReset(session);
            else await api.kill(session);
            refresh();
          }}
        >
          <Power size={20} />
          {data?.kill_switch ? "Reset Kill Switch" : "KILL SWITCH"}
        </Button>
        {data?.kill_switch && (
          <div className="mt-2 text-center">
            <StatusPill label="KILL SWITCH ACTIVE" tone="negative" icon={<AlertTriangle size={12} />} />
          </div>
        )}
      </div>
    </Page>
  );
}
