# API specification (planned contract)

Base path: `/api`. JSON uses camelCase, UUID strings and ISO-8601 UTC timestamps. Errors use `{success:false,data:null,error:{code,message},timestamp}`. Implemented routes include health, branch setup, PC inventory, agent tokens/heartbeats, hourly pricing, session lifecycle, and customer management. Payment, reports, JWT and WebSocket routes remain planned.

## Authentication and access

Planned endpoints: `POST /auth/login`, `/auth/refresh`, `/auth/logout`. Access JWTs will carry user, cafe and role claims; refresh tokens must be rotated and revocable. Roles: OWNER, MANAGER, RECEPTIONIST. The current staff APIs use Spring Security's development HTTP Basic account configured from root `.env` (`SPRING_SECURITY_USER_NAME`, `SPRING_SECURITY_USER_PASSWORD`). This single account has no CafeOS roles or branch scope and is not the production auth design.

## CORS and frontend proxy

The backend uses `setAllowedOriginPatterns` (not `setAllowedOrigins`) to permit `localhost`, `127.0.0.1`, and all LAN ranges: `192.168.*.*`, `10.*.*.*`, and `172.*.*.*`. `allowCredentials: true` is set. The frontend calls all backend routes via the relative path `/api`; the Vite dev server proxies `/api` to `localhost:8080` on the server side. This means no hardcoded LAN IP is required in the frontend, and the app works identically at `localhost:5173`, any LAN IP, and VS Code Dev Tunnel URLs without CORS issues.

## Initial resource routes

| Method | Route | Purpose |
|---|---|---|
| GET | `/health` | Backend application health |
| GET | `/branches` | List configured branches |
| POST | `/branches` | Create/reuse cafe and create a branch |
| GET | `/pcs?branchId={uuid}` | List PCs for a branch |
| POST | `/pcs` | Register a PC in a branch |
| PUT | `/pcs/{id}` | Rename a PC or set AVAILABLE/OFFLINE/MAINTENANCE |
| DELETE | `/pcs/{id}` | Remove a PC; PCs with session history are archived |
| POST | `/pcs/{id}/agent-token` | Issue or rotate a device bearer token; shown once, `Cache-Control: no-store` |
| POST | `/agent/enroll` | Public first-run enrollment; reuses a machine/hostname match or creates a PC automatically in the supplied branch, or the only branch |
| POST | `/agent/register` | Bind authenticated agent hostname to its token-bound PC |
| POST | `/agent/heartbeat` | Store authenticated CPU/memory heartbeat |
| GET | `/agent/session` | Return the authenticated PC's current open session, or null |
| GET | `/pricing-plans?branchId={uuid}` | List branch hourly rate plans |
| POST | `/pricing-plans` | Create a branch hourly rate |
| GET | `/sessions?branchId={uuid}` | List branch session history and live remaining time |
| POST | `/sessions` | Start on an AVAILABLE PC with a pricing plan and allocated seconds; optionally attach customer name/phone |
| POST | `/sessions/{id}/pause` | Pause the authoritative timer |
| POST | `/sessions/{id}/resume` | Resume the authoritative timer |
| POST | `/sessions/{id}/extend` | Add allocated seconds to the same session; dashboard action sends 1800 seconds (30 minutes) |
| POST | `/sessions/{id}/end` | Complete session and calculate amount due |
| GET | `/customers` | List customers (**implemented**) |
| POST | `/customers` | Create a customer record (**implemented**) |
| GET/POST | `/sessions/{id}/payments` | Read/record idempotent payment |
| GET | `/reports/daily?date=YYYY-MM-DD` | Daily revenue and utilization |

## Session contract notes

Start accepts `pcId`, `pricingPlanId`, `allocatedSeconds` (60 seconds to 24 hours), and optionally `customerName` and `customerPhone`. When `customerName`/`customerPhone` are provided, the server auto-provisions a Customer record on first use (matched by phone number) and links it to the session. The session DTO response includes `customerId`, `customerName`, and `customerPhone` in addition to the existing session fields. The server snapshots the hourly rate and returns the session ID, status, timestamps, expiry, and remaining seconds. Only an AVAILABLE PC and an active pricing plan from the same branch can be used. The hourly rate is multiplied by actual billable seconds and rounded to two decimals; paused time is excluded. The session ends early at the receptionist's request or automatically at expiry. End is idempotent after completion and returns the final amount due. Extend changes the existing session allocation, up to 24 hours total. Invalid transitions and unavailable PCs return HTTP 409. PC and session rows are locked for state changes, and a partial unique index is the final concurrent-session guard.

The backend checks for expired ACTIVE sessions every second and recovers overdue sessions after restart using persisted timestamps. Remaining-time values from the API are authoritative; the frontend timer only presents those values between refreshes. The overview dashboard polls session history every five seconds and renders the latest started, paused, and completed sessions as Recent activity. Recording a payment is still planned.

## Customer contract notes

`GET /customers` returns the list of all customer records. `POST /customers` creates a customer directly. Customer records are also auto-provisioned via `POST /sessions` when `customerName` and `customerPhone` are supplied; the server matches on phone number to avoid duplicates. The `Customer` entity and `CustomerRepository` are fully implemented; the `customers` table is live and `gaming_sessions.customer_id` is an active FK.

## Device and realtime contract

Agent enrollment is a public first-run operation for the pilot. Request body: `{machineId, hostname, os, ipAddress, agentVersion, branchId?}`. The server reuses an active PC matching the persistent machine ID or hostname; otherwise it creates a PC in the supplied branch, or in the only configured branch when `branchId` is omitted. If multiple branches exist without a branch ID, enrollment returns `409 CONFLICT`. The response is `201` with `{pcId, token}`; the server stores only the token's SHA-256 hash. The agent stores the token and machine ID in `%LOCALAPPDATA%\CafeOS\agent-state.json` and uses `Authorization: Bearer <token>` for authenticated calls. Registration body: `{hostname, os, ipAddress, agentVersion}`. Heartbeat body: `{agentStatus, agentVersion, cpuPercent, memoryPercent}` where CPU and memory are 0–100. The backend records server receipt time; AVAILABLE or OCCUPIED PCs with no heartbeat for 45 seconds are marked `OFFLINE`. The Python agent sends every 15 seconds with retry/backoff and re-enrolls after HTTP 401/403.

PC list items also return `lastHeartbeatAt` and `agentTokenIssued`; the latter indicates whether a credential exists but never reveals it. Registration stores OS/IP as heartbeat diagnostic details and binds the reported hostname to the token-bound PC.

The agent session display polls `GET /agent/session` and writes a local status snapshot for the logged-in PC-user UI. The response uses the same session fields as the staff session contract, including status, plan, rate, start time, expiry time, remaining seconds, and customer fields. The server remains authoritative; the UI only interpolates the countdown between refreshes.

WebSocket events include `PC_REGISTERED`, `PC_STATUS_UPDATED`, `SESSION_STARTED`, `SESSION_PAUSED`, `SESSION_RESUMED`, `SESSION_EXTENDED`, and `SESSION_COMPLETED`; WebSocket delivery and all session-related contracts remain planned. Clients will reconnect and reconcile from REST after reconnect.

OpenAPI UI is available at `/swagger-ui/index.html`; generated docs are under `/v3/api-docs`.

The frontend asks for the temporary Basic credentials on the Gaming PCs page and keeps them in memory for that browser session. Agent token rotation revokes the previous token. The current agent stores its token locally in `%LOCALAPPDATA%\CafeOS\agent-state.json`; use TLS and OS-managed secret storage before deployment.
