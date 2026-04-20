import { useEffect, useState } from "react";
import { LogOut, SlidersHorizontal, Radar, ShieldAlert } from "lucide-react";
import { Button, Card, KvRow, SectionHeader } from "../components/ui";
import { Page } from "../components/TopBar";
import { api } from "../api";
import type { Session } from "../auth";
import type { Settings } from "../types";
import { clearSession } from "../auth";

export function SettingsScreen({ session, onUnpaired }: { session: Session; onUnpaired: () => void }) {
  const [data, setData] = useState<Settings | null>(null);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    api.settings(session).then(setData).catch((e) => setErr(e.message));
  }, []);

  if (err) return <Page><Card className="p-4 text-accent-red text-sm">{err}</Card></Page>;

  return (
    <Page>
      <Group
        title="Trading Mode"
        icon={<SlidersHorizontal size={16} />}
        badge="bg-accent-indigo/15 text-accent-indigo"
      >
        <KvRow label="Mode" value={<span className="uppercase">{data?.mode ?? "—"}</span>} />
        <KvRow label="Testnet" value={data?.testnet ? "Yes" : "No"} />
      </Group>

      <Group
        title="Scanner"
        icon={<Radar size={16} />}
        badge="bg-accent-teal/15 text-accent-teal"
      >
        <KvRow label="Universe size" value={data?.universe_size ?? "—"} />
        <KvRow label="Cycle interval" value={data ? `${data.cycle_seconds}s` : "—"} />
      </Group>

      <Group
        title="Risk Limits"
        icon={<ShieldAlert size={16} />}
        badge="bg-accent-amber/15 text-accent-amber"
      >
        <KvRow label="Max risk / trade" value={data ? `${data.max_risk_pct_per_trade}%` : "—"} />
        <KvRow label="Max concurrent" value={data?.max_concurrent_positions ?? "—"} />
        <KvRow label="Max leverage" value={data ? `${data.max_leverage}x` : "—"} />
        <KvRow label="Daily loss halt" value={data ? `${data.daily_loss_halt_pct}%` : "—"} />
        <KvRow label="Max drawdown kill" value={data ? `${data.max_drawdown_kill_pct}%` : "—"} />
      </Group>

      <SectionHeader title="Device" />
      <Card className="p-4">
        <div className="flex items-start gap-3">
          <div className="w-10 h-10 rounded-xl grid place-items-center bg-accent-red/15 text-accent-red shrink-0">
            <LogOut size={18} />
          </div>
          <div className="flex-1">
            <div className="font-semibold text-text-primary">Unpair this device</div>
            <div className="text-xs text-text-secondary mt-1">
              Removes stored URL and token from this browser.
            </div>
          </div>
        </div>
        <Button
          variant="secondary"
          className="w-full mt-3"
          onClick={() => {
            clearSession();
            onUnpaired();
          }}
        >
          Unpair
        </Button>
      </Card>
    </Page>
  );
}

function Group({
  title,
  icon,
  badge,
  children,
}: {
  title: string;
  icon: React.ReactNode;
  badge: string;
  children: React.ReactNode;
}) {
  return (
    <div className="mb-5">
      <SectionHeader title={title} />
      <Card className="p-4">
        <div className="flex items-center gap-2 mb-2">
          <span className={`w-7 h-7 rounded-lg grid place-items-center ${badge}`}>{icon}</span>
        </div>
        <div>{children}</div>
      </Card>
    </div>
  );
}
