# Payroll Management System (PMS)

### Final  Project – Console-Based Java Application

---

## 1. Project Overview

**Payroll Management System (PMS)** is a **console-based Java application** that calculates employee payroll while analyzing **attendance** and **performance**.

The system demonstrates:

- Role-based access control (Admin vs Employee)
- Employee information management
- Attendance tracking (check-in / check-out, overtime)
- Performance reviews and bonus calculation
- Payroll processing using a PostgreSQL stored procedure
- Payslip generation as PDF using JasperReports

**Teacher’s goal**:
> “Calculate payroll while analyzing employee performance and attendance.”

This project fully implements that goal.

---

## 2. Main Features (Teacher Requirements Mapping)

### 2.1 Role-based login (Admin + Employee)

- Separate login flows for:
  - **Admin**: username + password.
  - **Employee**: email + password.
- Successful login routes to:
  - **Admin dashboard** – management functions only.
  - **Employee dashboard** – self-service functions only.
- Role-based menus ensure employees cannot access admin-only features.

### 2.2 Employee information management

- Admin can:
  - Create new employees (full name, email, base salary, position, department, password).
  - View employee list with pagination.
  - Search employees by name.
  - Update employee details.
  - Disable employees (soft delete via `is_active = FALSE`).

### 2.3 Attendance tracking (check-in / check-out)

- Employee functions:
  - **Check-in**:
    - Once per day.
    - Only allowed during configured time window (e.g. 7:00–23:00).
    - Stored with `check_in`, `status`, etc.
  - **Check-out**:
    - Requires a valid check-in today.
    - Once per day.
    - Calculates:
      - `work_hours`
      - `overtime_hours` (hours above 8 per day)
- System guarantees:
  - One attendance record per employee per day (unique `(employee_id, date)`).
  - Correct `check_out >= check_in` via DB constraint.
  - Ability to view:
    - **My Attendance** (per employee).
    - **All Attendance** (admin, with pagination).

### 2.4 Salary + overtime pay calculation

- Base salary per employee stored in `employees.base_salary`.
- Overtime pay:
  - Uses daily `overtime_hours` from attendance.
  - Hourly rate derived from base salary.
- Deductions:
  - **Income Tax**: 10% of gross (base + overtime + bonus).
  - **Social Security**: 2% of base salary.
- Total pay:
  - `total_paid = base_salary + bonus – deductions`.
- Implemented in `SalaryCalculator` and `PayrollService`.

### 2.5 Performance rating & bonus calculation

- Admin can record performance reviews:
  - Score (0 < score ≤ 100).
  - Comments.
  - Reviewer admin ID.
- System maintains:
  - All performance records per employee.
  - Latest review.
  - **Average score** for bonus calculation.
- Bonus tiers (example logic):
  - High scores receive higher bonus percentages of base salary.
- Bonuses are:
  - Computed in Java.
  - Stored in `bonus` table.
  - Linked to specific payrolls.

### 2.6 Payslip PDFs using JasperReports

- Admin:
  - Calculates payrolls for employees.
  - Generates payslips for any existing payroll record.
- Employee:
  - Views own payroll history.
  - Generates own payslips.
- PDF generation:
  - Uses **JasperReports** with template `payslip.jrxml`.
  - Outputs PDF files to a local `reports/` folder.
  - Shows:
    - Employee info, pay period, payment date.
    - Base salary, overtime pay, bonus.
    - Tax, social security.
    - Net pay.

---

## 3. Technology Stack

- **Language**: Java (JDK 11+ recommended; tested with JDK 17)
- **UI**: Console (System.in / System.out) – no GUI, no web
- **Database**: PostgreSQL
- **Database access**: JDBC + `PreparedStatement` (no ORM / no JPA)
- **Reporting**: JasperReports (JRXML → PDF) + OpenPDF
- **Project type**: Plain Java project (no Maven/Gradle)
- **Models**: Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- **Security**:
  - Password hashing with PBKDF2 + salt (`PasswordUtil`)
  - Backward-compatible with legacy SHA-256 hashes (existing data)
  - Role-based access enforced in controllers & menus
- **Architecture**:
  - Layered:
    - Controller (console) → Service → Repository/DAO → Database
  - DTOs and mappers isolate presentation from DB models

---

## 4. Project Structure

```text
pms_complete/
├─ pms_complete/
│  ├─ src/
│  │  ├─ MainApplication.java
│  │  │    ↳ Program entry point, role selection, admin/employee dashboards
│  │  │
│  │  ├─ project/config/
│  │  │    └─ DbConfig.java
│  │  │         ↳ Central PostgreSQL connection management
│  │  │
│  │  ├─ project/dao/
│  │  │    ├─ UserDao.java
│  │  │    └─ UserDaoImpl.java
│  │  │         ↳ Login (admin/employee), last_login updates
│  │  │
│  │  ├─ project/model/
│  │  │    ├─ Employee.java
│  │  │    ├─ Attendance.java
│  │  │    ├─ Performance.java
│  │  │    ├─ Payroll.java
│  │  │    ├─ Bonus.java
│  │  │    ├─ LeaveRequest.java
│  │  │    ├─ Payslip.java
│  │  │    ├─ User.java
│  │  │    └─ Pagination.java
│  │  │
│  │  ├─ project/dto/
│  │  │    ├─ EmployeeDTO.java
│  │  │    ├─ AttendanceDTO.java
│  │  │    ├─ PerformanceDTO.java
│  │  │    ├─ PayrollDTO.java
│  │  │    ├─ LeaveRequestDTO.java
│  │  │    ├─ SalaryReportDTO.java
│  │  │    ├─ LoginRequest.java
│  │  │    ├─ LoginResponse.java
│  │  │    └─ UserDTO.java
│  │  │
│  │  ├─ project/mapper/
│  │  │    └─ EntityMapper.java
│  │  │         ↳ Converts between models and DTOs
│  │  │
│  │  ├─ project/repository/
│  │  │    ├─ EmployeeRepository.java
│  │  │    ├─ AttendanceRepository.java
│  │  │    ├─ PerformanceRepository.java
│  │  │    ├─ PayrollRepository.java
│  │  │    ├─ BonusRepository.java
│  │  │    └─ LeaveRequestRepository.java
│  │  │         ↳ All pure JDBC with PreparedStatement
│  │  │
│  │  ├─ project/service/
│  │  │    ├─ AuthService.java
│  │  │    ├─ EmployeeService.java
│  │  │    ├─ AttendanceService.java
│  │  │    ├─ PerformanceService.java
│  │  │    ├─ PayrollService.java
│  │  │    ├─ BonusService.java
│  │  │    └─ LeaveRequestService.java
│  │  │         ↳ Business logic for each aggregate
│  │  │
│  │  ├─ project/controller/
│  │  │    ├─ AdminController.java
│  │  │    ├─ EmployeeController.java
│  │  │    ├─ AttendanceController.java
│  │  │    ├─ PerformanceController.java
│  │  │    ├─ PayrollController.java
│  │  │    └─ LeaveRequestController.java
│  │  │         ↳ Menus and user input handling
│  │  │
│  │  ├─ project/util/
│  │  │    ├─ InputUtil.java
│  │  │    ├─ ViewUtil.java
│  │  │    ├─ DateUtil.java
│  │  │    ├─ SalaryCalculator.java
│  │  │    └─ PasswordUtil.java
│  │  │
│  │  ├─ project/report/
│  │  │    ├─ JasperReportGenerator.java
│  │  │    └─ templates/
│  │  │         └─ payslip.jrxml
│  │  │
│  │  └─ project/procedure/
│  │       └─ PayrollProcedure.java
│  │            ↳ Calls PostgreSQL stored procedure calculate_payroll(...)
│  │
│  └─ database/
│     └─ setup.sql
│          ↳ Full schema, stored procedure, indexes, views, sample data
│
├─ README.md
└─ QUICKSTART.md

```

## +. High-Level Flowchart (Text Version)

### Main Flow (Simplified)

1. **Start application**
  - `MainApplication.main()` calls `DbConfig.init()` and prints welcome.

2. **Role selection**
  - Show menu: Admin / Employee / Exit.
  - User chooses role.

3. **Admin login**
  - Read username + password.
  - `UserDaoImpl.adminLogin(...)`:
    - Fetch admin row.
    - Verify password (PBKDF2 or legacy SHA-256).
    - Upgrade hash if needed.
  - If OK → `runAdminDashboard(admin)`.

4. **Employee login**
  - Read email + password.
  - `UserDaoImpl.employeeLogin(...)`:
    - Fetch employee row where `is_active = TRUE`.
    - Verify password.
    - Upgrade hash if needed.
    - Update `last_login`.
  - If OK → `runEmployeeDashboard(employee)`.

5. **Admin dashboard (loop)**
  - Options:
    - Manage employees (CRUD).
    - View all attendance.
    - Manage performance (add/view reviews).
    - Calculate payroll.
    - Generate payslips.
    - Manage bonuses.
    - Review leave requests.
  - On each option, the corresponding controller is called:
    - e.g. `PayrollController.calculatePayroll()` → `PayrollService.calculatePayroll()`.

6. **Employee dashboard (loop)**
  - Options:
    - Check in / Check out (`AttendanceController` → `AttendanceService` → `AttendanceRepository`).
    - View my attendance.
    - View my performance.
    - View my payslip (build `Payslip` and call `JasperReportGenerator`).
    - Change password.
    - Submit / view my leave requests.

7. **Exit**
  - On main menu `0`, application says goodbye and `DbConfig.close()`.

---

## +. Database ERD (Entities & Relationships)

### Tables (from `setup.sql`)

---

**`admins`**

| Column | Notes |
|---|---|
| `admin_id` | PK |
| `username` | UNIQUE |
| `password` | |
| `permission_level` | |
| `created_at` | |
| `last_login` | |
| `is_active` | |

---

**`employees`**

| Column | Notes |
|---|---|
| `employee_id` | PK |
| `full_name` | |
| `email` | UNIQUE |
| `password` | |
| `is_active` | |
| `base_salary` | |
| `position` | |
| `department` | |
| `hire_date` | |
| `last_login` | |
| `created_at` | |
| `updated_at` | |

---

**`attendance`**

| Column | Notes |
|---|---|
| `attendance_id` | PK |
| `employee_id` | FK → `employees.employee_id` |
| `date` | |
| `check_in` | |
| `check_out` | |
| `status` | `PRESENT`, `ABSENT`, `LATE`, `HALF_DAY`, `ON_LEAVE` |
| `work_hours` | |
| `overtime_hours` | |
| `late_minutes` | |
| `early_leave_minutes` | |
| `leave_type` | |
| `note` | |
| `created_at` | |
| *(unique)* | `(employee_id, date)` |

---

**`performance`**

| Column | Notes |
|---|---|
| `performance_id` | PK |
| `employee_id` | FK → `employees.employee_id` |
| `review_date` | |
| `score` | 0 < score ≤ 100 |
| `comments` | |
| `reviewer_id` | FK → `admins.admin_id` |
| `created_at` | |

---

**`payroll`**

| Column | Notes |
|---|---|
| `payroll_id` | PK |
| `employee_id` | FK → `employees.employee_id` |
| `pay_period_start` | |
| `pay_period_end` | |
| `base_salary` | |
| `bonus` | |
| `deductions` | |
| `total_paid` | |
| `payment_date` | |
| `created_at` | |

---

**`bonus`**

| Column | Notes |
|---|---|
| `bonus_id` | PK |
| `employee_id` | FK → `employees.employee_id` |
| `payroll_id` | FK → `payroll.payroll_id` (nullable, `ON DELETE SET NULL`) |
| `amount` | |
| `reason` | |
| `awarded_date` | |
| `created_at` | |

---

**`leave_request`**

| Column | Notes |
|---|---|
| `leave_request_id` | PK |
| `employee_id` | FK → `employees.employee_id` |
| `start_date` | |
| `end_date` | |
| `leave_type` | `SICK`, `VACATION`, `PERSONAL`, `EMERGENCY` |
| `reason` | |
| `status` | `PENDING`, `APPROVED`, `REJECTED` |
| `reviewer_id` | FK → `admins.admin_id` (nullable) |
| `review_note` | |
| `request_date` | |
| `review_date` | |
| `created_at` | |

## +. Database Relationships (Text ERD)

1. **Admins ↔ Performance**
  - One admin can write many performance reviews.
  - `performance.reviewer_id` → `admins.admin_id`

2. **Employees ↔ Attendance**
  - One employee has many attendance records.
  - `attendance.employee_id` → `employees.employee_id`
  - Unique constraint on `(employee_id, date)` ensures at most one row per day per employee.

3. **Employees ↔ Performance**
  - One employee has many performance records.
  - `performance.employee_id` → `employees.employee_id`

4. **Employees ↔ Payroll**
  - One employee has many payroll records (each for a period).
  - `payroll.employee_id` → `employees.employee_id`

5. **Employees ↔ Bonus**
  - One employee has many bonus records.
  - `bonus.employee_id` → `employees.employee_id`
  - A bonus may optionally be linked to a specific payroll:
    - `bonus.payroll_id` → `payroll.payroll_id`

6. **Employees ↔ Leave Requests**
  - One employee has many `leave_request` rows.
  - `leave_request.employee_id` → `employees.employee_id`
  - Each request may be reviewed by an admin:
    - `leave_request.reviewer_id` → `admins.admin_id`


## 5. System Requirements

### 5.1 Software

| Requirement | Version |
|---|---|
| JDK | 11 or higher (JDK 17 recommended) |
| PostgreSQL | 12 or higher |
| pgAdmin 4 / psql | To run `setup.sql` |
| IntelliJ IDEA | Or any Java IDE |

### 5.2 Required JAR Libraries

Create a `lib/` folder and add these JARs:

**Database + Lombok**
- `postgresql-42.x.x.jar` (PostgreSQL JDBC driver)
- `lombok.jar`

**JasperReports stack (tested combination)**
- `jasperreports-6.20.6.jar`
- `commons-beanutils-1.9.4.jar`
- `commons-collections4-4.2.jar`
- `commons-digester-2.1.jar`
- `commons-logging-1.2.jar`
- `openpdf-1.3.30.jar`
- `jackson-core-2.14.1.jar`
- `jackson-databind-2.14.1.jar`
- `jackson-annotations-2.14.1.jar`
- `jackson-dataformat-xml-2.14.1.jar`
- `ecj-3.21.0.jar` (Eclipse Java compiler used by JasperReports)

---

## 6. Database Setup

### 6.1 Create the database

1. Open pgAdmin.
2. Connect to your PostgreSQL server.
3. Right-click **Databases** → **Create** → **Database**.
4. Name: `payroll_db`
5. Click **Save**.

### 6.2 Run the schema + sample data script

1. In pgAdmin, expand **Databases** → `payroll_db`.
2. Right-click `payroll_db` → **Tools** → **Query Tool**.
3. In Query Tool, open:
   ```
   pms_complete/pms_complete/database/setup.sql
   ```
4. Click **Execute** (or press `F5`).
5. When complete, output should contain:
   ```
   ✓ Setup complete! Database initialized with sample data.
   ```

The script creates:
- **Tables:** `admins`, `employees`, `attendance`, `performance`, `payroll`, `bonus`, `leave_request`
- **Stored procedure:** `calculate_payroll(...)`
- Indexes and useful views
- **Sample data for:** 2 admins (`admin`, `hr`), 5 employees, attendance & performance history

---

## 7. DB Connection Configuration

`DbConfig` uses environment variables (or system properties):

| Variable | Default | Notes |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/payroll_db` | |
| `DB_USER` | `postgres` | |
| `DB_PASS` | *(none)* | Must be set for your machine |

**Windows PowerShell**
```powershell
$env:DB_URL  = "jdbc:postgresql://localhost:5432/payroll_db"
$env:DB_USER = "postgres"
$env:DB_PASS = "your_postgres_password"
```

**Windows CMD**
```cmd
set DB_URL=jdbc:postgresql://localhost:5432/payroll_db
set DB_USER=postgres
set DB_PASS=your_postgres_password
```

**IntelliJ IDEA**

Run → Edit Configurations → Select `MainApplication` → Environment variables → Add:
- `DB_URL`
- `DB_USER`
- `DB_PASS`

---

## 8. IntelliJ IDEA Configuration

1. **Open the project**
  - File → Open… → select `pms_complete` folder → OK.

2. **Mark source root**
  - In the Project window, right-click `pms_complete/src` → Mark Directory As → **Sources Root**.

3. **Add external libraries**
  - File → Project Structure → Modules → Dependencies tab.
  - Click `+` → JARs or directories.
  - Select your `lib/` folder with all JARs → Apply → OK.

4. **Enable Lombok**
  - File → Settings → Plugins → search `Lombok` → Install → Restart IDE.
  - File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors.
  - Tick **Enable annotation processing** → Apply → OK.

---

## 9. Running the Application

1. Open `pms_complete/src/MainApplication.java`.
2. Right-click the file → **Run 'MainApplication.main()'**.
3. In the console, you should see:
   ```
   [DB] Connected to PostgreSQL successfully (jdbc:postgresql://localhost:5432/payroll_db).
   ```
4. The PMS banner and role selection menu:
   ```
   [1] Admin
   [2] Employee
   [0] Exit
   ```

If connection fails, check:
- PostgreSQL service is running.
- `payroll_db` exists.
- `DB_URL`, `DB_USER`, `DB_PASS` are correct.

---

## 10. Test Accounts

After executing `setup.sql`:

**Admin accounts**

| Username | Password |
|---|---|
| `admin` | `admin123` |
| `hr` | `hr123` |

**Employee accounts**

| Email | Password |
|---|---|
| `alice@pms.com` | `alice123` |
| `bob@pms.com` | `bob123` |
| `carol@pms.com` | `carol123` |
| `david@pms.com` | `david123` |
| `emma@pms.com` | `emma123` |

---

## 11. Testing Guide (Manual End-to-End Tests)

### 11.1 Pre-Test Checklist

Before starting tests, confirm:

- [ ] Java, PostgreSQL, pgAdmin installed.
- [ ] Database `payroll_db` exists.
- [ ] `setup.sql` executed successfully.
- [ ] All required JARs are added in Project Structure → Modules → Dependencies.
- [ ] `pms_complete/src` is marked as Sources Root.
- [ ] `DB_URL`, `DB_USER`, `DB_PASS` are set.
- [ ] `MainApplication` starts and shows role selection menu.

### 11.2 Database Quick Checks (optional but recommended)

In pgAdmin Query Tool:

```sql
SELECT * FROM admins;
SELECT * FROM employees;
SELECT * FROM performance;
SELECT * FROM attendance;
```

You should see initial data (admins, employees, some attendance and performance).

### 11.3 Admin Login and Navigation

1. Run application.
2. Choose role: `1` (Admin).
3. Login with: `admin` / `admin123`

**Expected:**
- Welcome message: `"Welcome, admin! [SUPER_ADMIN]"`
- Admin menu with options for employees, attendance, performance, payroll, etc.

### 11.4 Employee Login

1. From main menu, choose role: `2` (Employee).
2. Login with: `alice@pms.com` / `alice123`

**Expected:**
- Welcome message for Alice.
- Employee menu with: Check in / Check out, My attendance, My performance, My payslip, Change password, Leave request options.

### 11.5 Attendance Test

**As an employee:**
1. Choose **Check In** → should succeed.
2. Choose **Check In** again → should be rejected (`"already checked in today"`).
3. Choose **Check Out** → should succeed and compute work/overtime hours.
4. Choose **Check Out** again → should be rejected.
5. Choose **My Attendance** → verify today's row appears.

**Database verification:**
```sql
SELECT employee_id, date, check_in, check_out, work_hours, overtime_hours
FROM attendance
WHERE employee_id = 1
ORDER BY date DESC;
```

### 11.6 Performance & Bonus Test

**As admin:**
1. Use Performance menu to add a new review:
  - Select employee ID.
  - Enter score and comments.
2. View performance list for that employee.

**Confirm:**
- New review appears.
- Average score updated.

### 11.7 Payroll Calculation Test

**As admin:**
1. Use Payroll menu → **Calculate Payroll** (for one employee).
2. Enter: valid employee ID, month and year.

**Confirm:**
- Overlapping payroll periods are prevented.
- Successful calculation prints summary (base, bonus, deductions, total paid).

**Database verification:**
```sql
SELECT * FROM payroll
WHERE employee_id = 1
ORDER BY payroll_id DESC
LIMIT 1;
```

### 11.8 Payslip PDF Test (JasperReports)

**As admin:**
1. Use Payroll menu → **Generate Payslip**.
2. Enter employee ID.
3. From the list, choose a Payroll ID.

**Expected:**
- Console prints full payslip (via `Payslip.toString()`).
- A PDF path is printed, e.g. `reports/payslip_emp1_2026-03-01.pdf`.

**Open the generated PDF and confirm:**
- Employee info.
- Pay period & payment date.
- Earnings: base salary, overtime pay, performance bonus.
- Deductions: tax (10%), social security (2%).
- Net pay.

**As employee:**
1. Login → Choose **My Payslip** → Select a payroll ID.
2. A personal payslip PDF is also created in `reports/`.

### 11.9 Password Change Test (Security)

1. As an employee, login with original password.
2. Use **Change Password** function.
3. Log out, then log in with the new password.

**DB check:** The `employees.password` field should now contain a PBKDF2 hash (starts with `pbkdf2:`).

---

## 12. Key Features Implementation (Where in Code)

| Feature | Repositories / Services / Controllers |
|---|---|
| **Role-based login** | `MainApplication` – role selection; `UserDaoImpl` – `adminLogin`, `employeeLogin` with PBKDF2 + SHA-256 migration |
| **Employee management** | `EmployeeRepository`, `EmployeeService`, `AdminController` |
| **Attendance tracking** | `AttendanceRepository`, `AttendanceService`, `AttendanceController` |
| **Performance & bonus** | `PerformanceRepository`, `BonusRepository`, `PerformanceService`, `BonusService`, `PayrollService`, `PerformanceController`, `PayrollController` |
| **Payroll with stored procedure** | `database/setup.sql` → `calculate_payroll(...)`; `PayrollProcedure`; `PayrollService.calculatePayroll(...)` |
| **Payslip PDF (JasperReports)** | Template: `project/report/templates/payslip.jrxml`; `JasperReportGenerator`; `PayrollService.buildPayslip(...)`; `PayrollController` |
| **Security** | `PasswordUtil` – PBKDF2 + SHA-256; `UserDaoImpl` – hash verification & upgrade; `EmployeeRepository` – PBKDF2 for new passwords |

---

## 13. Final Checklist for Teacher (What Has Been Done)

- [x] Role-based login (Admin + Employee) implemented.
- [x] Employee CRUD and disabling implemented.
- [x] Attendance tracking (check-in/out, work & overtime hours) implemented.
- [x] Performance reviews and average score implemented.
- [x] Salary, overtime, bonus, tax, and social security calculations implemented.
- [x] Payroll insertion delegated to PostgreSQL stored procedure.
- [x] Bonus table linked to payroll with transactional consistency.
- [x] Payslip PDF generation using JasperReports template implemented.
- [x] Employee self-service for attendance, performance, payslips, password change, and leave requests implemented.
- [x] Passwords stored securely with PBKDF2 + salt, with migration from SHA-256.
- [x] Project fully documented with setup, quickstart, and testing instructions.

> **This project is complete and ready for submission.**

## 14. Project Validation

This section explains how we validated that the Payroll Management System (PMS) satisfies the functional and technical requirements.

### 14.1 Functional Validation

**Goal:** “Calculate payroll while analyzing employee performance and attendance.”

We validated each required feature:

- **Role-based login (Admin + Employee)**
  - Tested valid and invalid logins for both roles.
  - Verified that each role only sees its own menu options (no cross-access).

- **Employee management**
  - Created, updated, listed, searched, and disabled employees.
  - Verified that disabled employees cannot log in.
  - Checked `employees` table after each operation.

- **Attendance tracking**
  - Performed check-in and check-out scenarios:
    - First check-in/checkout → success.
    - Second check-in/checkout in same day → rejected.
  - Verified attendance rows in `attendance` table (date, times, work_hours, overtime_hours).

- **Performance and bonus**
  - Added performance reviews with different scores.
  - Verified they appear in `performance` table with correct reviewer.
  - Checked that average score and bonus change as expected when recalculating payroll.

- **Payroll calculation**
  - Ran payroll for single employees and all employees for given month/year.
  - Confirmed validations:
    - No overlapping payroll periods.
    - No negative total pay.
  - Inspected `payroll` and `bonus` tables to confirm new rows and amounts.

- **Payslip PDFs (JasperReports)**
  - Generated payslips from the Admin side and Employee side.
  - Opened generated PDFs in the `reports/` folder.
  - Verified:
    - Employee info, period, pay date.
    - Base salary, overtime, bonus.
    - Tax, social security, net pay.

### 14.2 Database & Integrity Validation

- **Schema and constraints**
  - Confirmed all tables, foreign keys, and checks were created by `setup.sql`.
  - Validated:
    - Unique email per employee.
    - One attendance row per employee per day.
    - Score > 0 and ≤ 100 for performance.
    - Non-negative salary, bonus, deductions, and total_paid.

- **Stored procedure**
  - Used `calculate_payroll(...)` through `PayrollService`.
  - Intentionally tested error cases (invalid employee, bad dates) to ensure procedure rejects invalid input.

### 14.3 Security Validation

- **Passwords**
  - Verified initial sample users (SHA-256) can log in.
  - After first login, confirmed that:
    - Hash in DB is upgraded to PBKDF2 format (`pbkdf2:` prefix).
  - Created new employees and changed passwords:
    - Checked that their hashes are immediately stored as PBKDF2.
  - Tried wrong password logins to ensure correct rejection.

- **Role isolation**
  - Confirmed:
    - Admin functions cannot be reached from the Employee menu.
    - Employees cannot see or modify other employees’ data or payrolls.

### 14.4 Usability Validation

- **Console UX**
  - Menus and prompts tested with normal and invalid input (e.g., wrong options).
  - Error messages and success messages are clear and informative.
  - Application handles repeated use (multiple logins, multiple calculations) without crashing.

### 14.5 Final Validation Checklist

Before final submission, we verified:

- [x] Setup script `setup.sql` runs without errors and initializes sample data.
- [x] Application connects to PostgreSQL using environment variables (`DB_URL`, `DB_USER`, `DB_PASS`).
- [x] All teacher-required features are present and working:
  - Role-based login (Admin/Employee)
  - Employee management
  - Attendance check-in/out
  - Salary + overtime calculation
  - Performance ratings and bonuses
  - JasperReports PDF payslips
- [x] No known runtime errors in full flow:
  - Login → dashboard → attendance → performance → payroll → payslip PDF.
- [x] Passwords are never stored in plain text and all new passwords use PBKDF2.
- [x] Generated PDFs match the values stored in the database.


