ALTER TABLE pricing_plans
  ADD COLUMN duration_minutes INTEGER NOT NULL DEFAULT 60;

ALTER TABLE pricing_plans
  ADD CONSTRAINT pricing_plans_duration_minutes_check CHECK (duration_minutes > 0 AND duration_minutes <= 1440);
