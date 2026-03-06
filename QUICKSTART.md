# 🚀 PMS QUICK START GUIDE

Run the Payroll Management System (PMS) in a few minutes.

This guide is for **demo + testing**. For full documentation, see `README.md`.

---

## 1) Database setup (PostgreSQL)

### Option A: pgAdmin (recommended)

1. Open **pgAdmin**.
2. Right-click **Databases → Create → Database**.
3. Name: `payroll_db` → **Save**.
4. Select `payroll_db` → **Tools → Query Tool**.
5. Open file: `pms_complete/pms_complete/database/setup.sql`.
6. Execute (F5).
7. Confirm message:

```text
✓ Setup complete! Database initialized with sample data.
```

### Option B: Command line (psql)

```bash
psql -U postgres -c "CREATE DATABASE payroll_db;"
psql -U postgres -d payroll_db -f "D:\java programingat istad\pms_complete\pms_complete\database\setup.sql"
```

---

## 2) Configure DB connection variables (required)

The app reads:

- `DB_URL`  (default: `jdbc:postgresql://localhost:5432/payroll_db`)
- `DB_USER` (default: `postgres`)
- `DB_PASS` (required on your machine)

### Windows PowerShell

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/payroll_db"
$env:DB_USER = "postgres"
$env:DB_PASS = "your_postgres_password"
```

### Windows CMD

```cmd
set DB_URL=jdbc:postgresql://localhost:5432/payroll_db
set DB_USER=postgres
set DB_PASS=your_postgres_password
```

---

## 3) Add required JAR libraries

In IntelliJ:

1. File → **Project Structure** → **Modules** → **Dependencies**
2. Click **+ → JARs or directories**
3. Select your `lib/` folder (where you placed all JARs) → Apply → OK

### Required JARs

- `postgresql-42.x.x.jar`
- `lombok.jar`
- JasperReports stack (tested):
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
  - `ecj-3.21.0.jar`

---

## 4) IntelliJ project setup (one-time)

1. Open the project folder in IntelliJ.
2. Mark source root:
   - Right-click `pms_complete/src` → **Mark Directory As → Sources Root**
3. Enable Lombok:
   - Install Lombok plugin
   - Settings → Compiler → Annotation Processors → enable annotation processing

---

## 5) Run the application

1. Open `pms_complete/src/MainApplication.java`
2. Run `MainApplication.main()`
3. Expected output:
   - `[DB] Connected to PostgreSQL successfully (...)`
   - Role selection menu (Admin / Employee / Exit)

---

## 6) Login cheat sheet (from `setup.sql`)

| Type | Username/Email | Password |
|------|-----------------|----------|
| Admin | `admin` | `admin123` |
| Admin | `hr` | `hr123` |
| Employee | `alice@pms.com` | `alice123` |
| Employee | `bob@pms.com` | `bob123` |
| Employee | `carol@pms.com` | `carol123` |
| Employee | `david@pms.com` | `david123` |
| Employee | `emma@pms.com` | `emma123` |

---

## 7) Full demo + test script (show teacher)

### A) Admin demo (2–3 minutes)

1. Login as **Admin**: `admin / admin123`
2. **Manage Employees**:
   - List employees (shows sample employees)
3. **Performance**:
   - Add a performance review for employee ID `1` (e.g. score `95`)
4. **Calculate Payroll**:
   - Calculate payroll for employee ID `1` for current month/year
5. **Generate Payslip (PDF)**:
   - Pick employee ID `1`
   - Select a payroll record ID
   - Confirm the app prints a PDF path under `reports/`

### B) Employee demo (1 minute)

1. Login as **Employee**: `alice@pms.com / alice123`
2. **Check In**
3. **Check Out**
4. **My Payslip**:
   - Select the same payroll record ID
   - Confirm PDF generated

---

## 8) Verification checklist (final)

Before submitting:

- [ ] Database `payroll_db` created
- [ ] `setup.sql` executed successfully
- [ ] DB variables set (`DB_PASS` correct)
- [ ] App starts and connects to PostgreSQL
- [ ] Admin login works
- [ ] Employee login works
- [ ] Check-in/out works (no duplicates)
- [ ] Payroll calculation works (no overlapping period)
- [ ] JasperReports payslip PDF is generated into `reports/`

---

## 9) Troubleshooting

### “Driver not found”
- Add `postgresql-42.x.x.jar` to IntelliJ module dependencies.

### “Connection failed”
- Confirm PostgreSQL is running and `DB_PASS` is correct.
- Confirm database name is `payroll_db`.

### Lombok errors
- Install Lombok plugin + enable annotation processing.

### Payslip PDF generation failed (JasperReports)
- Ensure all JasperReports stack JARs are in dependencies.
- Confirm `payslip.jrxml` exists at:
  - `pms_complete/src/project/report/templates/payslip.jrxml`

