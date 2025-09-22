-- Insert sample data for testing

-- Insert sample users
INSERT INTO users (username, email, full_name, password, balance) VALUES
('john_doe', 'john@example.com', 'John Doe', '$2a$10$example.hash.for.password123', 1000.00),
('jane_smith', 'jane@example.com', 'Jane Smith', '$2a$10$example.hash.for.password456', 1500.50),
('bob_johnson', 'bob@example.com', 'Bob Johnson', '$2a$10$example.hash.for.password789', 750.25),
('alice_brown', 'alice@example.com', 'Alice Brown', '$2a$10$example.hash.for.password000', 2000.00),
('charlie_davis', 'charlie@example.com', 'Charlie Davis', '$2a$10$example.hash.for.password111', 500.75);

-- Insert sample transactions
INSERT INTO transactions (user_id, amount, type, description, status, created_at, processed_at) VALUES
-- John's transactions
(1, 200.00, 'CREDIT', 'Salary deposit', 'COMPLETED', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
(1, 50.00, 'DEBIT', 'Grocery shopping', 'COMPLETED', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
(1, 100.00, 'DEBIT', 'Utility bill payment', 'COMPLETED', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
(1, 25.00, 'CREDIT', 'Cashback reward', 'COMPLETED', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),

-- Jane's transactions
(2, 300.00, 'CREDIT', 'Freelance payment', 'COMPLETED', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
(2, 75.00, 'DEBIT', 'Restaurant bill', 'COMPLETED', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(2, 150.00, 'DEBIT', 'Online shopping', 'COMPLETED', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),

-- Bob's transactions
(3, 500.00, 'CREDIT', 'Bonus payment', 'COMPLETED', NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days'),
(3, 200.00, 'DEBIT', 'Car maintenance', 'COMPLETED', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
(3, 30.00, 'DEBIT', 'Coffee subscription', 'COMPLETED', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),

-- Alice's transactions
(4, 1000.00, 'CREDIT', 'Investment return', 'COMPLETED', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
(4, 250.00, 'DEBIT', 'Medical expenses', 'COMPLETED', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
(4, 80.00, 'DEBIT', 'Gym membership', 'COMPLETED', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),

-- Charlie's transactions
(5, 400.00, 'CREDIT', 'Part-time job', 'COMPLETED', NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days'),
(5, 120.00, 'DEBIT', 'Phone bill', 'COMPLETED', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(5, 45.00, 'DEBIT', 'Movie tickets', 'COMPLETED', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),

-- Some pending transactions
(1, 75.00, 'DEBIT', 'Pending online order', 'PENDING', NOW(), NULL),
(2, 200.00, 'CREDIT', 'Pending refund', 'PENDING', NOW(), NULL);