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

        int employeeId = inputUtil.readInt("Enter Employee ID: ");

        var employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            System.out.println("Employee not found!");
            return;
        }

        System.out.println("\nEmployee: " + employee.getFirstName() + " " + employee.getLastName());
        System.out.println("Department: " + employee.getDepartment());
        System.out.println("Position: " + employee.getPosition());

        List<PerformanceDTO> performances = performanceService.getEmployeePerformances(employeeId);

        if (performances != null && !performances.isEmpty()) {
            System.out.println("\n=== PERFORMANCE HISTORY ===");
            System.out.println("Date\t\tRating\tCategory\t\tReviewer\tComments");
            System.out.println("------------------------------------------------------------------------");

            for (PerformanceDTO p : performances) {
                String stars = getStarRating(p.getRating());
                System.out.printf("%s\t%d %s\t%-20s\t%s\t%s\n",
                        p.getReviewDate(),
                        p.getRating(),
                        stars,
                        p.getCategory() != null ? p.getCategory() : "General",
                        p.getReviewer(),
                        p.getComments() != null ? (p.getComments().length() > 20 ?
                                p.getComments().substring(0, 17) + "..." : p.getComments()) : "-");
            }

            double avgRating = performanceService.getAverageRating(employeeId);
            System.out.println("\n" + getRatingDescription(avgRating));
            System.out.println("Overall Average Rating: " + String.format("%.2f", avgRating) +
                    " " + getStarRating((int)Math.round(avgRating)));

        } else {
            System.out.println("No performance reviews found for this employee!");
        }
    }

    private void viewAllPerformanceReviews() {
        System.out.println("\n=== ALL PERFORMANCE REVIEWS ===");

        var employees = employeeService.getAllEmployees(null);

        if (employees == null || employees.isEmpty()) {
            System.out.println("No employees found!");
            return;
        }

        System.out.println("\nRecent Performance Reviews:");
        System.out.println("Emp ID\tEmployee Name\t\tDate\t\tRating\tCategory");
        System.out.println("------------------------------------------------------------------------");

        for (var emp : employees) {
            List<PerformanceDTO> performances = performanceService.getEmployeePerformances(emp.getEmployeeId());

            if (performances != null && !performances.isEmpty()) {
                // Show only the most recent review
                PerformanceDTO latest = performances.get(performances.size() - 1);
                String stars = getStarRating(latest.getRating());

                System.out.printf("%d\t%-20s\t%s\t%d %s\t%s\n",
                        emp.getEmployeeId(),
                        emp.getFirstName() + " " + emp.getLastName(),
                        latest.getReviewDate(),
                        latest.getRating(),
                        stars,
                        latest.getCategory() != null ? latest.getCategory() : "General");
            }
        }
    }

    private void viewTopPerformers() {
        System.out.println("\n=== TOP PERFORMERS ===");

        int year = inputUtil.readInt("Enter year: ");

        List<PerformanceDTO> topPerformers = performanceService.getTopPerformers(year);

        if (topPerformers != null && !topPerformers.isEmpty()) {
            System.out.println("\nTop Performers of " + year);
            System.out.println("========================================");
            System.out.printf("%-5s %-20s %-10s %-10s %s\n",
                    "ID", "Employee", "Rating", "Date", "Comments");
            System.out.println("------------------------------------------------------------------------");

            for (PerformanceDTO p : topPerformers) {
                var emp = employeeService.getEmployeeById(p.getEmployeeId());
                String empName = emp != null ? emp.getFirstName() + " " + emp.getLastName() : "Unknown";
                String stars = getStarRating(p.getRating());

                System.out.printf("%-5d %-20s %d %s\t%s\t%s\n",
                        p.getEmployeeId(),
                        empName.length() > 20 ? empName.substring(0, 17) + "..." : empName,
                        p.getRating(),
                        stars,
                        p.getReviewDate(),
                        p.getComments() != null ? (p.getComments().length() > 20 ?
                                p.getComments().substring(0, 17) + "..." : p.getComments()) : "-");
            }

            // Group by department
            System.out.println("\n=== SUMMARY BY DEPARTMENT ===");
            var byDepartment = topPerformers.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            p -> {
                                var emp = employeeService.getEmployeeById(p.getEmployeeId());
                                return emp != null ? emp.getDepartment() : "Unknown";
                            },
                            java.util.stream.Collectors.counting()
                    ));

            byDepartment.forEach((dept, count) -> {
                System.out.println(dept + ": " + count + " top performer(s)");
            });

        } else {
            System.out.println("No top performers found for year " + year + "!");
        }
    }

    private void viewDepartmentSummary() {
        System.out.println("\n=== DEPARTMENT PERFORMANCE SUMMARY ===");

        var employees = employeeService.getAllEmployees(null);

        if (employees == null || employees.isEmpty()) {
            System.out.println("No employees found!");
            return;
        }

        // Group employees by department
        var byDepartment = employees.stream()
                .collect(java.util.stream.Collectors.groupingBy(EmployeeDTO::getDepartment));

        System.out.println("\nDepartment Performance Summary:");
        System.out.println("========================================");
        System.out.printf("%-20s %-10s %-10s %-10s\n",
                "Department", "Employees", "Avg Rating", "Performance");
        System.out.println("------------------------------------------------------------------------");

        for (var entry : byDepartment.entrySet()) {
            String department = entry.getKey();
            List<EmployeeDTO> deptEmployees = entry.getValue();

            double totalRating = 0;
            int ratedEmployees = 0;

            for (var emp : deptEmployees) {
                double avgRating = performanceService.getAverageRating(emp.getEmployeeId());
                if (avgRating > 0) {
                    totalRating += avgRating;
                    ratedEmployees++;
                }
            }

            double deptAvgRating = ratedEmployees > 0 ? totalRating / ratedEmployees : 0;
            String performanceLevel = getPerformanceLevel(deptAvgRating);

            System.out.printf("%-20s %-10d %-10.2f %s\n",
                    department,
                    deptEmployees.size(),
                    deptAvgRating,
                    performanceLevel);
        }
    }

    private String getStarRating(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("⭐");
        }
        return stars.toString();
    }

    private String getRatingDescription(double avgRating) {
        if (avgRating >= 4.5) return "🌟 Excellent Performer";
        if (avgRating >= 3.5) return "👍 Good Performer";
        if (avgRating >= 2.5) return "👌 Average Performer";
        if (avgRating >= 1.5) return "⚠️ Needs Improvement";
        return "❌ Poor Performer";
    }

    private String getPerformanceLevel(double rating) {
        if (rating >= 4.0) return "Excellent";
        if (rating >= 3.0) return "Good";
        if (rating >= 2.0) return "Average";
        if (rating >= 1.0) return "Below Average";
        return "Not Rated";
    }
}