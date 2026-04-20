const LS = { base: "tb.baseUrl", token: "tb.token" };

export interface Session {
  baseUrl: string;
  token: string;
}

export function loadSession(): Session | null {
  const baseUrl = localStorage.getItem(LS.base);
  const token = localStorage.getItem(LS.token);
  if (!baseUrl || !token) return null;
  return { baseUrl, token };
}

export function saveSession(s: Session) {
  localStorage.setItem(LS.base, s.baseUrl.replace(/\/+$/, ""));
  localStorage.setItem(LS.token, s.token);
}

export function clearSession() {
  localStorage.removeItem(LS.base);
  localStorage.removeItem(LS.token);
}
