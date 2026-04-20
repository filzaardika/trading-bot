import { useState } from "react";
import { Sparkles, Link as LinkIcon, Key, Loader2, AlertCircle, Eye, EyeOff, ClipboardPaste } from "lucide-react";
import { Button, Card } from "../components/ui";
import { pair, ping, ApiError } from "../api";
import { saveSession } from "../auth";

export function PairScreen({ onPaired }: { onPaired: () => void }) {
  const [baseUrl, setBaseUrl] = useState("https://bot.opclaw.my.id");
  const [token, setToken] = useState("");
  const [showToken, setShowToken] = useState(false);
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setErr(null);
    try {
      const clean = baseUrl.trim().replace(/\/+$/, "");
      await ping(clean);
      const r = await pair(clean, token.trim());
      if (!r.ok) throw new Error("Pairing rejected");
      saveSession({ baseUrl: clean, token: token.trim() });
      onPaired();
    } catch (e) {
      const msg = e instanceof ApiError ? `API error (${e.status}): ${e.message}` : e instanceof Error ? e.message : String(e);
      setErr(msg);
    } finally {
      setBusy(false);
    }
  }

  async function pasteToken() {
    try {
      const t = await navigator.clipboard.readText();
      if (t) setToken(t.trim());
    } catch {
      setErr("Clipboard blocked. Long-press the field and tap Paste.");
    }
  }

  async function pasteUrl() {
    try {
      const t = await navigator.clipboard.readText();
      if (t) setBaseUrl(t.trim());
    } catch {
      /* ignore */
    }
  }

  return (
    <div className="min-h-dvh grid place-items-center px-5 py-10 bg-[radial-gradient(ellipse_at_top,rgba(99,102,241,0.18),transparent_55%)]">
      <div className="w-full max-w-sm">
        <div className="flex flex-col items-center text-center mb-8">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-accent-indigo to-accent-teal grid place-items-center shadow-glow mb-4">
            <Sparkles size={28} className="text-white" />
          </div>
          <h1 className="text-2xl font-bold">Trading Bot</h1>
          <p className="text-text-secondary text-sm mt-1">
            Pair this device with your VPS bot
          </p>
        </div>

        <Card className="p-5">
          <form onSubmit={submit} className="space-y-4">
            <Field
              icon={<LinkIcon size={16} />}
              label="Base URL"
              placeholder="https://bot.example.com"
              value={baseUrl}
              onChange={setBaseUrl}
              autoComplete="url"
              inputMode="url"
              trailing={
                <IconBtn label="Paste" onClick={pasteUrl}>
                  <ClipboardPaste size={14} />
                </IconBtn>
              }
            />
            <Field
              icon={<Key size={16} />}
              label="API Token"
              placeholder="Paste bearer token"
              value={token}
              onChange={setToken}
              type={showToken ? "text" : "password"}
              autoComplete="off"
              trailing={
                <div className="flex items-center gap-1">
                  <IconBtn label="Paste" onClick={pasteToken}>
                    <ClipboardPaste size={14} />
                  </IconBtn>
                  <IconBtn label={showToken ? "Hide" : "Show"} onClick={() => setShowToken((v) => !v)}>
                    {showToken ? <EyeOff size={14} /> : <Eye size={14} />}
                  </IconBtn>
                </div>
              }
            />
            {err && (
              <div className="flex items-start gap-2 p-3 rounded-xl bg-accent-red/10 border border-accent-red/30 text-accent-red text-xs">
                <AlertCircle size={16} className="shrink-0 mt-0.5" />
                <span>{err}</span>
              </div>
            )}
            <Button type="submit" disabled={busy || !baseUrl || !token} className="w-full">
              {busy ? <Loader2 className="animate-spin" size={16} /> : null}
              {busy ? "Pairing…" : "Pair device"}
            </Button>
          </form>
        </Card>

        <p className="text-[11px] text-text-muted text-center mt-4 leading-relaxed">
          Token is stored in this browser only. Use HTTPS — never send tokens over plain HTTP.
        </p>
      </div>
    </div>
  );
}

function IconBtn({ children, onClick, label }: { children: React.ReactNode; onClick: () => void; label: string }) {
  return (
    <button
      type="button"
      onClick={onClick}
      aria-label={label}
      className="shrink-0 px-2 py-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-bg-surfaceAlt transition-colors"
    >
      {children}
    </button>
  );
}

function Field({
  icon,
  label,
  value,
  onChange,
  placeholder,
  type = "text",
  autoComplete,
  inputMode,
  trailing,
}: {
  icon: React.ReactNode;
  label: string;
  value: string;
  onChange: (v: string) => void;
  placeholder?: string;
  type?: string;
  autoComplete?: string;
  inputMode?: React.HTMLAttributes<HTMLInputElement>["inputMode"];
  trailing?: React.ReactNode;
}) {
  return (
    <label className="block">
      <div className="text-xs font-semibold text-text-secondary mb-1.5 ml-1">{label}</div>
      <div className="flex items-center gap-2 pl-3 pr-1 py-1.5 rounded-xl bg-bg-elev border border-border-default focus-within:border-accent-indigo transition-colors">
        <span className="text-text-muted">{icon}</span>
        <input
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          type={type}
          autoComplete={autoComplete}
          inputMode={inputMode}
          autoCapitalize="none"
          autoCorrect="off"
          spellCheck={false}
          className="flex-1 min-w-0 bg-transparent outline-none text-sm text-text-primary placeholder:text-text-muted py-1.5 select-text"
          style={{ userSelect: "text", WebkitUserSelect: "text" }}
        />
        {trailing}
      </div>
    </label>
  );
}
