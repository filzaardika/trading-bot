import { useEffect, useState } from "react";
import { ArrowDownRight, ArrowUpRight, Inbox } from "lucide-react";
import { Card, PnlChip, cn, fmtPct, fmtUsd } from "../components/ui";
import { Page } from "../components/TopBar";
import { api } from "../api";
import type { Session } from "../auth";
import type { Position } from "../types";

export function PositionsScreen({ session }: { session: Session }) {
  const [items, setItems] = useState<Position[] | null>(null);
  const [err, setErr] = useState<string | null>(null);

  async function refresh() {
    try {
      setItems(await api.positions(session));
      setErr(null);
    } catch (e) {
      setErr(e instanceof Error ? e.message : String(e));
    }
  }

  useEffect(() => {
    refresh();
    const t = setInterval(refresh, 4000);
    return () => clearInterval(t);
  }, []);

  if (err) return <Page><Card className="p-4 text-accent-red text-sm">{err}</Card></Page>;

  if (items && items.length === 0) {
    return (
      <Page>
        <div className="mt-24 flex flex-col items-center text-center text-text-muted">
          <Inbox size={40} className="mb-3" />
          <div className="text-sm font-semibold text-text-secondary">No open positions</div>
          <div className="text-xs mt-1">Positions will appear here when the bot opens a trade.</div>
        </div>
      </Page>
    );
  }

  return (
    <Page>
      <div className="space-y-3">
        {items?.map((p) => <PositionCard key={p.id} p={p} />)}
      </div>
    </Page>
  );
}

function PositionCard({ p }: { p: Position }) {
  const isLong = p.side === "long";
  return (
    <Card className="p-4">
      <div className="flex items-start justify-between mb-3">
        <div className="flex items-center gap-2.5">
          <div
            className={cn(
              "w-10 h-10 rounded-xl grid place-items-center",
              isLong ? "bg-accent-teal/15 text-accent-teal" : "bg-accent-red/15 text-accent-red",
            )}
          >
            {isLong ? <ArrowUpRight size={18} /> : <ArrowDownRight size={18} />}
          </div>
          <div>
            <div className="font-bold text-text-primary">{p.symbol}</div>
            <div className="flex items-center gap-1.5 mt-0.5">
              <span
                className={cn(
                  "text-[10px] font-bold uppercase tracking-wider px-1.5 py-0.5 rounded",
                  isLong ? "bg-accent-teal/15 text-accent-teal" : "bg-accent-red/15 text-accent-red",
                )}
              >
                {p.side}
              </span>
              <span className="text-[10px] font-semibold px-1.5 py-0.5 rounded bg-bg-elev text-text-secondary">
                {p.leverage}x
              </span>
            </div>
          </div>
        </div>
        <PnlChip pnl={p.unrealized_pnl} pct={p.unrealized_pnl_pct} />
      </div>

      <PriceTrack p={p} />

      <div className="grid grid-cols-4 gap-2 mt-3">
        <DataCell label="Size" value={p.size.toFixed(4)} />
        <DataCell label="Entry" value={p.entry.toFixed(4)} />
        <DataCell label="Mark" value={p.mark.toFixed(4)} />
        <DataCell label="Opened" value={new Date(p.opened_at * 1000).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })} />
      </div>
    </Card>
  );
}

function DataCell({ label, value }: { label: string; value: string }) {
  return (
    <div className="min-w-0">
      <div className="text-[10px] uppercase tracking-wider text-text-muted font-semibold">{label}</div>
      <div className="num text-sm font-semibold text-text-primary truncate mt-0.5">{value}</div>
    </div>
  );
}

function PriceTrack({ p }: { p: Position }) {
  const sl = p.stop_loss;
  const tp = p.take_profit;
  const vals = [p.entry, p.mark, sl, tp].filter((v): v is number => v != null);
  const min = Math.min(...vals);
  const max = Math.max(...vals);
  const range = Math.max(max - min, 1e-9);
  const pos = (v: number) => `${((v - min) / range) * 100}%`;

  return (
    <div className="relative py-4">
      <div className="h-1 rounded-full bg-bg-elev" />
      {sl != null && <Marker pos={pos(sl)} color="bg-accent-red" label="SL" />}
      {tp != null && <Marker pos={pos(tp)} color="bg-accent-teal" label="TP" />}
      <Marker pos={pos(p.entry)} color="bg-text-secondary" label="E" top />
      <Marker pos={pos(p.mark)} color="bg-accent-indigo" label="●" bold />
    </div>
  );
}

function Marker({
  pos,
  color,
  label,
  top,
  bold,
}: {
  pos: string;
  color: string;
  label: string;
  top?: boolean;
  bold?: boolean;
}) {
  return (
    <div
      className="absolute"
      style={{ left: pos, top: top ? 0 : "50%", transform: "translate(-50%, -50%)" }}
    >
      <div className={cn("w-2 h-2 rounded-full", color, bold && "ring-2 ring-accent-indigo/40")} />
      <div className="text-[9px] text-text-muted text-center mt-0.5 font-semibold">{label}</div>
    </div>
  );
}
