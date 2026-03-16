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
            System.out.println("  [0] Back");
            System.out.print("  Select: ");
            switch (InputUtil.readMenuChoice("")) {
                case "1" -> addReview(reviewerId);
                case "2" -> viewEmployeePerformance();
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
        double score    = InputUtil.readScore("  Score");
        String comments = InputUtil.readString("  Comments      : ");

        if (perfService.addReview(id, score, comments, reviewerId))
            ViewUtil.printSuccess("Performance review saved.");
        else
            ViewUtil.printError("Failed to save review.");
    }

    private void viewEmployeePerformance() {
        int id = InputUtil.readInt("  Employee ID: ");
        EmployeeDTO emp = empService.getById(id);
        if (emp == null) { ViewUtil.printError("Employee not found."); return; }

        List<PerformanceDTO> list = perfService.getByEmployee(id);
        ViewUtil.printTitle("PERFORMANCE — " + emp.getFullName());

        if (list.isEmpty()) { ViewUtil.printInfo("No reviews found."); return; }
        list.forEach(ViewUtil::printPerformanceRecord);

        double avg = perfService.getAverageScore(id);
        System.out.printf("%n  Average Score: %.2f%n", avg);
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
