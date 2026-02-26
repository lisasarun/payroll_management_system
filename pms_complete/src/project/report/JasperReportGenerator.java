package project.report;

import project.model.Payslip;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Generates payslip text files.
 * NOTE: JasperReports library is optional — this class produces a formatted .txt payslip
 * which works without adding the JasperReports JAR dependency.
 * To upgrade to PDF output, add jasperreports JAR and replace generateTxtPayslip() with PDF rendering.
 */
public class JasperReportGenerator {

    private static final String OUTPUT_DIR = "reports/";

    public String generatePayslip(Payslip payslip) {
        // Ensure output directory exists
        new java.io.File(OUTPUT_DIR).mkdirs();

        String filename = OUTPUT_DIR + "payslip_emp" + payslip.getEmployeeId()
                + "_" + payslip.getPayPeriodStart() + ".txt";

        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.print(payslip.toString());
            pw.printf("  Generated: %s%n",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            System.out.println("  Payslip saved to: " + filename);
            return filename;
        } catch (Exception e) {
            System.err.println("[Report] Failed to save payslip: " + e.getMessage());
            return null;
        }
    }
}
