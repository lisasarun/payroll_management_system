-- ============================================================
--  PMS — Payroll Management System
--  PostgreSQL Setup Script
--  Run: psql -U postgres -d payroll_db -f setup.sql
-- ============================================================

-- Drop existing tables (safe re-run)
DROP TABLE IF EXISTS bonus       CASCADE;
DROP TABLE IF EXISTS payroll     CASCADE;
DROP TABLE IF EXISTS performance CASCADE;
DROP TABLE IF EXISTS attendance  CASCADE;
DROP TABLE IF EXISTS employees   CASCADE;
DROP TABLE IF EXISTS admins      CASCADE;

--  ADMINS
CREATE TABLE admins (
    admin_id         SERIAL       PRIMARY KEY,
    username         VARCHAR(50)  UNIQUE NOT NULL,
    password         VARCHAR(255) NOT NULL,
    permission_level VARCHAR(30)  NOT NULL DEFAULT 'ADMIN',
    last_login       TIMESTAMP
);

--  EMPLOYEES
CREATE TABLE employees (
    employee_id SERIAL        PRIMARY KEY,
    full_name   VARCHAR(100)  NOT NULL,
    email       VARCHAR(100)  UNIQUE NOT NULL,
    password    VARCHAR(255)  NOT NULL,
    is_active   BOOLEAN       NOT NULL DEFAULT TRUE,
    base_salary DECIMAL(12,2) NOT NULL DEFAULT 0,
    last_login  TIMESTAMP,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ATTENDANCE
CREATE TABLE attendance (
    attendance_id       SERIAL  PRIMARY KEY,
    employee_id         INT     NOT NULL REFERENCES employees(employee_id) ON DELETE CASCADE,
    date                DATE    NOT NULL,
    check_in            TIMESTAMP,
    check_out           TIMESTAMP,
    status              VARCHAR(20) DEFAULT 'PRESENT',
    work_hours          DECIMAL(5,2),
    overtime_hours      DECIMAL(5,2) DEFAULT 0,
    late_minutes        INT     DEFAULT 0,
    early_leave_minutes INT     DEFAULT 0,
    leave_type          VARCHAR(50),
    note                TEXT,
    CONSTRAINT uq_attendance UNIQUE (employee_id, date)
);

--  PERFORMANCE
CREATE TABLE performance (
    performance_id SERIAL       PRIMARY KEY,
    employee_id    INT          NOT NULL REFERENCES employees(employee_id) ON DELETE CASCADE,
    review_date    DATE         NOT NULL DEFAULT CURRENT_DATE,
    score          DECIMAL(5,2) NOT NULL CHECK (score >= 0 AND score <= 100),
    comments       TEXT,
    reviewer_id    INT          NOT NULL REFERENCES admins(admin_id)
);

-- PAYROLL
CREATE TABLE payroll (
    payroll_id       SERIAL        PRIMARY KEY,
    employee_id      INT           NOT NULL REFERENCES employees(employee_id) ON DELETE CASCADE,
    pay_period_start DATE          NOT NULL,
    pay_period_end   DATE          NOT NULL,
    base_salary      DECIMAL(12,2) NOT NULL DEFAULT 0,
    bonus            DECIMAL(12,2) NOT NULL DEFAULT 0,
    deductions       DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_paid       DECIMAL(12,2) NOT NULL DEFAULT 0,
    payment_date     DATE          NOT NULL DEFAULT CURRENT_DATE
);

--  BONUS
CREATE TABLE bonus (
    bonus_id     SERIAL        PRIMARY KEY,
    employee_id  INT           NOT NULL REFERENCES employees(employee_id) ON DELETE CASCADE,
    payroll_id   INT           REFERENCES payroll(payroll_id),
    amount       DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    reason       TEXT,
    awarded_date DATE          NOT NULL DEFAULT CURRENT_DATE
);

-- STORED PROCEDURE
CREATE OR REPLACE PROCEDURE calculate_payroll(
    p_employee_id   INT,
    p_period_start  DATE,
    p_period_end    DATE,
    p_bonus         DECIMAL,
    p_deductions    DECIMAL
)
LANGUAGE plpgsql AS $$
DECLARE
    v_base_salary  DECIMAL(12,2);
    v_total_paid   DECIMAL(12,2);
BEGIN
    SELECT base_salary INTO v_base_salary
    FROM employees WHERE employee_id = p_employee_id;

    IF v_base_salary IS NULL THEN
        RAISE EXCEPTION 'Employee % not found', p_employee_id;
    END IF;

    v_total_paid := v_base_salary + p_bonus - p_deductions;

    INSERT INTO payroll (employee_id, pay_period_start, pay_period_end,
                         base_salary, bonus, deductions, total_paid, payment_date)
    VALUES (p_employee_id, p_period_start, p_period_end,
            v_base_salary, p_bonus, p_deductions, v_total_paid, CURRENT_DATE);
END;
$$;

-- SAMPLE DATA
INSERT INTO admins (username, password, permission_level) VALUES
    ('admin',  'admin123',  'SUPER_ADMIN'),
    ('hr',     'hr123',     'HR_MANAGER');

INSERT INTO employees (full_name, email, password, is_active, base_salary) VALUES
    ('Alice Johnson', 'alice@pms.com', 'alice123', TRUE, 3000.00),
    ('Bob Smith',     'bob@pms.com',   'bob123',   TRUE, 2500.00),
    ('Carol Davis',   'carol@pms.com', 'carol123', TRUE, 3500.00);

-- Sample attendance (today's check-in for employee 1)
INSERT INTO attendance (employee_id, date, check_in, status)
VALUES (1, CURRENT_DATE, NOW(), 'PRESENT')
ON CONFLICT (employee_id, date) DO NOTHING;

-- Sample performance review for employee 1
INSERT INTO performance (employee_id, review_date, score, comments, reviewer_id)
VALUES (1, CURRENT_DATE, 88.50, 'Excellent team player', 1);

-- Verify setup
SELECT 'admins'     AS tbl, COUNT(*) FROM admins
UNION ALL
SELECT 'employees',          COUNT(*) FROM employees
UNION ALL
SELECT 'attendance',         COUNT(*) FROM attendance
UNION ALL
SELECT 'performance',        COUNT(*) FROM performance;
