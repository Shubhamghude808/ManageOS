# CafeOS

Local-first gaming cafe management starter. Implemented workflows include cafe/branch setup, PC inventory, per-PC agent tokens and heartbeats, hourly pricing, and the core gaming-session lifecycle with pause-aware billing and expiry recovery. Production authentication, payment collection, Windows service/tray, invoices and cloud sync remain future milestones.

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
5. Browse to `http://localhost:5173`; backend health is at `http://localhost:8080/api/health`.

To run sessions, use **Gaming Sessions** to add an hourly rate and start, pause, resume, end, or extend a session by 30 minutes. The Overview page shows recent session activity and refreshes it every five seconds. To enroll a PC, use **Gaming PCs** in the dashboard, issue its one-time agent token, and follow [Windows Agent setup](docs/windows-agent.md). Use an admin-computer LAN IP for the agent's server URL.

The compose service starts PostgreSQL only. Backend configuration defaults support local development; set environment variables for deployments. See `docs/` before extending the schema or API.

## Quality checks

`cd backend; mvn test` · `cd frontend; npm run build` · `cd pc-agent; python -m compileall agent`

See [architecture](docs/architecture.md), [roadmap](docs/development-roadmap.md), and [agent notes](pc-agent/README.md).
