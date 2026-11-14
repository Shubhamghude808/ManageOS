# CafeOS

Local-first gaming cafe management system. Covers cafe/branch setup, PC inventory, per-PC agent tokens and heartbeats, hourly pricing, and the full gaming-session lifecycle with pause-aware billing, expiry recovery, and customer profile linking. Each gaming PC runs a **Hardened Kiosk client** that locks the screen when idle and shows an overlay HUD during active sessions. Production authentication, payment collection, invoices, and cloud sync remain future milestones.

## Prerequisites

- Java 21 and Maven 3.9+
- Node.js 20+
- Python 3.12
- Docker Compose

## Start locally

1. Copy `.env.example` to `.env` and set local development credentials. Backend startup from `backend/` imports `../.env`.
2. Start PostgreSQL: `docker compose up -d postgres`
3. Backend: `cd backend; mvn spring-boot:run`
4. Frontend: `cd frontend; npm install; npm run dev`
5. Browse to `http://localhost:5173` — also works on your LAN IP and VS Code Dev Tunnel URLs; the Vite proxy routes `/api` to `localhost:8080` so no hardcoded IP is needed. Backend health: `http://localhost:8080/api/health`.

To run sessions, use **Gaming Sessions** to add an hourly rate, then start, pause, resume, end, or extend a session. Optionally supply a customer name and phone number on session start to auto-create a customer profile. The Overview page shows recent session activity and refreshes every five seconds. To enroll a PC, use **Gaming PCs** in the dashboard, issue its one-time agent token, and follow [Windows Agent setup](docs/windows-agent.md).

The compose service starts PostgreSQL only. Backend binds to `0.0.0.0` (all interfaces) via `server.address: 0.0.0.0` in `application.yml`. See `docs/` before extending the schema or API.

## Windows Kiosk Client (`pc-agent`)

Each gaming PC runs two processes:

| Process | Command | Role |
|---|---|---|
| Background agent | `python -m agent.main` | Heartbeat, session sync, token auth |
| Kiosk UI | `python -m agent.ui` | Hardened lock screen + Gamer HUD |

The kiosk UI has three states:

- **Hardened Lock Screen** — fullscreen, low-level `WH_KEYBOARD_LL` hooks block Win key, Alt+Tab, Alt+Esc, Ctrl+Esc, and Alt+F4.
- **Gamer HUD** — overlay countdown with audible alerts at 10 min, 5 min, and 1 min remaining; collapses to a pill when the gamer is active.
- **Step-Away Lock** — PIN-protected, triggered automatically on idle.

Staff bypass: `Ctrl+Shift+F12` + admin PIN `7777`. An interactive demo is available via `python pc-agent/demo_client.py`.

## Quality checks

`cd backend; mvn test` · `cd frontend; npm run build` · `cd pc-agent; python -m compileall agent` · `python -m agent.ui` (smoke-test kiosk UI launch)

See [architecture](docs/architecture.md), [roadmap](docs/development-roadmap.md), [API spec](docs/api-specification.md), and [Windows Agent setup](docs/windows-agent.md).
