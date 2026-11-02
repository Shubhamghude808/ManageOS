# API specification (planned contract)

Base path: `/api`. JSON uses camelCase, UUID strings and ISO-8601 UTC timestamps. Errors use `{success:false,data:null,error:{code,message},timestamp}`. Implemented routes include health, branch setup, PC inventory, agent tokens/heartbeats, hourly pricing, and session lifecycle. Customer, payment, reports, JWT and WebSocket routes remain planned.

## Authentication and access

Planned endpoints: `POST /auth/login`, `/auth/refresh`, `/auth/logout`. Access JWTs will carry user, cafe and role claims; refresh tokens must be rotated and revocable. Roles: OWNER, MANAGER, RECEPTIONIST. The current staff APIs use Spring Security's development HTTP Basic account configured from root `.env` (`SPRING_SECURITY_USER_NAME`, `SPRING_SECURITY_USER_PASSWORD`). This single account has no CafeOS roles or branch scope and is not the production auth design.

## Initial resource routes

| Method | Route | Purpose |
|---|---|---|
| GET | `/health` | Backend application health |
| GET | `/branches` | List configured branches |
| POST | `/branches` | Create/reuse cafe and create a branch |
| GET | `/pcs?branchId={uuid}` | List PCs for a branch |
| POST | `/pcs` | Register a PC in a branch |
| PUT | `/pcs/{id}` | Rename a PC or set AVAILABLE/OFFLINE/MAINTENANCE |
| POST | `/pcs/{id}/agent-token` | Issue or rotate a device bearer token; shown once, `Cache-Control: no-store` |
| POST | `/agent/register` | Bind authenticated agent hostname to its token-bound PC |
| POST | `/agent/heartbeat` | Store authenticated CPU/memory heartbeat |
| GET | `/pricing-plans?branchId={uuid}` | List branch hourly rate plans |
| POST | `/pricing-plans` | Create a branch hourly rate |
| GET | `/sessions?branchId={uuid}` | List branch session history and live remaining time |
| POST | `/sessions` | Start on an AVAILABLE PC with a pricing plan and allocated seconds |
| POST | `/sessions/{id}/pause` | Pause the authoritative timer |
| POST | `/sessions/{id}/resume` | Resume the authoritative timer |
| POST | `/sessions/{id}/extend` | Add allocated seconds to the same session; dashboard action sends 1800 seconds (30 minutes) |
| POST | `/sessions/{id}/end` | Complete session and calculate amount due |
| GET/POST | `/customers` | List/create customer |
| GET/POST | `/sessions/{id}/payments` | Read/record idempotent payment |
| GET | `/reports/daily?date=YYYY-MM-DD` | Daily revenue and utilization |

## Session contract notes

Start accepts `pcId`, `pricingPlanId`, and `allocatedSeconds` (60 seconds to 24 hours). The server snapshots the hourly rate and returns the session ID, status, timestamps, expiry, and remaining seconds. Only an AVAILABLE PC and an active pricing plan from the same branch can be used. The hourly rate is multiplied by actual billable seconds and rounded to two decimals; paused time is excluded. The session ends early at the receptionist's request or automatically at expiry. End is idempotent after completion and returns the final amount due. Extend changes the existing session allocation, up to 24 hours total. Invalid transitions and unavailable PCs return HTTP 409. PC and session rows are locked for state changes, and a partial unique index is the final concurrent-session guard.

The backend checks for expired ACTIVE sessions every second and recovers overdue sessions after restart using persisted timestamps. Remaining-time values from the API are authoritative; the frontend timer only presents those values between refreshes. The overview dashboard polls session history every five seconds and renders the latest started, paused, and completed sessions as Recent activity. Recording a payment is still planned.

## Device and realtime contract

Agent enrollment is implemented as an owner-authenticated token issuance operation for a known PC. The server generates a 256-bit random token, stores only its SHA-256 hash, and returns the plaintext once. The agent authenticates using `Authorization: Bearer <token>`; the authenticated token determines its PC ID. Registration body: `{hostname, os, ipAddress, agentVersion}`. Heartbeat body: `{agentStatus, agentVersion, cpuPercent, memoryPercent}` where CPU and memory are 0–100. The backend records server receipt time; PCs with no heartbeat for 45 seconds are marked `OFFLINE`. The Python agent sends every 15 seconds with retry/backoff.

PC list items also return `lastHeartbeatAt` and `agentTokenIssued`; the latter indicates whether a credential exists but never reveals it. Registration stores OS/IP as heartbeat diagnostic details and binds the reported hostname to the token-bound PC.

WebSocket events include `PC_REGISTERED`, `PC_STATUS_UPDATED`, `SESSION_STARTED`, `SESSION_PAUSED`, `SESSION_RESUMED`, `SESSION_EXTENDED`, and `SESSION_COMPLETED`; WebSocket delivery and all session-related contracts remain planned. Clients will reconnect and reconcile from REST after reconnect.

OpenAPI UI is available at `/swagger-ui/index.html`; generated docs are under `/v3/api-docs`.

The frontend asks for the temporary Basic credentials on the Gaming PCs page and keeps them in memory for that browser session. Agent token rotation revokes the previous token. The agent stores its token in a local `.env` during this pilot skeleton; use TLS and OS-managed secret storage before deployment.
