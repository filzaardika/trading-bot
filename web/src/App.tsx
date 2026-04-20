import { useEffect, useState } from "react";
import { TopBar } from "./components/TopBar";
import { BottomNav, type Tab } from "./components/BottomNav";
import { PairScreen } from "./screens/Pair";
import { DashboardScreen } from "./screens/Dashboard";
import { PositionsScreen } from "./screens/Positions";
import { ControlScreen } from "./screens/Control";
import { SettingsScreen } from "./screens/Settings";
import { loadSession, type Session } from "./auth";
import { api } from "./api";

export default function App() {
  const [session, setSession] = useState<Session | null>(() => loadSession());
  const [tab, setTab] = useState<Tab>("dashboard");
  const [meta, setMeta] = useState<{ mode: "paper" | "live"; testnet: boolean; connected: boolean }>({
    mode: "paper",
    testnet: true,
    connected: false,
  });

  useEffect(() => {
    if (!session) return;
    let alive = true;
    const tick = async () => {
      try {
        const d = await api.dashboard(session);
        if (!alive) return;
        setMeta({ mode: d.mode, testnet: d.testnet, connected: true });
      } catch {
        if (!alive) return;
        setMeta((m) => ({ ...m, connected: false }));
      }
    };
    tick();
    const t = setInterval(tick, 7000);
    return () => {
      alive = false;
      clearInterval(t);
    };
  }, [session]);

  if (!session) {
    return <PairScreen onPaired={() => setSession(loadSession())} />;
  }

  return (
    <>
      <TopBar mode={meta.mode} testnet={meta.testnet} connected={meta.connected} />
      {tab === "dashboard" && <DashboardScreen session={session} />}
      {tab === "positions" && <PositionsScreen session={session} />}
      {tab === "control" && <ControlScreen session={session} />}
      {tab === "settings" && (
        <SettingsScreen session={session} onUnpaired={() => setSession(null)} />
      )}
      <BottomNav active={tab} onChange={setTab} />
    </>
  );
}
