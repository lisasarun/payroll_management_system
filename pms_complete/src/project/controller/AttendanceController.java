package project.controller;

import project.dto.AttendanceDTO;
import project.model.Pagination;
import project.service.AttendanceService;
import project.service.EmployeeService;
import project.util.InputUtil;
import project.util.ViewUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AttendanceController {

    private final AttendanceService attService = new AttendanceService();
    private final EmployeeService   empService = new EmployeeService();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void checkIn(int employeeId) {
        System.out.println();
        ViewUtil.printTitle("CHECK IN");
        if (attService.checkIn(employeeId))
            ViewUtil.printSuccess("Checked in at " + java.time.LocalDateTime.now().format(FMT));

    }

    public void checkOut(int employeeId) {
        System.out.println();
        ViewUtil.printTitle("CHECK OUT");
        if (attService.checkOut(employeeId))
            ViewUtil.printSuccess("Checked out at " + java.time.LocalDateTime.now().format(FMT));

    }

    public void viewMyAttendance(int employeeId) {
        ViewUtil.printTitle("MY ATTENDANCE");
        List<AttendanceDTO> list = attService.getByEmployee(employeeId);
        if (list.isEmpty()) { ViewUtil.printInfo("No records found."); return; }
        ViewUtil.printAttendanceTable(list);
    }

    public void viewAllAttendance() {
        int total = attService.countAll();
        if (total == 0) { ViewUtil.printInfo("No attendance records found."); return; }

        int size = 5;
        Pagination pg = new Pagination(1, size, total);
        boolean running = true;

        while (running) {
            List<AttendanceDTO> list = attService.getAllPaged(pg.getPage(), size);
            ViewUtil.printTitle("ALL ATTENDANCE — Page " + pg.getPage() + " / " + pg.getTotalPages());
            System.out.printf("  %-6s %-20s %-10s %-8s %-19s %-19s%n",
                    "EmpID", "Name", "Date", "Status", "Check In", "Check Out");
            System.out.println("  " + "-".repeat(86));
            list.forEach(a -> System.out.printf("  %-6d %-20s %-10s %-8s %-19s %-19s%n",
                    a.getEmployeeId(),
                    a.getEmployeeName() != null ? a.getEmployeeName() : "—",
                    a.getDate(),
                    a.getStatus()    != null ? a.getStatus()             : "—",
                    a.getCheckIn()   != null ? a.getCheckIn().format(FMT)  : "—",
                    a.getCheckOut()  != null ? a.getCheckOut().format(FMT) : "—"));

            System.out.println();
            ViewUtil.printPaginationHint(pg.getPage(), pg.getTotalPages());
            System.out.print("  Option: ");
            String ch = InputUtil.readMenuChoice("").toUpperCase();
            switch (ch) {
                case "N" -> { if (pg.hasNext()) pg.next(); else ViewUtil.printInfo("Last page."); }
                case "P" -> { if (pg.hasPrev()) pg.prev(); else ViewUtil.printInfo("First page."); }
                case "0" -> running = false;
            }
        }
    }

    public void viewEmployeeAttendance() {
        int id = InputUtil.readInt("  Employee ID: ");
        
        if (id <= 0) {
            ViewUtil.printError("Invalid Employee ID. ID must be a positive number.");
            return;
        }
        
        if (empService.getById(id) == null) { ViewUtil.printError("Employee not found or disabled."); return; }
        List<AttendanceDTO> list = attService.getByEmployee(id);
        if (list.isEmpty()) { ViewUtil.printInfo("No attendance records."); return; }
        ViewUtil.printAttendanceTable(list);
    }
}
