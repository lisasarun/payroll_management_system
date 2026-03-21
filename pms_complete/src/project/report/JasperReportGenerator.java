package project.report;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import project.model.Payslip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class JasperReportGenerator {

    private static final String OUTPUT_DIR     = "reports/";
    private static final String TEMPLATE_PATH  = "/project/report/templates/payslip.jrxml";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");


    public String generatePayslip(Payslip slip) {
        try {
            new File(OUTPUT_DIR).mkdirs();

            // Check if PDF already exists for this payroll ID and period
            String existingPdf = findExistingPayslip(slip);
            if (existingPdf != null) {
                System.out.println("  ℹ Payslip PDF already exists: " + existingPdf);
                return existingPdf;
            }

            // Use a unique filename to avoid Windows file-lock issues when a previous PDF is still open.
            String filename = buildUniqueFilename(slip);

            InputStream templateStream = loadTemplate();
            if (templateStream == null) {
                System.err.println("[Report] Could not find payslip.jrxml on classpath or file system.");
                return null;
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);

            Map<String, Object> params = new HashMap<>();
            params.put("EMPLOYEE_NAME", slip.getEmployeeName());
            params.put("EMPLOYEE_ID",   String.format("EMP-%03d", slip.getEmployeeId()));
            params.put("EMAIL",         slip.getEmail());

            String period = slip.getPayPeriodStart().format(DATE_FMT)
                    + "  to  "
                    + slip.getPayPeriodEnd().format(DATE_FMT);
            params.put("PERIOD",  period);
            params.put("PAY_DATE", slip.getPaymentDate().format(DATE_FMT));

            params.put("BASE_SALARY", fmt(slip.getBaseSalary()));
            params.put("OVERTIME",    fmt(slip.getOvertimePay()));
            params.put("BONUS",       fmt(slip.getBonus()));
            params.put("TAX",         fmt(slip.getTax()));
            params.put("SOC_SEC",     fmt(slip.getSocialSecurity()));
            params.put("NET_PAY",     fmt(slip.getTotalPaid()));

            JRBeanCollectionDataSource dataSource =
                    new JRBeanCollectionDataSource(Collections.singletonList(slip));

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);
            try {
                JasperExportManager.exportReportToPdfFile(jasperPrint, filename);
            } catch (JRRuntimeException ex) {
                // Common on Windows: the target file is locked by a PDF viewer.
                // Retry once with a fresh unique filename.
                if (isFileLockIssue(ex)) {
                    String retry = buildUniqueFilename(slip);
                    JasperExportManager.exportReportToPdfFile(jasperPrint, retry);
                    filename = retry;
                } else {
                    throw ex;
                }
            }

            System.out.println("  ✔ JasperReports payslip PDF saved → " + filename);
            return filename;

        } catch (Exception e) {
            System.err.println("[Report] JasperReports PDF generation failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Finds existing payslip PDF for the given employee and pay period.
     * Returns the file path if found, null otherwise.
     */
    private String findExistingPayslip(Payslip slip) {
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) return null;

        String prefix = "payslip_emp" + slip.getEmployeeId() + "_" + slip.getPayPeriodStart();
        File[] files = dir.listFiles((d, name) -> name.startsWith(prefix) && name.endsWith(".pdf"));

        if (files != null && files.length > 0) {
            // Return the first matching file (most recent should be used if multiple exist)
            return files[0].getPath();
        }
        return null;
    }

    private InputStream loadTemplate() {

        InputStream in = JasperReportGenerator.class.getResourceAsStream(TEMPLATE_PATH);
        if (in != null) return in;

        try {
            File file = new File("pms_complete/src/project/report/templates/payslip.jrxml");
            if (file.exists()) {
                return new FileInputStream(file);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String buildUniqueFilename(Payslip slip) {
        String ts = LocalDateTime.now().format(TS_FMT);
        String base = OUTPUT_DIR + "payslip_emp" + slip.getEmployeeId()
                + "_" + slip.getPayPeriodStart()
                + "_" + ts;
        String candidate = base + ".pdf";

        // Extra safety: if file already exists, append a counter.
        int counter = 1;
        while (new File(candidate).exists()) {
            candidate = base + "_" + counter + ".pdf";
            counter++;
        }
        return candidate;
    }

    private boolean isFileLockIssue(Throwable ex) {
        // Walk causes and check for "used by another process" / FileNotFoundException patterns.
        Throwable cur = ex;
        while (cur != null) {
            if (cur instanceof FileNotFoundException) return true;
            String msg = cur.getMessage();
            if (msg != null && msg.toLowerCase().contains("being used by another process")) return true;
            cur = cur.getCause();
        }
        return false;
    }

    private String fmt(BigDecimal v) {
        if (v == null) return "$0.00";
        return String.format("$%,.2f", v);
    }
}
