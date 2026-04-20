import { Sparkles } from "lucide-react";
import { PulsingDot, StatusPill, cn } from "./ui";

export function TopBar({
  mode,
  testnet,
  connected,
}: {
  mode: "paper" | "live";
  testnet: boolean;
  connected: boolean;
}) {
  return (
    <header className="safe-top sticky top-0 z-20 bg-bg-base/80 backdrop-blur-xl border-b border-border-subtle">
      <div className="max-w-md mx-auto px-4 py-3 flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <div className="relative w-9 h-9 rounded-xl bg-gradient-to-br from-accent-indigo to-accent-teal grid place-items-center shadow-glow">
            <Sparkles size={18} className="text-white" />
          </div>
          <div>
            <div className="text-sm font-bold text-text-primary leading-tight">Trading Bot</div>
            <div className="flex items-center gap-1.5 text-[10px] text-text-muted">
              <PulsingDot color={connected ? "bg-accent-teal" : "bg-accent-red"} />
              <span className="font-semibold tracking-wider uppercase">
                {connected ? "LIVE" : "Offline"}
              </span>
            </div>
          </div>
        </div>
        <div className="flex gap-1.5">
          <StatusPill
            label={mode === "live" ? "LIVE" : "PAPER"}
            tone={mode === "live" ? "negative" : "neutral"}
          />
          <StatusPill label={testnet ? "TESTNET" : "MAINNET"} tone={testnet ? "warn" : "neutral"} />
        </div>
      </div>
    </header>
  );
}

export function Page({ children, pad = true }: { children: React.ReactNode; pad?: boolean }) {
  return (
    <main className={cn("max-w-md mx-auto pb-28", pad && "px-4 pt-4")}>{children}</main>
  );
}
