package project.service;

import project.dao.EmployeeDao;
import project.dao.EmlpoyeeDaoImpl;
import project.dto.SalaryReportDTO;
import project.model.Employee;
import project.model.Payroll;
import project.report.JasperReportGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportService {
    private final EmployeeDao employeeDao;
    private final JasperReportGenerator reportGenerator;

    public ReportService() {
        this.employeeDao = new EmlpoyeeDaoImpl();
        this.reportGenerator = new JasperReportGenerator();
    }

    public boolean generateSalaryReport(int month, int year, String outputPath) {
        try {
            List<Employee> employees = employeeDao.findAll(null);

            List<SalaryReportDTO> reportData = employees.stream()
                    .map(emp -> {
                        Payroll payroll = emp.getPayrolls().stream()
                                .filter(p -> p.getMonth() == month && p.getYear() == year)
                                .findFirst()
                                .orElse(null);

                        SalaryReportDTO dto = new SalaryReportDTO();
                        dto.setEmployeeId(emp.getEmployeeId());
                        dto.setEmployeeName(emp.getFirstName() + " " + emp.getLastName());
                        dto.setDepartment(emp.getDepartment());
                        dto.setBasicSalary(emp.getSalary());

                        if (payroll != null) {
                            dto.setAllowances(payroll.getAllowances());
                            dto.setDeductions(payroll.getDeductions());
                            dto.setNetSalary(payroll.getNetSalary());
                            dto.setStatus(payroll.getStatus());
                        }

                        return dto;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", "Salary Report - " + month + "/" + year);
            parameters.put("MONTH", month);
            parameters.put("YEAR", year);
            parameters.put("TOTAL_EMPLOYEES", reportData.size());

            return reportGenerator.generateReport(reportData, parameters, outputPath);

        } catch (Exception e) {
            System.err.println("Error generating salary report: " + e.getMessage());
            return false;
        }
    }

    public boolean generateAttendanceReport(int month, int year, String outputPath) {
        try {
            List<Employee> employees = employeeDao.findAll(null);
            AttendanceService attendanceService = new AttendanceService();

            List<Map<String, Object>> reportData = employees.stream()
                    .map(emp -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("employeeId", emp.getEmployeeId());
                        data.put("employeeName", emp.getFirstName() + " " + emp.getLastName());
                        data.put("department", emp.getDepartment());

                        double attendancePercent = attendanceService.getAttendancePercentage(
                                emp.getEmployeeId(), month, year);
                        data.put("attendancePercentage", attendancePercent);

                        String status = attendancePercent >= 90 ? "Excellent" :
                                attendancePercent >= 75 ? "Good" :
                                        attendancePercent >= 60 ? "Average" : "Poor";
                        data.put("status", status);

                        return data;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", "Attendance Report - " + month + "/" + year);

            return reportGenerator.generateReport(reportData, parameters, outputPath);

        } catch (Exception e) {
            System.err.println("Error generating attendance report: " + e.getMessage());
            return false;
        }
    }
}{
}
