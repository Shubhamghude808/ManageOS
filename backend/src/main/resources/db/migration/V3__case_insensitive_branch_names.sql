ALTER TABLE branches DROP CONSTRAINT IF EXISTS branches_cafe_id_name_key;
CREATE UNIQUE INDEX branches_cafe_name_case_insensitive_key
  ON branches (cafe_id, lower(name));
