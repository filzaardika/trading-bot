import { ReactNode } from "react";

export function cn(...v: (string | false | null | undefined)[]) {
  return v.filter(Boolean).join(" ");
}

export function fmtUsd(n: number, opts: { sign?: boolean; digits?: number } = {}) {
  const digits = opts.digits ?? 2;
  const abs = Math.abs(n);
  const str = abs.toLocaleString("en-US", { minimumFractionDigits: digits, maximumFractionDigits: digits });
  const sign = n < 0 ? "-" : opts.sign && n > 0 ? "+" : "";
  return `${sign}$${str}`;
}

export function fmtPct(n: number, sign = true) {
  const s = n < 0 ? "-" : sign && n > 0 ? "+" : "";
  return `${s}${Math.abs(n).toFixed(2)}%`;
}

export function pnlColor(n: number) {
  if (n > 0) return "text-accent-teal";
  if (n < 0) return "text-accent-red";
  return "text-text-secondary";
}

export function Card({ children, className }: { children: ReactNode; className?: string }) {
  return (
    <div
      className={cn(
        "rounded-2xl bg-bg-surface border border-border-subtle shadow-card",
        className,
      )}
    >
      {children}
    </div>
  );
}

export function GradientCard({ children, className }: { children: ReactNode; className?: string }) {
  return (
    <div
      className={cn(
        "relative rounded-3xl overflow-hidden",
        "bg-gradient-to-br from-accent-indigo via-accent-indigoDim to-[#1e1b4b]",
        "shadow-glow",
        className,
      )}
    >
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(255,255,255,0.15),transparent_60%)]" />
      <div className="relative">{children}</div>
    </div>
  );
}

export function PulsingDot({ color = "bg-accent-teal" }: { color?: string }) {
  return (
    <span className="relative inline-flex h-2.5 w-2.5">
      <span className={cn("absolute inline-flex h-full w-full rounded-full opacity-60 animate-ring-pulse", color)} />
      <span className={cn("relative inline-flex rounded-full h-2.5 w-2.5 animate-pulse-dot", color)} />
    </span>
  );
}

export function StatusPill({
  label,
  tone = "neutral",
  icon,
}: {
  label: string;
  tone?: "positive" | "negative" | "warn" | "neutral";
  icon?: ReactNode;
}) {
  const tones: Record<string, string> = {
    positive: "bg-accent-teal/15 text-accent-teal border-accent-teal/30",
    negative: "bg-accent-red/15 text-accent-red border-accent-red/30",
    warn: "bg-accent-amber/15 text-accent-amber border-accent-amber/30",
    neutral: "bg-bg-elev text-text-secondary border-border-subtle",
  };
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold border",
        tones[tone],
      )}
    >
      {icon}
      {label}
    </span>
  );
}

export function PnlChip({ pnl, pct }: { pnl: number; pct?: number }) {
  const tone = pnl > 0 ? "positive" : pnl < 0 ? "negative" : "neutral";
  const arrow = pnl > 0 ? "▲" : pnl < 0 ? "▼" : "–";
  return (
    <StatusPill
      tone={tone}
      label={`${arrow} ${fmtUsd(pnl, { sign: true })}${pct !== undefined ? ` (${fmtPct(pct)})` : ""}`}
    />
  );
}

export function MetricTile({
  label,
  value,
  sub,
  icon,
  accent,
}: {
  label: string;
  value: ReactNode;
  sub?: ReactNode;
  icon?: ReactNode;
  accent?: string;
}) {
  return (
    <Card className="p-4 flex-1 min-w-0">
      <div className="flex items-center gap-2 text-text-secondary text-xs font-medium uppercase tracking-wider">
        {icon && <span className={cn("p-1 rounded-md bg-bg-elev", accent)}>{icon}</span>}
        <span>{label}</span>
      </div>
      <div className="mt-2 num text-2xl font-bold text-text-primary truncate">{value}</div>
      {sub && <div className="mt-1 text-xs text-text-muted">{sub}</div>}
    </Card>
  );
}

export function SectionHeader({ title, action }: { title: string; action?: ReactNode }) {
  return (
    <div className="flex items-center justify-between mb-3 px-1">
      <h2 className="text-sm font-semibold text-text-secondary uppercase tracking-widest">{title}</h2>
      {action}
    </div>
  );
}

export function KvRow({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div className="flex items-center justify-between py-2.5 border-b border-border-subtle last:border-0">
      <span className="text-sm text-text-secondary">{label}</span>
      <span className="num text-sm font-semibold text-text-primary">{value}</span>
    </div>
  );
}

export function Button({
  children,
  onClick,
  variant = "primary",
  disabled,
  className,
  type = "button",
}: {
  children: ReactNode;
  onClick?: () => void;
  variant?: "primary" | "secondary" | "danger" | "ghost";
  disabled?: boolean;
  className?: string;
  type?: "button" | "submit";
}) {
  const variants: Record<string, string> = {
    primary:
      "bg-gradient-to-br from-accent-indigo to-accent-indigoDim text-white hover:brightness-110 shadow-glow",
    secondary: "bg-bg-elev text-text-primary border border-border-default hover:bg-bg-surfaceAlt",
    danger:
      "bg-gradient-to-br from-accent-red to-red-600 text-white hover:brightness-110",
    ghost: "text-text-secondary hover:text-text-primary hover:bg-bg-elev",
  };
  return (
    <button
      type={type}
      disabled={disabled}
      onClick={onClick}
      className={cn(
        "inline-flex items-center justify-center gap-2 rounded-xl px-4 py-3 text-sm font-semibold transition-all",
        "active:scale-[0.98] disabled:opacity-40 disabled:cursor-not-allowed",
        variants[variant],
        className,
      )}
    >
      {children}
    </button>
  );
}
