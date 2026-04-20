# Trading Bot — Claude Opus 4.7 Brain

Crypto futures trading bot. Brain = Claude Opus 4.7 via `gh copilot` CLI subprocess.
Target: **Bybit USDT Perpetual**. Full-auto mode. Risk-capped.

## ⚠️ Disclaimer

Live trading = real money. You can lose everything. Bugs can cause losses.
Always start in **paper mode** + **testnet**. Use tiny size caps. NO withdraw permission on API keys.

## Features

- Screening top-N symbols by 24h volume
- Filters: liquidity, spread, volatility, funding rate
- Indicators: RSI, MACD, EMA, ATR, Bollinger Bands (via `pandas-ta`)
- News aggregation: RSS (CoinDesk, CoinTelegraph), NewsAPI, CryptoPanic, Twitter/X via snscrape
- Claude Opus 4.7 brain (via `gh copilot`) for decision making — JSON in / JSON out
- Risk guards: per-trade cap, max positions, max leverage, daily loss halt, drawdown kill switch
- Idempotent order executor with SL/TP
- SQLite storage for signals, trades, news cache
- Telegram notifications

## Quick Start

```bash
# 1. Clone + setup
python3 -m venv .venv
source .venv/bin/activate
pip install -e ".[dev]"

# 2. Config
cp .env.example .env
# Edit .env with your Bybit API keys (testnet first!)

# 3. Run (paper mode by default)
trading-bot
```

## Mobile App (Android)

The bot has a native Android companion app. See [`android/`](./android) for source.

### Architecture
```
[Android APK] ──HTTPS──▶ [Cloudflare Tunnel / ngrok] ──▶ [FastAPI on VPS:8080] ──▶ [Bot + SQLite]
```

### 1. Run the API server alongside the bot
```bash
# Starts on 0.0.0.0:8080 — prints a bearer token on first start (stored in SQLite for next runs)
trading-bot-api
```
Copy the bearer token from the log output — you'll need it to pair the app.

Override port/host via env: `API_HOST=0.0.0.0 API_PORT=8080 trading-bot-api`
Pin your own token: `API_TOKEN=your-long-random-string trading-bot-api`

### 2. Expose the API to the internet
The phone won't reach your VPS on `0.0.0.0:8080` directly. Use a tunnel:

```bash
# Cloudflare Tunnel (free, HTTPS, no account needed for quick tunnel)
cloudflared tunnel --url http://localhost:8080

# Or ngrok
ngrok http 8080
```
Copy the https URL (e.g. `https://foo-bar-baz.trycloudflare.com`) — that's your **Host URL** in the app.

### 3. Get the APK
**Option A — GitHub Actions (recommended):**
1. Push this repo to GitHub
2. Actions tab → "Android APK" workflow → latest run → download the `trading-bot-debug-apk` artifact
3. Unzip → install the `.apk` on your phone (enable "Install from unknown sources")

**Option B — Android Studio:**
1. Open `android/` folder in Android Studio
2. Sync Gradle → Run (plug in phone or use emulator)

### 4. Pair the app
On first launch you'll see the Pairing screen:
- **Host URL:** the https tunnel URL from step 2
- **Bearer token:** the token printed by `trading-bot-api`

Tap **Pair** and you're in.

### Screens (MVP)
- **Dashboard** — equity, today P&L, positions count, status LED, **EMERGENCY KILL SWITCH** (double-confirm, ≤2 taps from open)
- **Positions** — live open positions with entry/mark/leverage/unrealized P&L
- **Control** — pause/resume, force cycle now, **FLATTEN ALL** (double-confirm)
- **Settings** — read-only view of risk caps and mode (edit via server `.env` for now)

## API Keys (all free)

- **Bybit:** https://www.bybit.com → API Management. Permissions: **Read + Derivatives Trade**. NO withdraw. IP whitelist.
- **NewsAPI:** https://newsapi.org (free 100 req/day)
- **CryptoPanic:** https://cryptopanic.com/developers/api (free tier)
- **Telegram bot:** message @BotFather

## Risk Hard Caps (non-negotiable)

| Guard | Default |
|---|---|
| Max risk per trade | 2% equity |
| Max concurrent positions | 5 |
| Max leverage | 3x |
| Margin mode | Isolated |
| Daily loss halt | -5% |
| Drawdown kill switch | -15% |
| SL cooldown after hit | 4h |
| Funding rate gate | |f| > 0.1% skip |

## Project Structure

See `plan.md` in session folder for full architecture.

## License

Private. Not financial advice.
