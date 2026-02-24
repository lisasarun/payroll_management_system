package project.report;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import project.dto.PayrollDTO;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

/**
  Generates payslip PDF reports using JasperReports.

 * Setup needed:
    1. Place payslip.jrxml in src/project/report/templates/
    2. The .jrxml uses fields matching PayrollDTO field names:
       employeeName, payPeriodStart, payPeriodEnd, baseSalary,
       overtimePay, bonus, deductions, totalPaid, paymentDate
    3. Add jasperreports-X.X.X.jar to project libraries.

   Called from PayrollController after payroll is saved.
 */
public class JasperReportGenerator {

    // Template path inside resources
    private static final String TEMPLATE_PATH = "/project/report/templates/payslip.jrxml";

    // Output directory (project root /reports/)
    private static final String OUTPUT_DIR = "reports/";

    /**
     * Generates a payslip PDF for a single payroll record.
     *
     * @param dto  PayrollDTO with all payroll details populated
     * @return     Path of the generated PDF file, or null on failure
     */
    public static String generatePayslip(PayrollDTO dto) {
        try {
            // 1. Load and compile .jrxml template
            InputStream templateStream = JasperReportGenerator.class.getResourceAsStream(TEMPLATE_PATH);
            if (templateStream == null) {
                System.err.println("[JasperReport] Template not found: " + TEMPLATE_PATH);
                return null;
            }
            JasperReport report = JasperCompileManager.compileReport(templateStream);

            // 2. Build parameters map (top-level labels/metadata)
            Map<String, Object> params = new HashMap<>();
            params.put("REPORT_TITLE", "PAYSLIP");
            params.put("COMPANY_NAME", "Payroll Management System");
            params.put("GENERATED_DATE", LocalDate.now().toString());

            // 3. Wrap DTO in a JRBeanCollectionDataSource
            //    The .jrxml should have fields matching PayrollDTO getters
            List<PayrollDTO> dataList = Collections.singletonList(dto);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);

            // 4. Fill report with data
            JasperPrint print = JasperFillManager.fillReport(report, params, dataSource);

            // 5. Ensure output directory exists
            File dir = new File(OUTPUT_DIR);
            if (!dir.exists()) dir.mkdirs();

            // 6. Export to PDF
            String fileName = OUTPUT_DIR + "payslip_emp" + dto.getEmployeeId()
                    + "_" + dto.getPayPeriodStart() + ".pdf";
            JasperExportManager.exportReportToPdfFile(print, fileName);

            System.out.println("[JasperReport] Payslip generated: " + fileName);
            return fileName;

        } catch (JRException e) {
            System.err.println("[JasperReport] Error generating payslip: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generates payslips for multiple employees (batch).
     *
     * @param dtoList  List of PayrollDTO records
     * @return         List of generated file paths
     */
    public static List<String> generateBatch(List<PayrollDTO> dtoList) {
        List<String> paths = new ArrayList<>();
        for (PayrollDTO dto : dtoList) {
            String path = generatePayslip(dto);
            if (path != null) paths.add(path);
        }
        System.out.printf("[JasperReport] Batch complete: %d payslips generated.%n", paths.size());
        return paths;
    }

}