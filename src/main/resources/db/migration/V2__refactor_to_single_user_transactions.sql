-- Refactor transactions table to single user model with DEBIT/CREDIT

-- Add password column to users table if it doesn't exist
ALTER TABLE users ADD COLUMN IF NOT EXISTS password VARCHAR(255);

-- First, create new transactions table with updated structure
CREATE TABLE transactions_new (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    type VARCHAR(10) NOT NULL CHECK (type IN ('DEBIT', 'CREDIT')),
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Migrate existing data from old transaction structure to new structure
-- Convert old transfers to DEBIT/CREDIT transactions
INSERT INTO transactions_new (user_id, amount, type, description, status, created_at, processed_at)
SELECT
    from_user_id as user_id,
    amount,
    'DEBIT' as type,
    COALESCE(description, 'Transfer to user ' || to_user_id) as description,
    status,
    created_at,
    processed_at
FROM transactions
WHERE status = 'COMPLETED';

INSERT INTO transactions_new (user_id, amount, type, description, status, created_at, processed_at)
SELECT
    to_user_id as user_id,
    amount,
    'CREDIT' as type,
    COALESCE(description, 'Transfer from user ' || from_user_id) as description,
    status,
    created_at,
    processed_at
FROM transactions
WHERE status = 'COMPLETED';

-- Drop old transactions table and rename new one
DROP TABLE transactions;
ALTER TABLE transactions_new RENAME TO transactions;

-- Create indexes for new structure
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_status_new ON transactions(status);
CREATE INDEX idx_transactions_created_at_new ON transactions(created_at);
CREATE INDEX idx_transactions_user_type ON transactions(user_id, type);
CREATE INDEX idx_transactions_user_created ON transactions(user_id, created_at);