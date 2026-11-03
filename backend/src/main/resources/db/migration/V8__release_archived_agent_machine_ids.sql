DROP INDEX IF EXISTS gaming_pcs_agent_machine_id_idx;
UPDATE gaming_pcs SET agent_machine_id = NULL WHERE deleted = true;
CREATE UNIQUE INDEX gaming_pcs_agent_machine_id_idx
  ON gaming_pcs(agent_machine_id)
  WHERE agent_machine_id IS NOT NULL AND deleted = false;
