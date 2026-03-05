-- ============================================================
--  PMS — Payroll Management System | PostgreSQL Setup
--  Version: 2.0 (Updated 2026-03-04)
--  HOW TO RUN:
--    1. Open pgAdmin
--    2. Right-click "Databases" → Create → Database → name: payroll_db
--    3. Open payroll_db → Tools → Query Tool
--    4. Paste this entire file → Press F5 (Run)
--
--  LOGIN CREDENTIALS (after running this script):
--    Admin   : admin / admin123
--    Admin   : hr    / hr123
--    Employee: alice@pms.com / alice123 (Software Engineer)
--    Employee: bob@pms.com   / bob123   (Marketing Specialist)
--    Employee: carol@pms.com / carol123 (Senior Developer)
--    Employee: david@pms.com / david123 (HR Coordinator)
--    Employee: emma@pms.com  / emma123  (Financial Analyst)
-- ============================================================

DROP TABLE IF EXISTS leave_request CASCADE;
DROP TABLE IF EXISTS bonus         CASCADE;
DROP TABLE IF EXISTS payroll       CASCADE;
DROP TABLE IF EXISTS performance   CASCADE;
DROP TABLE IF EXISTS attendance    CASCADE;
DROP TABLE IF EXISTS employees     CASCADE;
DROP TABLE IF EXISTS admins        CASCADE;

-- ============================================================
-- ADMINS TABLE
-- Stores admin users with permission levels
-- ============================================================
CREATE TABLE admins (
    admin_id         SERIAL       PRIMARY KEY,
    username         VARCHAR(50)  UNIQUE NOT NULL,
    password         VARCHAR(512) NOT NULL, -- Increased for PBKDF2 hash storage
    permission_level VARCHAR(30)  NOT NULL DEFAULT 'ADMIN'
        CHECK (permission_level IN ('SUPER_ADMIN', 'HR_MANAGER', 'ADMIN')),
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    last_login       TIMESTAMP,
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ============================================================
-- EMPLOYEES TABLE
-- Stores employee information and credentials
-- ============================================================
CREATE TABLE employees (
    employee_id   SERIAL        PRIMARY KEY,
    full_name     VARCHAR(100)  NOT NULL,
    email         VARCHAR(100)  UNIQUE NOT NULL
        CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    password      VARCHAR(512)  NOT NULL, -- Increased for PBKDF2 hash storage
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    base_salary   DECIMAL(12,2) NOT NULL DEFAULT 0
        CHECK (base_salary >= 0),
    position      VARCHAR(100),
    department    VARCHAR(100),
    hire_date     DATE          DEFAULT CURRENT_DATE,
    last_login    TIMESTAMP,
    created_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP     DEFAULT NOW()
);

-- ============================================================
-- ATTENDANCE TABLE
-- Tracks employee check-in/out and work hours
-- ============================================================
CREATE TABLE attendance (
    attendance_id       SERIAL       PRIMARY KEY,
    employee_id         INT          NOT NULL REFERENCES employees(employee_id) ON DELETE CASCADE,
    date                DATE         NOT NULL,
    check_in            TIMESTAMP,
    check_out           TIMESTAMP,
    status              VARCHAR(20)  DEFAULT 'PRESENT'
        CHECK (status IN ('PRESENT', 'ABSENT', 'LATE', 'HALF_DAY', 'ON_LEAVE')),
    work_hours          DECIMAL(5,2) CHECK (work_hours >= 0),
    overtime_hours      DECIMAL(5,2) DEFAULT 0 CHECK (overtime_hours >= 0),
    late_minutes        INT          DEFAULT 0 CHECK (late_minutes >= 0),
    early_leave_minutes INT          DEFAULT 0 CHECK (early_leave_minutes >= 0),
    leave_type          VARCHAR(50),
    note                TEXT,
    created_at          TIMESTAMP    DEFAULT NOW(),
    CONSTRAINT uq_attendance UNIQUE (employee_id, date),
    CONSTRAINT check_times CHECK (check_out IS NULL OR check_out >= check_in)
);

-- ============================================================
-- PERFORMANCE TABLE
-- Employee performance reviews and ratings
-- ============================================================
CREATE TABLE performance (
    performance_id SERIAL       PRIMARY KEY,
    employee_id    INT          NOT NULL REFERENCES employees(employee_id) ON DELETE CASCADE,
    review_date    DATE         NOT NULL DEFAULT CURRENT_DATE,
    score          DECIMAL(5,2) NOT NULL CHECK (score > 0 AND score <= 100),
    comments       TEXT,
    reviewer_id    INT          NOT NULL REFERENCES admins(admin_id),
    created_at     TIMESTAMP    DEFAULT NOW()
);

-- ============================================================
-- PAYROLL TABLE
-- Payroll records for each payment period
-- ============================================================
CREATE TABLE payroll (
    payroll_id       SERIAL        PRIMARY KEY,
    employee_id      INT           NOT NULL REFERENCES employees(employee_id) ON DELETE CASCADE,
    pay_period_start DATE          NOT NULL,
    pay_period_end   DATE          NOT NULL,
    base_salary      DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (base_salary >= 0),
    bonus            DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (bonus >= 0),
    deductions       DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (deductions >= 0),
    total_paid       DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (total_paid >= 0),
    payment_date     DATE          NOT NULL DEFAULT CURRENT_DATE,
    created_at       TIMESTAMP     DEFAULT NOW(),
    CONSTRAINT check_payroll_dates CHECK (pay_period_end >= pay_period_start)
);

-- ============================================================
-- BONUS TABLE
-- Bonus payments awarded to employees
-- ============================================================
CREATE TABLE bonus (
    bonus_id     SERIAL        PRIMARY KEY,
    employee_id  INT           NOT NULL REFERENCES employees(employee_id) ON DELETE CASCADE,
    payroll_id   INT           REFERENCES payroll(payroll_id) ON DELETE SET NULL,
    amount       DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    reason       TEXT,
    awarded_date DATE          NOT NULL DEFAULT CURRENT_DATE,
    created_at   TIMESTAMP     DEFAULT NOW()
);

-- ============================================================
-- LEAVE REQUEST TABLE
-- Employee leave/time-off requests
-- ============================================================
CREATE TABLE leave_request (
    leave_request_id SERIAL      PRIMARY KEY,
    employee_id      INT         NOT NULL REFERENCES employees(employee_id) ON DELETE CASCADE,
    start_date       DATE        NOT NULL,
    end_date         DATE        NOT NULL,
    leave_type       VARCHAR(20) NOT NULL
        CHECK (leave_type IN ('SICK', 'VACATION', 'PERSONAL', 'EMERGENCY')),
    reason           TEXT        NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    reviewer_id      INT         REFERENCES admins(admin_id),
    review_note      TEXT,
    request_date     DATE        NOT NULL DEFAULT CURRENT_DATE,
    review_date      DATE,
    created_at       TIMESTAMP   DEFAULT NOW(),
    CONSTRAINT check_leave_dates CHECK (end_date >= start_date)
);

-- ============================================================
-- STORED PROCEDURE: calculate_payroll
-- Calculates and inserts payroll record with validation
-- ============================================================
CREATE OR REPLACE PROCEDURE calculate_payroll(
    p_employee_id  INT,
    p_period_start DATE,
    p_period_end   DATE,
    p_bonus        DECIMAL,
    p_deductions   DECIMAL
)
LANGUAGE plpgsql AS $$
DECLARE
    v_base_salary DECIMAL(12,2);
    v_total_paid  DECIMAL(12,2);
    v_is_active   BOOLEAN;
BEGIN
    -- Validate employee exists and is active
    SELECT base_salary, is_active INTO v_base_salary, v_is_active
    FROM employees WHERE employee_id = p_employee_id;

    IF v_base_salary IS NULL THEN
        RAISE EXCEPTION 'Employee % not found', p_employee_id;
    END IF;

    IF NOT v_is_active THEN
        RAISE EXCEPTION 'Employee % is inactive', p_employee_id;
    END IF;

    -- Validate dates
    IF p_period_end < p_period_start THEN
        RAISE EXCEPTION 'End date must be >= start date';
    END IF;

    -- Validate amounts
    IF p_bonus < 0 OR p_deductions < 0 THEN
        RAISE EXCEPTION 'Bonus and deductions cannot be negative';
    END IF;

    -- Calculate total
    v_total_paid := v_base_salary + p_bonus - p_deductions;

    IF v_total_paid < 0 THEN
        RAISE EXCEPTION 'Total paid cannot be negative (deductions exceed salary + bonus)';
    END IF;

    -- Insert payroll record
    INSERT INTO payroll (employee_id, pay_period_start, pay_period_end,
                         base_salary, bonus, deductions, total_paid, payment_date)
    VALUES (p_employee_id, p_period_start, p_period_end,
            v_base_salary, p_bonus, p_deductions, v_total_paid, CURRENT_DATE);
END;
$$;

-- ============================================================
-- SAMPLE DATA
-- Default admin and employee accounts for testing
-- Passwords stored as SHA-256 hashes (legacy format)
-- ============================================================

-- Admin accounts
-- admin123 -> 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- hr123    -> 070a3b5e8d4bd5c46acccb91c9c54614c0cd649e78c4c4719e3a64270bae5ddf
INSERT INTO admins (username, password, permission_level) VALUES
    ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'SUPER_ADMIN'),
    ('hr',    '070a3b5e8d4bd5c46acccb91c9c54614c0cd649e78c4c4719e3a64270bae5ddf', 'HR_MANAGER');

-- Employee accounts (passwords are SHA-256 hashes)
-- alice123 -> 4e40e8ffe0ee32fa53e139147ed559229a5930f89c2204706fc174beb36210b3
-- bob123   -> 8d059c3640b97180dd2ee453e20d34ab0cb0f2eccbe87d01915a8e578a202b11
-- carol123 -> 6868b751d7c664a9492079bfb2858c86cc95f9480270413e334ed09fe94cf10d
-- david123 -> 0f14089313b20c1723ec1d660b0aaa4f473cf5b321cd370f2d48b7bcf9a7b234
-- emma123  -> 52293754fdbea92ab6c69cd64e644deed1552f40ccd3c1cef9d4d63c754d13e3
INSERT INTO employees (full_name, email, password, is_active, base_salary, position, department, hire_date) VALUES
    ('Alice Johnson', 'alice@pms.com', '4e40e8ffe0ee32fa53e139147ed559229a5930f89c2204706fc174beb36210b3', TRUE, 3000.00, 'Software Engineer', 'Engineering', CURRENT_DATE - 365),
    ('Bob Smith',     'bob@pms.com',   '8d059c3640b97180dd2ee453e20d34ab0cb0f2eccbe87d01915a8e578a202b11', TRUE, 2500.00, 'Marketing Specialist', 'Marketing', CURRENT_DATE - 180),
    ('Carol Davis',   'carol@pms.com', '6868b751d7c664a9492079bfb2858c86cc95f9480270413e334ed09fe94cf10d', TRUE, 3500.00, 'Senior Developer', 'Engineering', CURRENT_DATE - 730),
    ('David Wilson',  'david@pms.com', '0f14089313b20c1723ec1d660b0aaa4f473cf5b321cd370f2d48b7bcf9a7b234', TRUE, 2800.00, 'HR Coordinator', 'Human Resources', CURRENT_DATE - 90),
    ('Emma Thompson', 'emma@pms.com',  '52293754fdbea92ab6c69cd64e644deed1552f40ccd3c1cef9d4d63c754d13e3', TRUE, 3200.00, 'Financial Analyst', 'Finance', CURRENT_DATE - 540);

-- Sample performance reviews
INSERT INTO performance (employee_id, review_date, score, comments, reviewer_id) VALUES
    (1, CURRENT_DATE - 7,  92.00, 'Excellent work this month. Consistently delivers high-quality code.', 1),
    (2, CURRENT_DATE - 7,  81.00, 'Good performance overall. Meeting expectations in all areas.', 1),
    (3, CURRENT_DATE - 7,  88.50, 'Outstanding technical leadership and mentoring skills.', 1),
    (4, CURRENT_DATE - 14, 78.00, 'Good team collaboration and communication skills.', 1),
    (5, CURRENT_DATE - 14, 95.00, 'Exceptional analytical skills. Exceeded all quarterly targets.', 1),
    (1, CURRENT_DATE - 30, 89.50, 'Strong performance on key projects.', 2),
    (3, CURRENT_DATE - 30, 91.00, 'Continues to demonstrate excellent coding standards.', 2);

-- Sample attendance records (past 5 days for testing)
INSERT INTO attendance (employee_id, date, check_in, check_out, status, work_hours, overtime_hours, late_minutes) VALUES
    -- Yesterday
    (1, CURRENT_DATE - 1, (CURRENT_DATE - 1) + TIME '09:00:00', (CURRENT_DATE - 1) + TIME '18:00:00', 'PRESENT', 9.00, 1.00, 0),
    (2, CURRENT_DATE - 1, (CURRENT_DATE - 1) + TIME '08:30:00', (CURRENT_DATE - 1) + TIME '17:30:00', 'PRESENT', 9.00, 1.00, 0),
    (3, CURRENT_DATE - 1, (CURRENT_DATE - 1) + TIME '09:15:00', (CURRENT_DATE - 1) + TIME '18:15:00', 'LATE',    9.00, 1.00, 15),
    (4, CURRENT_DATE - 1, (CURRENT_DATE - 1) + TIME '08:45:00', (CURRENT_DATE - 1) + TIME '17:45:00', 'PRESENT', 9.00, 1.00, 0),
    (5, CURRENT_DATE - 1, (CURRENT_DATE - 1) + TIME '09:00:00', (CURRENT_DATE - 1) + TIME '19:00:00', 'PRESENT', 10.00, 2.00, 0),
    -- 2 days ago
    (1, CURRENT_DATE - 2, (CURRENT_DATE - 2) + TIME '08:55:00', (CURRENT_DATE - 2) + TIME '17:55:00', 'PRESENT', 9.00, 1.00, 0),
    (2, CURRENT_DATE - 2, (CURRENT_DATE - 2) + TIME '08:30:00', (CURRENT_DATE - 2) + TIME '17:00:00', 'PRESENT', 8.50, 0.50, 0),
    (3, CURRENT_DATE - 2, (CURRENT_DATE - 2) + TIME '09:00:00', (CURRENT_DATE - 2) + TIME '18:30:00', 'PRESENT', 9.50, 1.50, 0),
    (4, CURRENT_DATE - 2, (CURRENT_DATE - 2) + TIME '09:30:00', (CURRENT_DATE - 2) + TIME '18:00:00', 'LATE',    8.50, 0.50, 30),
    (5, CURRENT_DATE - 2, (CURRENT_DATE - 2) + TIME '09:00:00', (CURRENT_DATE - 2) + TIME '18:00:00', 'PRESENT', 9.00, 1.00, 0),
    -- 3 days ago
    (1, CURRENT_DATE - 3, (CURRENT_DATE - 3) + TIME '09:00:00', (CURRENT_DATE - 3) + TIME '18:30:00', 'PRESENT', 9.50, 1.50, 0),
    (2, CURRENT_DATE - 3, (CURRENT_DATE - 3) + TIME '08:30:00', (CURRENT_DATE - 3) + TIME '17:30:00', 'PRESENT', 9.00, 1.00, 0),
    (3, CURRENT_DATE - 3, (CURRENT_DATE - 3) + TIME '09:00:00', (CURRENT_DATE - 3) + TIME '18:00:00', 'PRESENT', 9.00, 1.00, 0),
    (5, CURRENT_DATE - 3, (CURRENT_DATE - 3) + TIME '09:00:00', (CURRENT_DATE - 3) + TIME '20:00:00', 'PRESENT', 11.00, 3.00, 0),
    -- 4 days ago
    (1, CURRENT_DATE - 4, (CURRENT_DATE - 4) + TIME '09:00:00', (CURRENT_DATE - 4) + TIME '17:00:00', 'PRESENT', 8.00, 0.00, 0),
    (2, CURRENT_DATE - 4, (CURRENT_DATE - 4) + TIME '08:30:00', (CURRENT_DATE - 4) + TIME '17:30:00', 'PRESENT', 9.00, 1.00, 0),
    (3, CURRENT_DATE - 4, (CURRENT_DATE - 4) + TIME '09:00:00', (CURRENT_DATE - 4) + TIME '18:00:00', 'PRESENT', 9.00, 1.00, 0),
    (4, CURRENT_DATE - 4, (CURRENT_DATE - 4) + TIME '09:00:00', (CURRENT_DATE - 4) + TIME '17:00:00', 'PRESENT', 8.00, 0.00, 0);

SELECT '✓ Setup complete! Database initialized with sample data.' AS status;

-- ============================================================
-- INDEXES
-- Optimized indexes for faster query performance
-- ============================================================
CREATE INDEX idx_admins_username            ON admins(username);
CREATE INDEX idx_employees_email            ON employees(email);
CREATE INDEX idx_employees_is_active        ON employees(is_active);
CREATE INDEX idx_attendance_employee        ON attendance(employee_id);
CREATE INDEX idx_attendance_date            ON attendance(date);
CREATE INDEX idx_attendance_status          ON attendance(status);
CREATE INDEX idx_performance_employee       ON performance(employee_id);
CREATE INDEX idx_performance_reviewer       ON performance(reviewer_id);
CREATE INDEX idx_payroll_employee           ON payroll(employee_id);
CREATE INDEX idx_payroll_period             ON payroll(pay_period_start, pay_period_end);
CREATE INDEX idx_bonus_employee             ON bonus(employee_id);
CREATE INDEX idx_bonus_payroll              ON bonus(payroll_id);
CREATE INDEX idx_leave_request_employee     ON leave_request(employee_id);
CREATE INDEX idx_leave_request_status       ON leave_request(status);
CREATE INDEX idx_leave_request_dates        ON leave_request(start_date, end_date);

-- ============================================================
-- VIEWS (Optional)
-- Useful views for common queries
-- ============================================================

-- View: Active employees with recent performance
CREATE OR REPLACE VIEW v_active_employees AS
SELECT
    e.employee_id,
    e.full_name,
    e.email,
    e.position,
    e.department,
    e.base_salary,
    e.hire_date,
    COALESCE(p.score, 0) as latest_score,
    p.review_date as last_review_date
FROM employees e
LEFT JOIN LATERAL (
    SELECT score, review_date
    FROM performance
    WHERE employee_id = e.employee_id
    ORDER BY review_date DESC
    LIMIT 1
) p ON TRUE
WHERE e.is_active = TRUE;

-- View: Employee payroll summary
CREATE OR REPLACE VIEW v_payroll_summary AS
SELECT
    e.employee_id,
    e.full_name,
    e.email,
    COUNT(p.payroll_id) as total_payrolls,
    SUM(p.total_paid) as total_earnings,
    AVG(p.total_paid) as avg_payment,
    MAX(p.payment_date) as last_payment_date
FROM employees e
LEFT JOIN payroll p ON e.employee_id = p.employee_id
GROUP BY e.employee_id, e.full_name, e.email;

SELECT '✓ All indexes and views created successfully.' AS status;
