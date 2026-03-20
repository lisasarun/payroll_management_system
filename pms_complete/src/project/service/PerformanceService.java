package project.service;

import project.dto.PerformanceDTO;
import project.mapper.EntityMapper;
import project.model.Employee;
import project.model.Performance;
import project.repository.EmployeeRepository;
import project.repository.PerformanceRepository;

import java.util.List;
import java.util.stream.Collectors;

public class PerformanceService {

    private final PerformanceRepository perfRepo = new PerformanceRepository();
    private final EmployeeRepository    empRepo  = new EmployeeRepository();

    public boolean addReview(int employeeId, double score, String comments, int reviewerId) {
        Employee emp = empRepo.findById(employeeId);
        if (emp == null) { System.out.println("  Employee not found."); return false; }

        Performance p = new Performance();
        p.setEmployeeId(employeeId);
        p.setReviewDate(java.time.LocalDate.now());
        p.setScore(java.math.BigDecimal.valueOf(score));
        p.setComments(comments);
        p.setReviewerId(reviewerId);
        return perfRepo.save(p);
    }

    public List<PerformanceDTO> getByEmployee(int employeeId) {
        Employee emp = empRepo.findById(employeeId);
        return perfRepo.findByEmployee(employeeId).stream()
                .map(p -> {
                    PerformanceDTO dto = EntityMapper.toPerformanceDTO(p);
                    if (emp != null) dto.setEmployeeName(emp.getFullName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public PerformanceDTO getLatest(int employeeId) {
        Performance p = perfRepo.findLatestByEmployee(employeeId);
        if (p == null) return null;
        PerformanceDTO dto = EntityMapper.toPerformanceDTO(p);
        Employee emp = empRepo.findById(employeeId);
        if (emp != null) dto.setEmployeeName(emp.getFullName());
        return dto;
    }

    public double getAverageScore(int employeeId) {
        return perfRepo.getAverageScore(employeeId);
    }

    /** Returns all active employees who have never received a performance review. */
    public List<project.dto.EmployeeDTO> getEmployeesWithoutReview() {
        java.util.Set<Integer> reviewed = perfRepo.findEmployeeIdsWithReviews();
        return empRepo.findAllOrdered(1, Integer.MAX_VALUE, "ID").stream()
                .filter(e -> !reviewed.contains(e.getEmployeeId()))
                .map(project.mapper.EntityMapper::toEmployeeDTO)
                .collect(Collectors.toList());
    }

    /** Returns the set of employee IDs that have at least one review. */
    public java.util.Set<Integer> getEmployeeIdsWithReviews() {
        return perfRepo.findEmployeeIdsWithReviews();
    }
}
