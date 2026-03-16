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


public class JasperReportGenerator {

    private static final String OUTPUT_DIR     = "reports/";
    private static final String TEMPLATE_PATH  = "/project/report/templates/payslip.jrxml";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");


    public String generatePayslip(Payslip slip) {
        try {
            new File(OUTPUT_DIR).mkdirs();

            String filename = OUTPUT_DIR + "payslip_emp"
                    + slip.getEmployeeId() + "_" + slip.getPayPeriodStart() + ".pdf";

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
            JasperExportManager.exportReportToPdfFile(jasperPrint, filename);

            System.out.println("  ✔ JasperReports payslip PDF saved → " + filename);
            return filename;

        } catch (Exception e) {
            System.err.println("[Report] JasperReports PDF generation failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
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

    private String fmt(BigDecimal v) {
        if (v == null) return "$0.00";
        return String.format("$%,.2f", v);
    }
}
