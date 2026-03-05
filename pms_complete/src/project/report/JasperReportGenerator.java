package project.report;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import project.model.Payslip;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates a professional PDF payslip using JasperReports and the payslip.jrxml template.
 *
 * Required libraries (add JARs to your classpath):
 *   - jasperreports-*.jar
 *   - commons-beanutils, commons-collections, commons-digester, commons-logging
 *   - itext-2.x or other PDF provider required by your JasperReports version
 *
 * Template location:
 *   - Classpath: /project/report/templates/payslip.jrxml
 *
 * Output:
 *   - PDF is saved to the "reports/" folder in the project root.
 */
public class JasperReportGenerator {

    private static final String OUTPUT_DIR     = "reports/";
    private static final String TEMPLATE_PATH  = "/project/report/templates/payslip.jrxml";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    /**
     * Generates a PDF payslip file using JasperReports.
     *
     * @param slip the payslip data
     * @return path to the generated PDF, or null if failed
     */
    public String generatePayslip(Payslip slip) {
        try {
            new File(OUTPUT_DIR).mkdirs();

            String filename = OUTPUT_DIR + "payslip_emp"
                    + slip.getEmployeeId() + "_" + slip.getPayPeriodStart() + ".pdf";

            // 1) Load and compile JRXML template
            InputStream templateStream = loadTemplate();
            if (templateStream == null) {
                System.err.println("[Report] Could not find payslip.jrxml on classpath or file system.");
                return null;
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);

            // 2) Prepare parameters from Payslip model (see payslip.jrxml)
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

            // 3) Minimal data source (we do not use fields in the template, only parameters)
            JRBeanCollectionDataSource dataSource =
                    new JRBeanCollectionDataSource(Collections.singletonList(slip));

            // 4) Fill report and export to PDF
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);
            JasperExportManager.exportReportToPdfFile(jasperPrint, filename);

            System.out.println("  ✔ JasperReports payslip PDF saved → " + filename);
            return filename;

        } catch (Exception e) {
            System.err.println("[Report] JasperReports PDF generation failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tries to load the JRXML template from the classpath first, then from a relative file path.
     */
    private InputStream loadTemplate() {
        // Classpath (recommended)
        InputStream in = JasperReportGenerator.class.getResourceAsStream(TEMPLATE_PATH);
        if (in != null) return in;

        // Fallback: direct file path during development/IDE runs
        try {
            File file = new File("pms_complete/src/project/report/templates/payslip.jrxml");
            if (file.exists()) {
                return new FileInputStream(file);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String fmt(BigDecimal v) {
        if (v == null) return "$0.00";
        return String.format("$%,.2f", v);
    }
}
