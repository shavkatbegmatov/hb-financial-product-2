-- Fix type constraint to include TRANSFER
-- Drop any existing type constraints that might prevent TRANSFER

-- Drop the inline check constraint from V2 migration
ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_new_type_check;

-- Drop the named constraint from V4 migration if it exists
ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_type_check;

-- Add the correct constraint with TRANSFER support
ALTER TABLE transactions ADD CONSTRAINT transactions_type_check
    CHECK (type IN ('DEBIT', 'CREDIT', 'TRANSFER'));