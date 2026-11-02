# Database model

All primary keys are UUIDs. Timestamps use `TIMESTAMPTZ` and are stored in UTC. Money uses `NUMERIC(12,2)`. Schema changes are forward-only Flyway migrations.

```mermaid
erDiagram
  CAFE ||--o{ BRANCH : owns
  CAFE ||--o{ APP_USER : has
  BRANCH ||--o{ GAMING_PC : contains
  BRANCH ||--o{ CUSTOMER : serves
  BRANCH ||--o{ PRICING_PLAN : configures
  BRANCH ||--o{ GAMING_SESSION : records
  GAMING_PC ||--o{ PC_HEARTBEAT : reports
  GAMING_PC ||--o{ GAMING_SESSION : hosts
  CUSTOMER o|--o{ GAMING_SESSION : associates
  PRICING_PLAN ||--o{ GAMING_SESSION : snapshots
  GAMING_SESSION ||--o{ PAYMENT : receives
  CAFE { uuid id PK string name }
  BRANCH { uuid id PK uuid cafe_id FK string name string timezone }
  APP_USER { uuid id PK uuid cafe_id FK string email string password_hash string role }
  GAMING_PC { uuid id PK uuid branch_id FK string name string status string agent_credential_hash timestamptz last_heartbeat_at }
  PC_HEARTBEAT { uuid id PK uuid pc_id FK timestamptz received_at string agent_version numeric cpu_percent numeric memory_percent jsonb details }
  CUSTOMER { uuid id PK uuid branch_id FK string name string phone }
  PRICING_PLAN { uuid id PK uuid branch_id FK numeric hourly_rate string currency boolean active }
  GAMING_SESSION { uuid id PK uuid branch_id FK uuid pc_id FK uuid customer_id FK uuid pricing_plan_id FK string status numeric hourly_rate bigint allocated_seconds timestamptz started_at timestamptz paused_at bigint accumulated_paused_seconds timestamptz ended_at numeric amount_due }
  PAYMENT { uuid id PK uuid session_id FK numeric amount string method string idempotency_key UK timestamptz received_at }
  SYNC_EVENT { uuid id PK string aggregate_type uuid aggregate_id string event_type jsonb payload timestamptz created_at timestamptz published_at }
```

## Invariants and access paths

- A partial unique index on `gaming_sessions(pc_id)` applies while status is `ACTIVE` or `PAUSED`; it prevents two concurrent open sessions on one PC.
- Session `hourly_rate` is a snapshot so later rate changes cannot rewrite history. Active/paused session command logic must subtract accumulated pauses from elapsed seconds; authoritative expiry belongs to backend timestamps, not client timers.
- Payment `idempotency_key` is globally unique for the initial single-cafe deployment. A duplicate key returns the original payment result after the service verifies the request matches.
- Foreign keys bind each operational row to its branch, PC or session. Reports use `(branch_id, started_at)` and heartbeat history uses `(pc_id, received_at DESC)` indexes.
- `sync_events` is an outbox; event creation must share a transaction with the business write.
- Agent bearer secrets are generated with 256 bits of randomness; only a SHA-256 hex digest is stored in `gaming_pcs.agent_credential_hash`. A partial unique index protects token uniqueness.
- `gaming_pcs.last_heartbeat_at` stores server receipt time. The scheduled liveness check marks `AVAILABLE` PCs `OFFLINE` when their last heartbeat is older than 45 seconds; it does not override `MAINTENANCE` or `OCCUPIED`.

Migrations V1–V4 create the foundation schema, case-insensitive cafe/branch/PC name indexes, and agent heartbeat support. Application-level session state transition validation and heartbeat retention/partitioning policies remain future work.
