DROP INDEX IF EXISTS gaming_pcs_stale_heartbeat_idx;

CREATE INDEX gaming_pcs_stale_heartbeat_idx
  ON gaming_pcs (last_heartbeat_at)
  WHERE status IN ('AVAILABLE', 'OCCUPIED') AND last_heartbeat_at IS NOT NULL;
