# Trading Bot Web

Single-page React (Vite + TypeScript + Tailwind) app that mirrors the design spec. Ships as the content loaded by the thin WebView Android APK (`android-webview/`).

## Dev

```bash
npm install
npm run dev
```

## Build & deploy (VPS)

```bash
npm ci
npm run build
sudo systemctl restart trading-bot-web
```

Served at `http://127.0.0.1:8200` via the `trading-bot-web.service` systemd unit, tunneled by Cloudflare to `https://app.opclaw.my.id`.

## Config

The app stores the bot base URL + bearer token in `localStorage` after pairing. The default base URL is pre-filled to `https://bot.opclaw.my.id` but any reachable HTTPS endpoint works.
