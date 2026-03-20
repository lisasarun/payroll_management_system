package project.controller;

import project.dto.EmployeeDTO;
import project.dto.PerformanceDTO;
import project.service.EmployeeService;
import project.service.PerformanceService;
import project.util.InputUtil;
import project.util.ViewUtil;

import java.util.List;

public class PerformanceController {

    private final PerformanceService perfService = new PerformanceService();
    private final EmployeeService    empService  = new EmployeeService();

    public void managePerformance(int reviewerId) {
        boolean running = true;
        while (running) {
            ViewUtil.printTitle("PERFORMANCE MANAGEMENT");
            System.out.println("  [1] Add Performance Review");
            System.out.println("  [2] View Employee Performance");
            System.out.println("  [3] Employees Without Review");
            System.out.println("  [0] Back");
            System.out.print("  Select: ");
            switch (InputUtil.readMenuChoice("")) {
                case "1" -> addReview(reviewerId);
                case "2" -> viewAllEmployeesWithReviewStatus();
                case "3" -> viewEmployeesWithoutReview();
                case "0" -> running = false;
                default  -> ViewUtil.printError("Invalid option.");
            }
        }
    }

    private void addReview(int reviewerId) {
        ViewUtil.printTitle("ADD PERFORMANCE REVIEW");
        int id = InputUtil.readInt("  Employee ID   : ");

        EmployeeDTO emp = empService.getById(id);
        if (emp == null) { ViewUtil.printError("Employee not found."); return; }

        System.out.println("  Employee: " + emp.getFullName());
        double score = InputUtil.readScore("  Score");

        // Comments are optional — press Enter to skip
        System.out.print("  Comments (leave blank to skip): ");
        String comments = InputUtil.readOptionalString("");
        if (comments.isEmpty()) comments = null;

        if (perfService.addReview(id, score, comments, reviewerId))
            ViewUtil.printSuccess("Performance review saved.");
        else
            ViewUtil.printError("Failed to save review.");
    }

    /**
     * Shows all active employees paginated (5/page) with a ✔/✘ sign
     * indicating whether they have received a performance review.
     * Selecting an employee ID shows their full review history.
     */
    private void viewAllEmployeesWithReviewStatus() {
        int total = empService.countAll();
        if (total == 0) { ViewUtil.printInfo("No employees found."); return; }

        java.util.Set<Integer> reviewed = perfService.getEmployeeIdsWithReviews();
        int size = 5;
        project.model.Pagination pg = new project.model.Pagination(1, size, total);

        while (true) {
            java.util.List<EmployeeDTO> list = empService.getAllPaged(pg.getPage(), size);
            ViewUtil.printTitle("ALL EMPLOYEES — Page " + pg.getPage() + " / " + pg.getTotalPages());
            System.out.printf("  %-4s %-3s %-25s %-28s%n", "ID", "Rev", "Full Name", "Email");
            System.out.println("  " + "-".repeat(64));
            for (EmployeeDTO e : list) {
                String mark = reviewed.contains(e.getEmployeeId()) ? "✔" : "✘";
                System.out.printf("  %-4d %-3s %-25s %-28s%n",
                        e.getEmployeeId(), mark,
                        trunc(e.getFullName(), 25), trunc(e.getEmail(), 28));
            }
            System.out.println("  " + "-".repeat(64));
            System.out.println("  ✔ = has review   ✘ = no review yet");
            System.out.println();
            if (pg.getTotalPages() > 1)
                System.out.print("  [N] Next  [P] Prev  [ID] View reviews  [0] Back: ");
            else
                System.out.print("  [ID] View reviews  [0] Back: ");

            String ch = InputUtil.readMenuChoice("").toUpperCase();
            if (ch.equals("0")) break;
            else if (ch.equals("N")) { if (pg.hasNext()) pg.next(); else ViewUtil.printInfo("Already on last page."); }
            else if (ch.equals("P")) { if (pg.hasPrev()) pg.prev(); else ViewUtil.printInfo("Already on first page."); }
            else {
                try {
                    int empId = Integer.parseInt(ch);
                    viewEmployeePerformanceById(empId);
                } catch (NumberFormatException e) {
                    ViewUtil.printInfo("Enter N, P, an Employee ID, or 0.");
                }
            }
        }
    }

    /** Shows all employees who have never received a performance review. */
    private void viewEmployeesWithoutReview() {
        java.util.List<EmployeeDTO> list = perfService.getEmployeesWithoutReview();
        ViewUtil.printTitle("EMPLOYEES WITHOUT PERFORMANCE REVIEW");
        if (list.isEmpty()) { ViewUtil.printInfo("All employees have been reviewed."); return; }
        System.out.printf("  %-4s %-25s %-28s%n", "ID", "Full Name", "Email");
        System.out.println("  " + "-".repeat(60));
        list.forEach(e -> System.out.printf("  %-4d %-25s %-28s%n",
                e.getEmployeeId(), trunc(e.getFullName(), 25), trunc(e.getEmail(), 28)));
        System.out.println("  " + "-".repeat(60));
        System.out.printf("  Total: %d employee(s) pending review.%n%n", list.size());
    }

    private void viewEmployeePerformanceById(int id) {
        EmployeeDTO emp = empService.getById(id);
        if (emp == null) { ViewUtil.printError("Employee not found."); return; }

        java.util.List<PerformanceDTO> list = perfService.getByEmployee(id);
        ViewUtil.printTitle("PERFORMANCE — " + emp.getFullName());
        if (list.isEmpty()) { ViewUtil.printInfo("No reviews found."); return; }
        list.forEach(ViewUtil::printPerformanceRecord);
        double avg = perfService.getAverageScore(id);
        System.out.printf("%n  Average Score: %.2f%n", avg);
    }

    private static String trunc(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    public void viewMyPerformance(int employeeId) {
        List<PerformanceDTO> list = perfService.getByEmployee(employeeId);
        ViewUtil.printTitle("MY PERFORMANCE REVIEWS");
        if (list.isEmpty()) { ViewUtil.printInfo("No performance reviews yet."); return; }
        list.forEach(ViewUtil::printPerformanceRecord);

        double avg = perfService.getAverageScore(employeeId);
        System.out.printf("%n  Your Average Score: %.2f%n", avg);

        if      (avg >= 90) ViewUtil.printSuccess("Excellent performance! (15% bonus eligible)");
        else if (avg >= 80) ViewUtil.printSuccess("Good performance! (10% bonus eligible)");
        else if (avg >= 75) ViewUtil.printSuccess("Satisfactory. (5% bonus eligible)");
        else                ViewUtil.printInfo("Keep improving! (No bonus this cycle)");
    }
}
