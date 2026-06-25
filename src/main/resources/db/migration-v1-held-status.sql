-- ============================================================
-- SI-BLO DB migration v1 — HELD status + held_at column
-- Run ONCE per PostgreSQL environment (Laragon local, Railway, etc.)
-- when:
--   - DataSeeder / booking fails with: violates check constraint "time_slots_status_check" ... HELD
--   - OR table was created before HELD status existed in the app
--
-- Note: ddl-auto=update does NOT alter existing CHECK constraints.
-- Fresh Hibernate-only DB (no CHECK) usually does not need step 1–2.
-- ============================================================

-- Step 1: Allow HELD in status CHECK constraint (required on old schemas)
ALTER TABLE time_slots DROP CONSTRAINT IF EXISTS time_slots_status_check;

ALTER TABLE time_slots ADD CONSTRAINT time_slots_status_check
  CHECK (status IN ('AVAILABLE', 'HELD', 'BOOKED', 'BLOCKED'));

-- Step 2: Optional column for held-slot expiry tracking (if entity adds held_at)
ALTER TABLE time_slots ADD COLUMN IF NOT EXISTS held_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_time_slots_held_at ON time_slots(held_at) WHERE status = 'HELD';
