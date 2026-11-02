ALTER TABLE gaming_pcs ADD COLUMN last_heartbeat_at TIMESTAMPTZ;

CREATE UNIQUE INDEX gaming_pcs_agent_credential_hash_key
  ON gaming_pcs (agent_credential_hash)
  WHERE agent_credential_hash IS NOT NULL;

CREATE INDEX gaming_pcs_stale_heartbeat_idx
  ON gaming_pcs (last_heartbeat_at)
  WHERE status = 'AVAILABLE' AND last_heartbeat_at IS NOT NULL;
