import type { Session } from "./auth";
import type { Dashboard, Position, Settings, ControlResp } from "./types";

export class ApiError extends Error {
  constructor(public status: number, msg: string) {
    super(msg);
  }
}

async function req<T>(s: Session, path: string, init: RequestInit = {}): Promise<T> {
  const url = s.baseUrl.replace(/\/+$/, "") + path;
  const res = await fetch(url, {
    ...init,
    headers: {
      Authorization: `Bearer ${s.token}`,
      "Content-Type": "application/json",
      ...(init.headers || {}),
    },
  });
  if (!res.ok) {
    let msg = `HTTP ${res.status}`;
    try {
      const body = await res.json();
      if (body?.detail) msg = typeof body.detail === "string" ? body.detail : JSON.stringify(body.detail);
    } catch {
      /* ignore */
    }
    throw new ApiError(res.status, msg);
  }
  if (res.status === 204) return undefined as T;
  return (await res.json()) as T;
}

export async function ping(baseUrl: string): Promise<{ ok: boolean; version: string }> {
  const url = baseUrl.replace(/\/+$/, "") + "/health";
  const res = await fetch(url);
  if (!res.ok) throw new ApiError(res.status, `HTTP ${res.status}`);
  return res.json();
}

export async function pair(baseUrl: string, token: string): Promise<{ ok: boolean; bot_name: string; mode: string; testnet: boolean }> {
  const url = baseUrl.replace(/\/+$/, "") + "/auth/pair";
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ token }),
  });
  if (!res.ok) throw new ApiError(res.status, `HTTP ${res.status}`);
  return res.json();
}

export const api = {
  dashboard: (s: Session) => req<Dashboard>(s, "/dashboard"),
  positions: (s: Session) => req<Position[]>(s, "/positions"),
  settings: (s: Session) => req<Settings>(s, "/settings"),
  pause: (s: Session) => req<ControlResp>(s, "/control/pause", { method: "POST", body: "{}" }),
  resume: (s: Session) => req<ControlResp>(s, "/control/resume", { method: "POST", body: "{}" }),
  cycleNow: (s: Session) => req<ControlResp>(s, "/control/cycle-now", { method: "POST", body: "{}" }),
  flatten: (s: Session) => req<ControlResp>(s, "/control/flatten", { method: "POST", body: "{}" }),
  kill: (s: Session) => req<ControlResp>(s, "/control/kill", { method: "POST", body: "{}" }),
  killReset: (s: Session) => req<ControlResp>(s, "/control/kill/reset", { method: "POST", body: "{}" }),
};
