# Local development deployment

1. Install Java 21, Maven, Node.js 20 and Docker Compose.
2. Copy `.env.example` to `.env`; set unique database and staff development credentials. The backend imports this root file when started from `backend/`.
3. Start PostgreSQL with `docker compose up -d postgres` and wait for its health check.
4. Start backend from `backend` with `mvn spring-boot:run`. Flyway applies the pending migrations; JPA validates the schema. `application.yml` sets `server.address: 0.0.0.0` so the backend binds to all network interfaces (localhost and LAN).
5. Start frontend from `frontend` with `npm install` then `npm run dev`. The frontend is accessible at `http://localhost:5173`, your LAN IP, or a VS Code Dev Tunnel URL — the Vite dev server proxies `/api` requests to `localhost:8080` on the server side, so no direct backend port exposure is needed for LAN or tunnel access.
6. In **Gaming PCs**, create a cafe/branch and PC. Issue an agent token and configure the Python agent on that PC using the Windows Agent guide. On each gaming PC, run `python -m agent.main` to start the background agent and `python -m agent.ui` to start the **Hardened Kiosk** client. The kiosk locks the screen when idle and shows a Gamer HUD when a session is active.
7. In **Gaming Sessions**, add an hourly rate, select an available PC, and start a session. You may optionally supply a customer name and phone number — the backend auto-creates a customer profile on first use. Pause, resume, end, or extend a session by 30 minutes. The Overview page refreshes Recent activity from the backend every five seconds.

Backend API requests are logged to the backend console with method, path, status code, and duration. Request bodies, authorization headers, and credentials are not logged. Start the backend in a visible terminal to watch requests triggered by the frontend.

Agent communication is currently plaintext HTTP and uses a token stored in the agent's local `.env`; run only on a trusted development LAN. For commercial installation, implement staff login/roles, TLS, Windows Credential Manager/DPAPI, least-privilege OS accounts, firewall rules, encrypted backups and a documented restore drill first. This repository is a foundation, not a production-hardened installer.
