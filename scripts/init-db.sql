-- Database initialization script for HB Financial Product
-- This script runs automatically when PostgreSQL container starts

-- Create the main database if it doesn't exist
-- (The database 'finance_product_2' is already created by POSTGRES_DB env var)

-- Set timezone
SET timezone = 'Asia/Tashkent';

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Grant necessary privileges
GRANT ALL PRIVILEGES ON DATABASE finance_product_2 TO postgres;

-- Log the initialization
DO $$
BEGIN
    RAISE NOTICE 'Database finance_product_2 initialized successfully';
    RAISE NOTICE 'Timezone set to: %', current_setting('timezone');
    RAISE NOTICE 'Current timestamp: %', NOW();
END $$;