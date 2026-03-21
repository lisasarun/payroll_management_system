-- I want to implement role-based features for ADMIN and EMPLOYEE.
-- Please help me design and implement logic for both roles.
-- ============================================================
--  PMS — Payroll Management System | PostgreSQL Setup
--  HOW TO RUN:
--  if you have IntelliJ IDEA Ultimate open Db
--  Click + Data Soure -> PostgreSQl
--
--  LOGIN CREDENTIALS (after running this script):
--    Admin   : Sokha / Sokha@123
--    Admin   : Vanna / Vanna@123
--    Employee: chantha@pms.com  / Chantha@123  (Chantha Dara)
--    Employee: piseth@pms.com   / Piseth@123   (Piseth Rith)
--    Employee: sreynang@pms.com / Sreynang@123 (Sreynang Chann)
--    Employee: bopha@pms.com    / Bopha@123    (Bopha Sok)
--    Employee: vicheka@pms.com  / Vicheka@123  (Vicheka Lim)
-- ============================================================

DROP TABLE IF EXISTS leave_request CASCADE;
DROP TABLE IF EXISTS bonus         CASCADE;
DROP TABLE IF EXISTS payroll       CASCADE;
DROP TABLE IF EXISTS performance   CASCADE;
DROP TABLE IF EXISTS attendance    CASCADE;
DROP TABLE IF EXISTS employees     CASCADE;
DROP TABLE IF EXISTS admins        CASCADE;
DROP TABLE IF EXISTS departments   CASCADE;
DROP TABLE IF EXISTS position_salary_rules CASCADE;

-- ============================================================
-- POSITION_SALARY_RULES TABLE
-- Defines minimum and maximum base salary per position
-- ============================================================
CREATE TABLE position_salary_rules (
    position            VARCHAR(100) PRIMARY KEY,
    min_salary          DECIMAL(12,2) NOT NULL CHECK (min_salary >= 0),
    max_salary          DECIMAL(12,2) NOT NULL CHECK (max_salary >= min_salary),
    created_at          TIMESTAMP DEFAULT NOW(),
    CONSTRAINT valid_salary_range CHECK (max_salary > min_salary)
);

-- ============================================================
-- DEPARTMENTS TABLE
-- Defines valid departments in the organization
-- ============================================================
CREATE TABLE departments (
    department_name     VARCHAR(100) PRIMARY KEY,
    description         TEXT,
    created_at          TIMESTAMP DEFAULT NOW()
);

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
    password      VARCHAR(512)  NOT NULL,
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    base_salary   DECIMAL(12,2) NOT NULL DEFAULT 0
        CHECK (base_salary >= 0),
    position      VARCHAR(100)  REFERENCES position_salary_rules(position) ON UPDATE CASCADE,
    department    VARCHAR(100)  REFERENCES departments(department_name) ON UPDATE CASCADE,
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

-- Position salary rules (MUST be inserted first - referenced by employees)
INSERT INTO position_salary_rules (position, min_salary, max_salary) VALUES
    ('Junior Developer', 500.00, 1500.00),
    ('Senior Developer', 2000.00, 5000.00),
    ('Software Engineer', 1000.00, 3000.00),
    ('HR Coordinator', 800.00, 2000.00),
    ('Marketing Specialist', 700.00, 1800.00),
    ('Financial Analyst', 1200.00, 3000.00),
    ('Manager', 3000.00, 8000.00);

-- Departments (MUST be inserted first - referenced by employees)
INSERT INTO departments (department_name, description) VALUES
    ('Engineering', 'Software development and technical teams'),
    ('Human Resources', 'Employee relations, recruitment, and HR management'),
    ('Marketing', 'Marketing campaigns, branding, and communications'),
    ('Finance', 'Financial planning, accounting, and analysis'),
    ('Operations', 'Business operations and process management'),
    ('Sales', 'Sales teams and customer acquisition'),
    ('IT Support', 'Technical support and IT infrastructure');

-- Admin accounts
-- Sokha@123 -> da09a75ad858c51b180c0aa6eccf1bd1465c5462e6de2ee8d81c216c97ce8799
-- Vanna@123 -> ed6d032129aa3ba43709a36374c16314129b4ae2ae1e05f38e2b419d28fef6ad
INSERT INTO admins (username, password, permission_level) VALUES
    ('Sokha', 'da09a75ad858c51b180c0aa6eccf1bd1465c5462e6de2ee8d81c216c97ce8799', 'SUPER_ADMIN'),
    ('Vanna', 'ed6d032129aa3ba43709a36374c16314129b4ae2ae1e05f38e2b419d28fef6ad', 'HR_MANAGER');

-- Employee accounts (passwords are SHA-256 hashes)
-- Chantha@123  -> 71c29f0e57ead50848bee1cd578947fa9c2ddd6dd765e50a7d530c5e724f3b84
-- Piseth@123   -> d35b134cf8b4d00667d7eca705ab8ccecb94bdda4769468dc1e5cf4ef95b16cf
-- Sreynang@123 -> a6619b7bad4162f0832e07f10a9f541b92cc6cf2856c6fc4422538a2bc27ddb1
-- Bopha@123    -> 15717da99859f3909da7acd20493f9fb1f815627a9b7ea44ce9d803087087b27
-- Vicheka@123  -> c3e5958923288d1ce56f3c5ff74315760a7cbb8aedd29a3807fdda2c9df12993
INSERT INTO employees (full_name, email, password, is_active, base_salary, position, department, hire_date) VALUES
    ('Chantha Dara',   'chantha@pms.com',  '71c29f0e57ead50848bee1cd578947fa9c2ddd6dd765e50a7d530c5e724f3b84', TRUE, 3000.00, 'Software Engineer', 'Engineering', CURRENT_DATE - 365),
    ('Piseth Rith',    'piseth@pms.com',   'd35b134cf8b4d00667d7eca705ab8ccecb94bdda4769468dc1e5cf4ef95b16cf', TRUE, 2500.00, 'Marketing Specialist', 'Marketing', CURRENT_DATE - 180),
    ('Sreynang Chann', 'sreynang@pms.com', 'a6619b7bad4162f0832e07f10a9f541b92cc6cf2856c6fc4422538a2bc27ddb1', TRUE, 3500.00, 'Senior Developer', 'Engineering', CURRENT_DATE - 730),
    ('Bopha Sok',      'bopha@pms.com',    '15717da99859f3909da7acd20493f9fb1f815627a9b7ea44ce9d803087087b27', TRUE, 2800.00, 'HR Coordinator', 'Human Resources', CURRENT_DATE - 90),
    ('Vicheka Lim',    'vicheka@pms.com',  'c3e5958923288d1ce56f3c5ff74315760a7cbb8aedd29a3807fdda2c9df12993', TRUE, 3200.00, 'Financial Analyst', 'Finance', CURRENT_DATE - 540);

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
