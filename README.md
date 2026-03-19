# Payroll Management System (PMS)

Console-based Java application for managing employees, attendance, performance, payroll, and payslip generation.

Architecture: **MVC + JDBC + PostgreSQL**

---

## 1) Project Overview

This project is a plain Java (non-Maven/Gradle) console application.

Main layers:

- `controller` -> handles menu flow and user input
- `service` -> business logic
- `repository` -> JDBC data access
- `util` -> helpers (input, date, salary calculation, password)

Main entry point: `MainApplication.java`  
Database config class: `DbConfig.java`

---

## 2) Required Software

Install these first:

1. **JDK 17** (or JDK 11+)
2. **IntelliJ IDEA** (Community or Ultimate)
3. **PostgreSQL** (recommended 14+)
4. (Optional) **psql** command-line tool (comes with PostgreSQL)

### Verify installation

```bash
java -version
javac -version
psql --version
```

---

## 3) Project Structure (Important Paths)

```text
pms_complete/
├─ README.md
├─ pms_complete.iml
└─ pms_complete/
   ├─ src/
   │  ├─ MainApplication.java
   │  └─ project/
   │     ├─ config/DbConfig.java
   │     ├─ controller/
   │     ├─ service/
   │     ├─ repository/
   │     └─ util/
   └─ database/setup.sql
```

---

## 4) Step-by-Step Setup (Beginner Friendly)

### Step 1: Open the project in IntelliJ

1. Open IntelliJ IDEA.
2. Click **Open**.
3. Select folder: `D:\java programingat istad\pms_complete`
4. Wait for indexing to complete.

### Step 2: Configure JDK in IntelliJ

1. Go to **File > Project Structure > Project**.
2. Set **Project SDK** to your JDK (17 recommended).
3. Set **Project language level** to match SDK.

### Step 3: Prepare PostgreSQL

1. Start PostgreSQL service.
2. Create a database (example name: `pms_db`).
3. Run the schema/data script:
   - Script path: `pms_complete\database\setup.sql`

Example SQL (inside `psql`):

```sql
CREATE DATABASE pms_db;
```

### Option A: Run script in terminal (`psql`)

```bash
psql -U postgres -d pms_db -f "D:\java programingat istad\pms_complete\pms_complete\database\setup.sql"
```

### Option B: Run script in IntelliJ Database Tool (Ultimate)

1. Open **Database** panel.
2. Add **Data Source > PostgreSQL**.
3. Connect to your database.
4. Open `setup.sql` and run it.

### Step 4: Configure database connection (`DbConfig.java`)

`DbConfig.java` supports environment variables (recommended) and JVM system properties.

### Recommended: use environment variables

Set these before running:

- `DB_URL` (example: `jdbc:postgresql://localhost:5432/pms_db`)
- `DB_USER` (example: `postgres`)
- `DB_PASS` (your PostgreSQL password)

Windows PowerShell (current terminal session):

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/pms_db"
$env:DB_USER="postgres"
$env:DB_PASS="your_password"
```

### Alternative: VM options in IntelliJ run configuration

```text
-DDB_URL=jdbc:postgresql://localhost:5432/pms_db -DDB_USER=postgres -DDB_PASS=your_password
```

### Step 5: Ensure required libraries are available

Because this is a plain Java project, libraries must be attached in IntelliJ.

Check **File > Project Structure > Modules > Dependencies** for required libraries, including:

- PostgreSQL JDBC driver
- Lombok
- JasperReports
- OpenPDF

If something is missing, add the corresponding JAR/library in IntelliJ.

### Step 6: Run the application

1. Open `pms_complete\src\MainApplication.java`.
2. Click the green **Run** icon next to `main`.
3. Use console menu:
   - `1` for Admin
   - `2` for Employee

If DB connection succeeds, you should see a successful PostgreSQL connection message on startup.

---

## 5) Default Login Accounts (from `setup.sql`)

Admin:

- `admin / admin123`
- `hr / hr123`

Employee examples:

- `alice@pms.com / alice123`
- `bob@pms.com / bob123`

---

## 6) Useful Commands

Run database script:

```bash
psql -U postgres -d pms_db -f "D:\java programingat istad\pms_complete\pms_complete\database\setup.sql"
```

Set DB variables in PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/pms_db"
$env:DB_USER="postgres"
$env:DB_PASS="your_password"
```

---

## 7) Troubleshooting (Common Errors + Fixes)

### `Driver not found — add postgresql JAR to libraries`

Cause: PostgreSQL JDBC driver is not attached.  
Fix: Add PostgreSQL JDBC library/JAR in IntelliJ module dependencies.

### `Connection failed` / authentication errors

Cause: wrong `DB_URL`, `DB_USER`, or `DB_PASS`.  
Fix:

1. Verify PostgreSQL is running.
2. Test login with `psql`.
3. Re-check environment variables or VM options.

### `database "pms_db" does not exist`

Cause: database not created yet.  
Fix: create database first, then run `setup.sql`.

### `relation "... " does not exist`

Cause: tables not created yet (schema script not executed).  
Fix: run `setup.sql` against the same database configured in `DB_URL`.

### App opens but exits or shows invalid input repeatedly

Cause: incorrect console input format.  
Fix: follow menu prompts carefully and enter valid choices only.

### Lombok annotations not working

Cause: Lombok plugin/annotation processing not enabled.  
Fix:

1. Install Lombok plugin in IntelliJ.
2. Enable **Build, Execution, Deployment > Compiler > Annotation Processors > Enable annotation processing**.

---

## 8) Notes for Development

- Keep credentials out of source code when possible.
- Prefer setting `DB_URL`, `DB_USER`, `DB_PASS` per environment.
- `MainApplication.java` is the correct run target for this console application.

---


