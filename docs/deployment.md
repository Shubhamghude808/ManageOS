# Local development deployment

1. Install Java 21, Maven, Node.js 20 and Docker Compose.
2. Copy `.env.example` to `.env`; set unique database and staff development credentials. The backend imports this root file when started from `backend/`.
3. Start PostgreSQL with `docker compose up -d postgres` and wait for its health check.
4. Start backend from `backend` with `mvn spring-boot:run`. Flyway applies the pending migrations; JPA validates the schema.
5. Start frontend from `frontend` with `npm install` then `npm run dev`.
6. In **Gaming PCs**, create a cafe/branch and PC. Issue an agent token and configure the Python agent on that PC using the Windows Agent guide.
7. In **Gaming Sessions**, add an hourly rate, select an available PC, and start a session. Pause, resume, end, or extend it by 30 minutes. The Overview page refreshes Recent activity from the backend every five seconds.

Backend API requests are logged to the backend console with method, path, status code, and duration. Request bodies, authorization headers, and credentials are not logged. Start the backend in a visible terminal to watch requests triggered by the frontend.

Agent communication is currently plaintext HTTP and uses a token stored in the agent's local `.env`; run only on a trusted development LAN. For commercial installation, implement staff login/roles, TLS, Windows Credential Manager/DPAPI, least-privilege OS accounts, firewall rules, encrypted backups and a documented restore drill first. This repository is a foundation, not a production-hardened installer.
