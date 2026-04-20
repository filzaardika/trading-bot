import { LayoutDashboard, LineChart, Power, Settings as SettingsIcon, type LucideIcon } from "lucide-react";
import { cn } from "./ui";

export type Tab = "dashboard" | "positions" | "control" | "settings";

const items: { id: Tab; label: string; icon: LucideIcon }[] = [
  { id: "dashboard", label: "Dashboard", icon: LayoutDashboard },
  { id: "positions", label: "Positions", icon: LineChart },
  { id: "control", label: "Control", icon: Power },
  { id: "settings", label: "Settings", icon: SettingsIcon },
];

export function BottomNav({ active, onChange }: { active: Tab; onChange: (t: Tab) => void }) {
  return (
    <nav className="fixed bottom-0 left-0 right-0 bg-bg-surface/90 backdrop-blur-xl border-t border-border-subtle safe-bottom z-30">
      <div className="max-w-md mx-auto flex items-center justify-around px-2 pt-2">
        {items.map((it) => {
          const Icon = it.icon;
          const on = active === it.id;
          return (
            <button
              key={it.id}
              onClick={() => onChange(it.id)}
              className={cn(
                "flex flex-col items-center gap-1 px-3 py-1.5 rounded-xl transition-colors flex-1",
                on ? "text-accent-indigo" : "text-text-muted",
              )}
            >
              <div
                className={cn(
                  "px-4 py-1.5 rounded-full transition-all",
                  on ? "bg-accent-indigo/15" : "bg-transparent",
                )}
              >
                <Icon size={20} strokeWidth={on ? 2.4 : 2} />
              </div>
              <span className={cn("text-[10px] font-semibold tracking-wide", on ? "opacity-100" : "opacity-70")}>
                {it.label}
              </span>
            </button>
          );
        })}
      </div>
    </nav>
  );
}
