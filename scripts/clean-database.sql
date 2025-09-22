-- Clean database script for development
-- This will drop all tables and reset the schema

-- Drop existing tables if they exist
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS flyway_schema_history CASCADE;

-- Drop indexes if they exist
DROP INDEX IF EXISTS idx_users_username;
DROP INDEX IF EXISTS idx_users_email;
DROP INDEX IF EXISTS idx_transactions_from_user;
DROP INDEX IF EXISTS idx_transactions_to_user;
DROP INDEX IF EXISTS idx_transactions_status;
DROP INDEX IF EXISTS idx_transactions_created_at;
DROP INDEX IF EXISTS idx_transactions_user_id;
DROP INDEX IF EXISTS idx_transactions_type;
DROP INDEX IF EXISTS idx_transactions_status_new;
DROP INDEX IF EXISTS idx_transactions_created_at_new;
DROP INDEX IF EXISTS idx_transactions_user_type;
DROP INDEX IF EXISTS idx_transactions_user_created;