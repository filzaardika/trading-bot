import { useState } from "react";
import { Pause, Play, FastForward, AlertTriangle, Loader2, Check } from "lucide-react";
import { Button, Card, SectionHeader, cn } from "../components/ui";
import { Page } from "../components/TopBar";
import { api } from "../api";
import type { Session } from "../auth";

export function ControlScreen({ session }: { session: Session }) {
  const [busy, setBusy] = useState<string | null>(null);
  const [toast, setToast] = useState<string | null>(null);
  const [confirmFlatten, setConfirmFlatten] = useState(false);

  async function run(key: string, fn: () => Promise<{ ok: boolean; message: string }>) {
    setBusy(key);
    try {
      const r = await fn();
      setToast(r.message || (r.ok ? "Done" : "Failed"));
      setTimeout(() => setToast(null), 2200);
    } catch (e) {
      setToast(e instanceof Error ? e.message : String(e));
      setTimeout(() => setToast(null), 2800);
    } finally {
      setBusy(null);
    }
  }

  return (
    <Page>
      <SectionHeader title="Bot Control" />
      <div className="grid grid-cols-3 gap-3">
        <ActionTile
          label="Pause"
          icon={<Pause size={20} />}
          color="text-accent-amber"
          busy={busy === "pause"}
          onClick={() => run("pause", () => api.pause(session))}
        />
        <ActionTile
          label="Resume"
          icon={<Play size={20} />}
          color="text-accent-teal"
          busy={busy === "resume"}
          onClick={() => run("resume", () => api.resume(session))}
        />
        <ActionTile
          label="Cycle"
          icon={<FastForward size={20} />}
          color="text-accent-indigo"
          busy={busy === "cycle"}
          onClick={() => run("cycle", () => api.cycleNow(session))}
        />
      </div>

      <SectionHeader title="Emergency" />
      <Card className="p-4 border-accent-red/30 bg-accent-red/5">
        <div className="flex items-start gap-3">
          <div className="w-10 h-10 rounded-xl grid place-items-center bg-accent-red/15 text-accent-red shrink-0">
            <AlertTriangle size={20} />
          </div>
          <div className="flex-1">
            <div className="font-semibold text-text-primary">Flatten All Positions</div>
            <div className="text-xs text-text-secondary mt-1 leading-relaxed">
              Closes every open position at market. Irreversible — verify before confirming.
            </div>
          </div>
        </div>
        <Button
          variant="danger"
          className="w-full mt-3"
          onClick={() => setConfirmFlatten(true)}
        >
          Flatten All
        </Button>
      </Card>

      {confirmFlatten && (
        <ConfirmDialog
          title="Flatten all positions?"
          body="This will market-close every open position. You cannot undo this."
          busy={busy === "flatten"}
          onCancel={() => setConfirmFlatten(false)}
          onConfirm={async () => {
            await run("flatten", () => api.flatten(session));
            setConfirmFlatten(false);
          }}
        />
      )}

      {toast && (
        <div className="fixed bottom-24 left-1/2 -translate-x-1/2 px-4 py-2.5 rounded-xl bg-bg-elev border border-border-default shadow-glow text-sm flex items-center gap-2 z-50">
          <Check size={14} className="text-accent-teal" />
          {toast}
        </div>
      )}
    </Page>
  );
}

function ActionTile({
  label,
  icon,
  color,
  busy,
  onClick,
}: {
  label: string;
  icon: React.ReactNode;
  color: string;
  busy: boolean;
  onClick: () => void;
}) {
  return (
    <button
      onClick={onClick}
      disabled={busy}
      className="rounded-2xl bg-bg-surface border border-border-subtle p-4 flex flex-col items-center gap-2 transition-all active:scale-[0.97] hover:border-border-default disabled:opacity-60"
    >
      <div className={cn("w-11 h-11 rounded-xl bg-bg-elev grid place-items-center", color)}>
        {busy ? <Loader2 size={20} className="animate-spin" /> : icon}
      </div>
      <span className="text-xs font-semibold text-text-primary">{label}</span>
    </button>
  );
}

function ConfirmDialog({
  title,
  body,
  busy,
  onCancel,
  onConfirm,
}: {
  title: string;
  body: string;
  busy: boolean;
  onCancel: () => void;
  onConfirm: () => void;
}) {
  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-sm grid place-items-center z-50 p-5">
      <Card className="p-5 w-full max-w-sm">
        <div className="flex items-center gap-3 mb-3">
          <div className="w-10 h-10 rounded-xl bg-accent-red/15 text-accent-red grid place-items-center">
            <AlertTriangle size={20} />
          </div>
          <div className="font-bold text-text-primary">{title}</div>
        </div>
        <p className="text-sm text-text-secondary leading-relaxed">{body}</p>
        <div className="flex gap-2 mt-4">
          <Button variant="secondary" className="flex-1" onClick={onCancel} disabled={busy}>
            Cancel
          </Button>
          <Button variant="danger" className="flex-1" onClick={onConfirm} disabled={busy}>
            {busy && <Loader2 size={14} className="animate-spin" />}
            Confirm
          </Button>
        </div>
      </Card>
    </div>
  );
}
