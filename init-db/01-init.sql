-- Initialize database schema (optional, Hibernate will auto-create tables)
-- This file runs when PostgreSQL container is first created

-- Create extension for UUID support
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Grant all privileges to orderuser
GRANT ALL PRIVILEGES ON DATABASE orderdb TO orderuser;

-- Log initialization
SELECT 'Database orderdb initialized successfully' AS status;

