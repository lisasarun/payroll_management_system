package project.controller;


import project.dto.AttendanceDTO;
import project.service.AttendanceService;
import project.service.EmployeeService;
import project.util.InputUtil;
import project.util.DateUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

    public class AttendanceController {
        private final AttendanceService attendanceService;
        private final EmployeeService employeeService;
        private final InputUtil inputUtil;

        public AttendanceController() {
            this.attendanceService = new AttendanceService();
            this.employeeService = new EmployeeService();
            this.inputUtil = new InputUtil();
        }

        public void showAttendanceMenu() {
            while (true) {
                System.out.println("\n=== ATTENDANCE MANAGEMENT SYSTEM ===");
                System.out.println("1. Mark Attendance");
                System.out.println("2. Bulk Mark Attendance");
                System.out.println("3. View Daily Attendance");
                System.out.println("4. View Monthly Attendance Report");
                System.out.println("5. View Employee Attendance Summary");
                System.out.println("6. Export Attendance Report");
                System.out.println("7. Back to Main Menu");

                int choice = inputUtil.readInt("Enter your choice: ");

                switch (choice) {
                    case 1:
                        markAttendance();
                        break;
                    case 2:
                        bulkMarkAttendance();
                        break;
                    case 3:
                        viewDailyAttendance();
                        break;
                    case 4:
                        viewMonthlyAttendanceReport();
                        break;
                    case 5:
                        viewEmployeeAttendanceSummary();
                        break;
                    case 6:
                        exportAttendanceReport();
                        break;
                    case 7:
                        return;
                    default:
                        System.out.println("Invalid choice!");
                }
            }
        }

        private void markAttendance() {
            System.out.println("\n=== MARK INDIVIDUAL ATTENDANCE ===");

            int employeeId = inputUtil.readInt("Enter Employee ID: ");

            // Verify employee exists
            var employee = employeeService.getEmployeeById(employeeId);
            if (employee == null) {
                System.out.println("Employee not found!");
                return;
            }

            System.out.println("Employee: " + employee.getFirstName() + " " + employee.getLastName());
            System.out.println("Date: " + LocalDate.now());

            AttendanceDTO attendance = new AttendanceDTO();
            attendance.setEmployeeId(employeeId);
            attendance.setDate(LocalDate.now());

            System.out.println("\nSelect Status:");
            System.out.println("1. Present");
            System.out.println("2. Absent");
            System.out.println("3. Late");
            System.out.println("4. Half Day");
            System.out.println("5. Leave");

            int statusChoice = inputUtil.readInt("Enter status: ");

            switch (statusChoice) {
                case 1:
                    attendance.setStatus("PRESENT");
                    attendance.setCheckIn(LocalTime.of(9, 0));
                    attendance.setCheckOut(LocalTime.of(17, 0));
                    break;
                case 2:
                    attendance.setStatus("ABSENT");
                    break;
                case 3:
                    attendance.setStatus("LATE");
                    attendance.setCheckIn(LocalTime.of(9, 30));
                    attendance.setCheckOut(LocalTime.of(17, 0));
                    break;
                case 4:
                    attendance.setStatus("HALF_DAY");
                    attendance.setCheckIn(LocalTime.of(9, 0));
                    attendance.setCheckOut(LocalTime.of(13, 0));
                    break;
                case 5:
                    attendance.setStatus("LEAVE");
                    break;
                default:
                    System.out.println("Invalid status!");
                    return;
            }

            attendance.setRemarks(inputUtil.readString("Remarks (optional): "));

            if (attendanceService.markAttendance(attendance)) {
                System.out.println("Attendance marked successfully!");
            } else {
                System.out.println("Failed to mark attendance!");
            }
        }

        private void bulkMarkAttendance() {
            System.out.println("\n=== BULK MARK ATTENDANCE ===");

            LocalDate date = inputUtil.readDate("Enter date (yyyy-mm-dd): ");

            List<AttendanceDTO> existingAttendance = attendanceService.getAttendanceByDate(date);
            if (existingAttendance != null && !existingAttendance.isEmpty()) {
                System.out.println("Attendance already marked for this date!");
                return;
            }

            var employees = employeeService.getAllEmployees(null);

            if (employees == null || employees.isEmpty()) {
                System.out.println("No employees found!");
                return;
            }

            System.out.println("Marking attendance for " + employees.size() + " employees");
            System.out.println("Default status will be PRESENT for all employees");
            System.out.println("You can modify individual records later");

            String confirm = inputUtil.readString("Proceed? (yes/no): ");

            if (confirm.equalsIgnoreCase("yes")) {
                int successCount = 0;

                for (var employee : employees) {
                    AttendanceDTO attendance = new AttendanceDTO();
                    attendance.setEmployeeId(employee.getEmployeeId());
                    attendance.setDate(date);
                    attendance.setStatus("PRESENT");
                    attendance.setCheckIn(LocalTime.of(9, 0));
                    attendance.setCheckOut(LocalTime.of(17, 0));
                    attendance.setRemarks("Bulk marked");

                    if (attendanceService.markAttendance(attendance)) {
                        successCount++;
                    }
                }

                System.out.println("Attendance marked for " + successCount + " out of " + employees.size() + " employees");
            }
        }

        private void viewDailyAttendance() {
            System.out.println("\n=== DAILY ATTENDANCE REPORT ===");

            LocalDate date = inputUtil.readDate("Enter date (yyyy-mm-dd): ");

            var attendanceList = attendanceService.getAttendanceByDate(date);

            if (attendanceList != null && !attendanceList.isEmpty()) {
                System.out.println("\nAttendance for " + date);
                System.out.println("========================================");
                System.out.printf("%-5s %-20s %-10s %-8s %-8s %s\n",
                        "ID", "Employee", "Status", "Check In", "Check Out", "Remarks");
                System.out.println("--------------------------------------------------------");

                int present = 0, absent = 0, late = 0, halfDay = 0, leave = 0;

                for (AttendanceDTO a : attendanceList) {
                    var emp = employeeService.getEmployeeById(a.getEmployeeId());
                    String empName = emp != null ? emp.getFirstName() + " " + emp.getLastName() : "Unknown";

                    String checkIn = a.getCheckIn() != null ? a.getCheckIn().toString() : "-";
                    String checkOut = a.getCheckOut() != null ? a.getCheckOut().toString() : "-";

                    System.out.printf("%-5d %-20s %-10s %-8s %-8s %s\n",
                            a.getEmployeeId(),
                            empName.length() > 20 ? empName.substring(0, 17) + "..." : empName,
                            a.getStatus(),
                            checkIn,
                            checkOut,
                            a.getRemarks() != null ? a.getRemarks() : "-");

                    // Count statistics
                    switch (a.getStatus()) {
                        case "PRESENT": present++; break;
                        case "ABSENT": absent++; break;
                        case "LATE": late++; break;
                        case "HALF_DAY": halfDay++; break;
                        case "LEAVE": leave++; break;
                    }
                }

                System.out.println("\n=== SUMMARY ===");
                System.out.println("Total Employees: " + attendanceList.size());
                System.out.println("Present: " + present);
                System.out.println("Absent: " + absent);
                System.out.println("Late: " + late);
                System.out.println("Half Day: " + halfDay);
                System.out.println("Leave: " + leave);
                System.out.println("Attendance Rate: " + String.format("%.2f", ((present + late) * 100.0) / attendanceList.size()) + "%");

            } else {
                System.out.println("No attendance records for this date!");
            }
        }

        private void viewMonthlyAttendanceReport() {
            System.out.println("\n=== MONTHLY ATTENDANCE REPORT ===");

            int month = inputUtil.readInt("Enter month (1-12): ");
            int year = inputUtil.readInt("Enter year: ");

            var employees = employeeService.getAllEmployees(null);

            if (employees == null || employees.isEmpty()) {
                System.out.println("No employees found!");
                return;
            }

            System.out.println("\nMonthly Attendance Summary - " + month + "/" + year);
            System.out.println("==========================================");
            System.out.printf("%-5s %-20s %-10s %-10s %-10s\n",
                    "ID", "Employee", "Present", "Absent", "Percentage");
            System.out.println("------------------------------------------------");

            for (var emp : employees) {
                double percentage = attendanceService.getAttendancePercentage(emp.getEmployeeId(), month, year);

                // Calculate present days (simplified - you might want to get actual count)
                int workingDays = DateUtil.getWorkingDaysInMonth(month, year);
                int presentDays = (int) Math.round((percentage * workingDays) / 100);
                int absentDays = workingDays - presentDays;

                System.out.printf("%-5d %-20s %-10d %-10d %.2f%%\n",
                        emp.getEmployeeId(),
                        emp.getFirstName() + " " + emp.getLastName(),
                        presentDays,
                        absentDays,
                        percentage);
            }
        }

        private void viewEmployeeAttendanceSummary() {
            System.out.println("\n=== EMPLOYEE ATTENDANCE SUMMARY ===");

            int employeeId = inputUtil.readInt("Enter Employee ID: ");

            var employee = employeeService.getEmployeeById(employeeId);
            if (employee == null) {
                System.out.println("Employee not found!");
                return;
            }

            System.out.println("Employee: " + employee.getFirstName() + " " + employee.getLastName());

            int year = inputUtil.readInt("Enter year: ");

            System.out.println("\nMonthly Attendance Summary for " + year);
            System.out.println("==========================================");
            System.out.printf("%-10s %-10s %-10s %-10s\n",
                    "Month", "Present", "Working Days", "Percentage");
            System.out.println("------------------------------------------------");

            for (int month = 1; month <= 12; month++) {
                double percentage = attendanceService.getAttendancePercentage(employeeId, month, year);
                int workingDays = DateUtil.getWorkingDaysInMonth(month, year);
                int presentDays = (int) Math.round((percentage * workingDays) / 100);

                System.out.printf("%-10d %-10d %-10d %.2f%%\n",
                        month,
                        presentDays,
                        workingDays,
                        percentage);
            }
        }

        private void exportAttendanceReport() {
            System.out.println("\n=== EXPORT ATTENDANCE REPORT ===");

            System.out.println("Select Report Type:");
            System.out.println("1. Daily Report");
            System.out.println("2. Monthly Report");

            int type = inputUtil.readInt("Enter choice: ");

            if (type == 1) {
                LocalDate date = inputUtil.readDate("Enter date (yyyy-mm-dd): ");
                String filename = "attendance_report_" + date + ".csv";

                var attendanceList = attendanceService.getAttendanceByDate(date);

                if (attendanceList != null && !attendanceList.isEmpty()) {
                    // Simple CSV export
                    try (java.io.PrintWriter writer = new java.io.PrintWriter(filename)) {
                        writer.println("Employee ID,Employee Name,Status,Check In,Check Out,Remarks");

                        for (AttendanceDTO a : attendanceList) {
                            var emp = employeeService.getEmployeeById(a.getEmployeeId());
                            String empName = emp != null ? emp.getFirstName() + " " + emp.getLastName() : "Unknown";

                            writer.printf("%d,\"%s\",%s,%s,%s,\"%s\"\n",
                                    a.getEmployeeId(),
                                    empName,
                                    a.getStatus(),
                                    a.getCheckIn() != null ? a.getCheckIn() : "",
                                    a.getCheckOut() != null ? a.getCheckOut() : "",
                                    a.getRemarks() != null ? a.getRemarks() : "");
                        }

                        System.out.println("Report exported to: " + filename);
                    } catch (Exception e) {
                        System.out.println("Error exporting report: " + e.getMessage());
                    }
                } else {
                    System.out.println("No data to export!");
                }
            } else if (type == 2) {
                int month = inputUtil.readInt("Enter month (1-12): ");
                int year = inputUtil.readInt("Enter year: ");
                String filename = "attendance_report_" + month + "_" + year + ".csv";

                // Similar logic for monthly export
                System.out.println("Monthly export not yet implemented!");
            }
        }
    }
}
