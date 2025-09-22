-- Add to_user_id column to transactions table for transfer support
ALTER TABLE transactions ADD COLUMN to_user_id BIGINT;

-- Add foreign key constraint for to_user_id
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_to_user
    FOREIGN KEY (to_user_id) REFERENCES users(id) ON DELETE SET NULL;

-- Add TRANSFER to transaction type enum
ALTER TABLE transactions ALTER COLUMN type TYPE VARCHAR(10);

-- Update constraint to include TRANSFER
ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_type_check;
ALTER TABLE transactions ADD CONSTRAINT transactions_type_check
    CHECK (type IN ('DEBIT', 'CREDIT', 'TRANSFER'));

-- Add index for to_user_id for better performance
CREATE INDEX idx_transactions_to_user_id ON transactions(to_user_id);

-- Add composite index for transfer queries
CREATE INDEX idx_transactions_user_to_user ON transactions(user_id, to_user_id)
    WHERE to_user_id IS NOT NULL;