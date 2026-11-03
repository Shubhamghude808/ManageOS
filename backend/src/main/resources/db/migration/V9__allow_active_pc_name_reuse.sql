DROP INDEX IF EXISTS gaming_pcs_branch_name_case_insensitive_key;
CREATE UNIQUE INDEX gaming_pcs_branch_name_case_insensitive_key
  ON gaming_pcs(branch_id, lower(name))
  WHERE deleted = false;
