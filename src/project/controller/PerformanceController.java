package project.controller;


import project.dto.PerformanceDTO;
import project.service.EmployeeService;
import project.service.PerformanceService;
import project.util.InputUtil;

import java.time.LocalDate;
import java.util.List;

public class PerformanceController {
    private final PerformanceService performanceService;
    private final EmployeeService employeeService;
    private final InputUtil inputUtil;

    public PerformanceController() {
        this.performanceService = new PerformanceService();
        this.employeeService = new EmployeeService();
        this.inputUtil = new InputUtil();
    }

    public void showPerformanceMenu() {
        while (true) {
            System.out.println("\n=== PERFORMANCE MANAGEMENT SYSTEM ===");
            System.out.println("1. Add Performance Review");
            System.out.println("2. View Employee Performance");
            System.out.println("3. View All Performance Reviews");
            System.out.println("4. View Top Performers");
            System.out.println("5. Department Performance Summary");
            System.out.println("6. Back to Main Menu");

            int choice = inputUtil.readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    addPerformanceReview();
                    break;
                case 2:
                    viewEmployeePerformance();
                    break;
                case 3:
                    viewAllPerformanceReviews();
                    break;
                case 4:
                    viewTopPerformers();
                    break;
                case 5:
                    viewDepartmentSummary();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private void addPerformanceReview() {
        System.out.println("\n=== ADD PERFORMANCE REVIEW ===");

        int employeeId = inputUtil.readInt("Enter Employee ID: ");

        var employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            System.out.println("Employee not found!");
            return;
        }

        System.out.println("Employee: " + employee.getFirstName() + " " + employee.getLastName());
        System.out.println("Department: " + employee.getDepartment());

        PerformanceDTO performance = new PerformanceDTO();
        performance.setEmployeeId(employeeId);

        System.out.println("\nRating Scale: 1 (Poor) to 5 (Excellent)");
        performance.setRating(inputUtil.readInt("Enter rating (1-5): "));

        if (performance.getRating() < 1 || performance.getRating() > 5) {
            System.out.println("Invalid rating! Must be between 1 and 5.");
            return;
        }

        System.out.println("\nPerformance Categories:");
        System.out.println("1. Technical Skills");
        System.out.println("2. Communication");
        System.out.println("3. Teamwork");
        System.out.println("4. Leadership");
        System.out.println("5. Overall Performance");

        int category = inputUtil.readInt("Select category (1-5): ");
        String categoryStr = getCategoryString(category);
        if (categoryStr != null) {
            performance.setCategory(categoryStr);
        } else {
            performance.setCategory("Overall Performance");
        }

        System.out.println("\nEnter comments (press Enter for no comments):");
        performance.setComments(inputUtil.readString("Comments: "));

        performance.setReviewDate(LocalDate.now());
        performance.setReviewer("Admin"); // You might want to get this from AuthService

        if (performanceService.addPerformanceReview(performance)) {
            System.out.println("Performance review added successfully!");

            // Show updated average rating
            double avgRating = performanceService.getAverageRating(employeeId);
            System.out.println("Employee's new average rating: " + String.format("%.2f", avgRating));
        } else {
            System.out.println("Failed to add performance review!");
        }
    }

    private String getCategoryString(int category) {
        switch (category) {
            case 1: return "Technical Skills";
            case 2: return "Communication";
            case 3: return "Teamwork";
            case 4: return "Leadership";
            case 5: return "Overall Performance";
            default: return null;
        }
    }

    private void viewEmployeePerformance() {
        System.out.println("\n=== VIEW EMPLOYEE PERFORMANCE ===");

        int employeeId = inputUtil.readInt