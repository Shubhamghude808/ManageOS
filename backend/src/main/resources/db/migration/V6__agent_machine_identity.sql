ALTER TABLE gaming_pcs ADD COLUMN agent_machine_id VARCHAR(128);
CREATE UNIQUE INDEX gaming_pcs_agent_machine_id_idx
  ON gaming_pcs(agent_machine_id) WHERE agent_machine_id IS NOT NULL;
