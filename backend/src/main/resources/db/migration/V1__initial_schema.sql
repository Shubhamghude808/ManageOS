CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE cafes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(160) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE branches (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  cafe_id UUID NOT NULL REFERENCES cafes(id),
  name VARCHAR(160) NOT NULL,
  timezone VARCHAR(64) NOT NULL DEFAULT 'Asia/Kolkata',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (cafe_id, name)
);
CREATE TABLE app_users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  cafe_id UUID NOT NULL REFERENCES cafes(id),
  email VARCHAR(254) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(24) NOT NULL CHECK (role IN ('OWNER','MANAGER','RECEPTIONIST')),
  active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (cafe_id, email)
);
CREATE TABLE gaming_pcs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  branch_id UUID NOT NULL REFERENCES branches(id),
  name VARCHAR(100) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE','OCCUPIED','OFFLINE','MAINTENANCE')),
  hostname VARCHAR(255),
  agent_credential_hash VARCHAR(255),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (branch_id, name)
);
CREATE TABLE pc_heartbeats (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  pc_id UUID NOT NULL REFERENCES gaming_pcs(id),
  received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  agent_version VARCHAR(40),
  cpu_percent NUMERIC(5,2),
  memory_percent NUMERIC(5,2),
  details JSONB NOT NULL DEFAULT '{}'::jsonb
);
CREATE INDEX pc_heartbeats_pc_received_idx ON pc_heartbeats(pc_id, received_at DESC);
CREATE TABLE customers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  branch_id UUID NOT NULL REFERENCES branches(id),
  name VARCHAR(160) NOT NULL,
  phone VARCHAR(32),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE pricing_plans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  branch_id UUID NOT NULL REFERENCES branches(id),
  name VARCHAR(120) NOT NULL,
  hourly_rate NUMERIC(12,2) NOT NULL CHECK (hourly_rate >= 0),
  currency CHAR(3) NOT NULL DEFAULT 'INR',
  active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE gaming_sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  branch_id UUID NOT NULL REFERENCES branches(id),
  pc_id UUID NOT NULL REFERENCES gaming_pcs(id),
  customer_id UUID REFERENCES customers(id),
  pricing_plan_id UUID NOT NULL REFERENCES pricing_plans(id),
  status VARCHAR(16) NOT NULL CHECK (status IN ('ACTIVE','PAUSED','COMPLETED','CANCELLED')),
  hourly_rate NUMERIC(12,2) NOT NULL CHECK (hourly_rate >= 0),
  allocated_seconds BIGINT NOT NULL CHECK (allocated_seconds > 0),
  started_at TIMESTAMPTZ NOT NULL,
  paused_at TIMESTAMPTZ,
  accumulated_paused_seconds BIGINT NOT NULL DEFAULT 0 CHECK (accumulated_paused_seconds >= 0),
  ended_at TIMESTAMPTZ,
  amount_due NUMERIC(12,2),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX gaming_sessions_one_open_per_pc ON gaming_sessions(pc_id) WHERE status IN ('ACTIVE','PAUSED');
CREATE INDEX gaming_sessions_branch_started_idx ON gaming_sessions(branch_id, started_at DESC);
CREATE TABLE payments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  session_id UUID NOT NULL REFERENCES gaming_sessions(id),
  amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
  method VARCHAR(12) NOT NULL CHECK (method IN ('CASH','UPI','OTHER')),
  idempotency_key VARCHAR(128) NOT NULL UNIQUE,
  reference VARCHAR(160),
  received_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE sync_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  aggregate_type VARCHAR(64) NOT NULL,
  aggregate_id UUID NOT NULL,
  event_type VARCHAR(100) NOT NULL,
  payload JSONB NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  published_at TIMESTAMPTZ
);
CREATE INDEX sync_events_pending_idx ON sync_events(created_at) WHERE published_at IS NULL;
