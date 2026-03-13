package project.procedure;

import project.config.DbConfig;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;


public class PayrollProcedure {

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
