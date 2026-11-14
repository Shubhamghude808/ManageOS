# Development roadmap and TODO

The phases remain in the product's agreed order. Status reflects the repository today; an item marked partial is useful for local development but is not a commercial-ready implementation.

Current agent status: enrollment, registration, heartbeats, protected local
token storage, atomic state, configurable logging, Windows Service and tray
entry points, PyInstaller configuration, authenticated PC-scoped session
status, PC-user countdown display, hardened kiosk with Windows keyboard hooks,
Gamer HUD overlay, Step-Away PIN lock, and `demo_client.py` interactive
simulation are implemented for pilot use. WebSockets, installer automation,
and production enrollment approval remain outstanding.

1. **Foundation — implemented:** modular-monolith backend, Flyway/PostgreSQL, API envelope and errors, health/OpenAPI, React shell, Python agent package, local `.env` configuration.
2. **Authentication — placeholder only:** Spring HTTP Basic with one local development account. CafeOS user provisioning, JWT rotation, roles, and branch authorization remain required.
3. **PC management — implemented for the starter:** cafe/branch setup, PC list/create/update, dashboard inventory, one-time token issuance and token rotation.
4. **Windows Agent — kiosk client implemented:** token-authenticated registration, heartbeat persistence, CPU/memory reporting, reconnect backoff, dashboard last-seen time and 45-second offline marking. Kiosk client (`python -m agent.ui`) implements: Hardened Lock Screen with `WH_KEYBOARD_LL` Windows hooks (blocks WinKey, Alt+Tab, Alt+Esc, Ctrl+Esc, Alt+F4), Gamer HUD overlay with pill mode and audible alerts at 10m/5m/1m, Step-Away PIN lock, staff bypass (`Ctrl+Shift+F12` + admin PIN `7777`), and `demo_client.py` interactive simulation. WebSocket, secure OS credential storage, and installer are outstanding.
5. **Session engine — core implemented:** hourly price plans; transactional start/pause/resume/extend/end; PC/session row locks plus database uniqueness; pause-aware billing; server timestamps; restart-safe expiry checks; a live session dashboard; live overview activity; and a +30-minute extension action. Sessions optionally accept `customerName`/`customerPhone` on start and return `customerId`, `customerName`, `customerPhone` in the response DTO. Integration/concurrency test coverage remains outstanding.
6. **Customer module — implemented:** `Customer` JPA entity and `CustomerRepository` are live. `GET /customers` and `POST /customers` are active endpoints. Customers are auto-provisioned on session start when `customerName`/`customerPhone` is provided; matched by phone number to avoid duplicates. The `customers` table and `gaming_sessions.customer_id` FK are active.
7. **Billing — next:** record/reconcile idempotent manual CASH/UPI payments and invoice-ready records. Session amount due is calculated, but payment collection and invoice generation are not implemented.
8. **Reports and realtime:** daily totals, utilization, WebSocket dashboard updates and reconnect reconciliation.
9. **Local deployment:** Windows packaging, secrets setup, backup/restore and operational diagnostics.
10. **Cloud foundation:** optional outbox delivery, remote administration and backups; local functionality remains independent.
11. **Pilot:** validate workflow and reliability with 3–5 PCs and actual cafe operators.

## Immediate TODO

- [x] Implement the session state machine; enforce one open session per PC with transactions and database constraints.
- [ ] Replace development HTTP Basic with provisioned users, password hashing, JWT refresh rotation, roles and branch scoping before deployment.
- [x] Add unit tests for hashed-token issuance and credential rotation.
- [ ] Add integration tests for bearer authentication, payload validation, persisted metrics and stale detection.
- [ ] Add Testcontainers PostgreSQL migration/invariant and session concurrency tests.
- [x] Surface recent session activity on the overview dashboard with automatic refresh.
- [x] Use a 30-minute session extension action in the dashboard.
- [x] Add Windows Credential Manager/DPAPI token storage with an explicit development-only plaintext fallback.
- [ ] Add bounded heartbeat retention/partitioning and an auditable agent credential revocation workflow.
- [ ] Implement authenticated WebSocket event delivery and REST reconciliation.
- [x] Implement Windows Service, separate user-session tray, PyInstaller spec, and PC-user session display.
- [x] Implement hardened kiosk client with WH_KEYBOARD_LL hooks, Gamer HUD overlay, Step-Away PIN lock, and staff bypass.
- [x] Implement Customer entity, CustomerRepository, and auto-provisioning on session start.
- [x] Vite proxy for universal frontend access (localhost, LAN, Dev Tunnels).
- [x] `server.address: 0.0.0.0` for all-interface backend binding (LAN, tunnel access).
- [ ] Add Inno Setup packaging and automatic Service/tray startup.
- [ ] Document backup restore procedure and production deployment hardening.
