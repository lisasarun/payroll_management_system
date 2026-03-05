package project.procedure;

import project.config.DbConfig;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 Calls the PostgreSQL stored procedure: calculate_payroll()
 Uses BigDecimal (not double) to avoid floating-point precision loss.

 SQL: CALL calculate_payroll(employee_id, period_start, period_end, bonus, deductions)
 */
public class PayrollProcedure {

    /**
     * Execute Payroll procedure with a provided connection (for transactions).
     */
    public static boolean calculateWithConnection(Connection conn,
                                                   int employeeId,
                                                   LocalDate periodStart,
                                                   LocalDate periodEnd,
                                                   BigDecimal bonus,
                                                   BigDecimal deductions) {
        String sql = "CALL calculate_payroll(?, ?, ?, ?, ?)";
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, employeeId);
            cs.setDate(2, Date.valueOf(periodStart));
            cs.setDate(3, Date.valueOf(periodEnd));
            cs.setBigDecimal(4, bonus);
            cs.setBigDecimal(5, deductions);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("[PayrollProcedure] calculate: " + e.getMessage());
            return false;
        }
    }

    /**
     * Legacy method - creates own connection (deprecated, use calculateWithConnection).
     */
    @Deprecated
    public static boolean calculate(int employeeId,
                                    LocalDate periodStart,
                                    LocalDate periodEnd,
                                    BigDecimal bonus,
                                    BigDecimal deductions) {
        try (Connection c = DbConfig.getConnection()) {
            return calculateWithConnection(c, employeeId, periodStart, periodEnd, bonus, deductions);
        } catch (SQLException e) {
            System.err.println("[PayrollProcedure] calculate: " + e.getMessage());
            return false;
        }
    }
}
