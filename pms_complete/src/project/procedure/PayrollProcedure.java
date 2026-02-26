package project.procedure;

import project.config.DbConfig;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Calls the calculate_payroll stored procedure in PostgreSQL.
 * Usage: PayrollProcedure.calculate(employeeId, periodStart, periodEnd, bonus, deductions)
 */
public class PayrollProcedure {

    public static boolean calculate(int employeeId,
                                    LocalDate periodStart,
                                    LocalDate periodEnd,
                                    double bonus,
                                    double deductions) {
        String sql = "CALL calculate_payroll(?, ?, ?, ?, ?)";
        try (Connection c = DbConfig.getConnection();
             CallableStatement cs = c.prepareCall(sql)) {
            cs.setInt(1, employeeId);
            cs.setDate(2, Date.valueOf(periodStart));
            cs.setDate(3, Date.valueOf(periodEnd));
            cs.setDouble(4, bonus);
            cs.setDouble(5, deductions);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("[PayrollProcedure] calculate: " + e.getMessage());
            return false;
        }
    }
}
