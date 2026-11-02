# CafeOS architecture

## Goals and boundaries

The initial deployment is one cafe, with the owner's Windows computer hosting the local application and database. Local operations must remain usable without internet. Cloud services are optional and must never become a dependency for live session timing or billing. The backend is a modular monolith; modules share one PostgreSQL database and are separated by business responsibility.

```mermaid
flowchart LR
  Staff[Owner / Manager / Receptionist] --> UI[React admin dashboard]
  UI -->|REST / JSON| API[Spring Boot modular monolith]
  UI <-->|WebSocket events| API
  API --> DB[(Local PostgreSQL)]
  Agent[Windows Agent process] -->|Bearer token / HTTP heartbeat| API
  Agent <-->|Authenticated WebSocket, planned| API
  Tray[User session tray app] <-->|Local IPC, future| Agent
  API -. outbox sync when available .-> Cloud[Optional CafeOS cloud]
```

## Components

- **Frontend** (`frontend`): staff UI, navigation and presentation. It does not decide authoritative elapsed time or price. TanStack Query owns server state; API calls go through the service layer.
- **Overview activity:** the dashboard reads the same branch session history endpoint used by the session page, refreshes it every five seconds, and presents recent session states. It does not maintain a second activity store.
- **Backend** (`backend`): REST boundary, temporary Basic/device bearer authorization, business services and persistence. Controllers validate DTOs and delegate. Session state changes use transactions and row locks; expiry is recalculated from stored UTC timestamps after restart.
- **PostgreSQL**: authoritative local store. Flyway owns schema changes; Hibernate validates but never creates or updates tables. UUID identifiers, UTC instants and decimal currency values are used.
- **Windows Agent** (`pc-agent`): the current Python process registers a PC and sends authenticated CPU/memory heartbeats over the cafe LAN. Windows Service hosting, tray UI, and WebSocket transport are still planned. Agent countdowns remain presentation only; agents do not lock or restrict Windows.
- **Cloud sync**: future optional outbox consumer. Local writes create durable sync events; no multi-master writes in the MVP.

## Backend module boundaries

`auth` is still a development Basic placeholder; `cafe` and `branch` own tenant and location metadata; `pc` owns inventory and status; `agent` owns device tokens and heartbeat receipt; `pricing` owns hourly rates; `session` owns the authoritative start/pause/resume/extend/end state machine and amount due; `customer`, `payment`, `reports`, `sync`, and `websocket` are planned modules. `common` provides API errors and shared primitives; `config` wires infrastructure. Modules should expose application services rather than depend on each other's persistence internals.

## Reliability and security

The partial unique index on open sessions protects the one-session-per-PC invariant even when requests race. Session commands also need row-level locking or equivalent optimistic concurrency and transactions. Agent tokens are issued for a PC, stored hashed, rotated by re-issuance, and accepted only on agent routes. Staff APIs use a single development HTTP Basic account loaded from the local `.env`; replace it with the planned JWT login/refresh flow and role/branch authorization before deployment. Agent traffic currently uses HTTP on the LAN; TLS, managed secrets, backups, and tested restore procedures are required for deployment.

## Target local and cloud data flow (session flow planned)

```mermaid
sequenceDiagram
  participant Staff
  participant UI
  participant API
  participant DB
  participant Sync as Optional cloud worker
  Staff->>UI: Start session
  UI->>API: POST session command
  API->>DB: Validate, persist state and outbox event atomically
  DB-->>API: Commit
  API-->>UI: Session DTO and authoritative timestamps
  API-->>UI: WebSocket update
  Sync->>DB: Read pending outbox entries
  Sync-->>Sync: Deliver only when internet is available
```

## Decisions

Hourly billing only; Windows 10/11 gaming PCs only; no console support, workstation locking, automated game management, payment gateway or microservices in the MVP. API and deployment details are in [API specification](api-specification.md) and [deployment](deployment.md).
