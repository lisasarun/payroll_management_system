# 🚀 PMS QUICK START GUIDE
## Get Running in 5 Minutes

---

## 1️⃣ DATABASE SETUP (2 minutes)

### Option A: Using pgAdmin (Recommended)
```
1. Open pgAdmin
2. Right-click "Databases" → Create → Database
3. Name: payroll_db → Save
4. Open payroll_db → Tools → Query Tool
5. Open file: pms_complete/database/setup.sql
6. Press F5 (Execute)
7. Wait for: "✓ Setup complete!"
```

### Option B: Using Command Line
```bash
# Create database
psql -U postgres -c "CREATE DATABASE payroll_db;"

# Run setup script
psql -U postgres -d payroll_db -f "D:\Java Programing\pms_complete\pms_complete\database\setup.sql"
```

---

## 2️⃣ SET PASSWORD (30 seconds)

**Windows (Command Prompt):**
```cmd
set DB_PASS=your_postgres_password
```

**Windows (PowerShell):**
```powershell
$env:DB_PASS="your_postgres_password"
```

**Linux/Mac:**
```bash
export DB_PASS=your_postgres_password
```

---

## 3️⃣ VERIFY LIBRARIES (1 minute)

Open IntelliJ → File → Project Structure → Libraries

**Required JARs:**
- ✅ postgresql-42.7.1.jar
- ✅ lombok.jar
- ✅ itextpdf-5.5.13.3.jar

**Missing a JAR?**
- PostgreSQL: https://jdbc.postgresql.org/download/
- Lombok: https://projectlombok.org/download
- iText5: Search "itextpdf-5.5.13.3.jar download"

---

## 4️⃣ RUN! (30 seconds)

1. Open `MainApplication.java`
2. Right-click → **Run 'MainApplication.main()'**
3. See welcome screen? ✅ **YOU'RE READY!**

---

## 🧪 QUICK TEST (1 minute)

### Test Admin Login:
```
Select: 1 (Admin)
Username: admin
Password: admin123
✅ Should see: "Welcome, admin! [SUPER_ADMIN]"
Select: 5 (List All Employees)
✅ Should see: 5 employees listed
```

### Test Employee Login:
```
Select: 2 (Employee)
Email: alice@pms.com
Password: alice123
✅ Should see: "Welcome, Alice Johnson!"
Select: 1 (Check In)
✅ Should see: "Checked in at [time]"
```

### Test Payroll Calculation:
```
Login as admin
Select: 4 (Calculate Payroll)
Select: 1 (Calculate for one employee)
Employee ID: 1
Month: 3 (or current month)
Year: 2026
Confirm: y
✅ Should see: Payroll summary with Base, Overtime, Bonus, Deductions, Total
```

### Test PDF Generation:
```
Login as admin
Select: 5 (Generate Payslip)
Employee ID: 1
Select any payroll ID from list
✅ Should see: "PDF saved → reports/payslip_emp1_[date].pdf"
Open the PDF → Should show professional payslip ✅
```

---

## 🎯 LOGIN CHEATSHEET

| Type     | Username/Email   | Password  |
|----------|------------------|-----------|
| Admin    | admin            | admin123  |
| Admin    | hr               | hr123     |
| Employee | alice@pms.com    | alice123  |
| Employee | bob@pms.com      | bob123    |
| Employee | carol@pms.com    | carol123  |
| Employee | david@pms.com    | david123  |
| Employee | emma@pms.com     | emma123   |

---

## ❌ TROUBLESHOOTING

### "Driver not found"
→ Add `postgresql-42.7.1.jar` to libraries

### "Connection failed"
→ Check DB_PASS environment variable is set correctly

### "Lombok errors"
→ Install Lombok plugin: Settings → Plugins → Search "Lombok"

### "No employees found"
→ Re-run setup.sql script

### "PDF generation failed"
→ Verify `itextpdf-5.5.13.3.jar` is in libraries (NOT version 7.x)

---

## ✅ VERIFICATION CHECKLIST

Before demonstrating to teacher:

- [ ] Database `payroll_db` exists
- [ ] Can login as admin (admin/admin123)
- [ ] Can login as employee (alice@pms.com/alice123)
- [ ] Employee check-in works
- [ ] Payroll calculation works
- [ ] PDF payslip generates correctly
- [ ] Reports folder contains PDF files

---

## 🎓 DEMO FLOW (Show Teacher)

**5-Minute Demo Script:**

1. **Start App** → Show welcome screen
2. **Admin Login** → admin/admin123
3. **List Employees** → Show 5 employees
4. **Performance Review** → Rate Alice (95 score)
5. **Calculate Payroll** → For Alice, current month
6. **Generate Payslip** → Show PDF with bonus
7. **Logout → Employee Login** → alice@pms.com/alice123
8. **Check In** → Show timestamp
9. **View My Payslip** → Same payslip from employee view

**⏱️ Total Time: ~3 minutes**
**💯 Result: Teacher sees FULL feature set working perfectly!**

---

## 📚 FULL DOCS

For complete documentation, see: `README.md`

---

**🟢 PROJECT IS 100% READY FOR SUBMISSION**
