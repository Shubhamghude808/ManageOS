CREATE UNIQUE INDEX cafes_name_case_insensitive_key ON cafes (lower(name));

ALTER TABLE gaming_pcs DROP CONSTRAINT IF EXISTS gaming_pcs_branch_id_name_key;
CREATE UNIQUE INDEX gaming_pcs_branch_name_case_insensitive_key
  ON gaming_pcs (branch_id, lower(name));
