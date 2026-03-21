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

        if (perfRepo.hasReviewToday(employeeId)) {
            System.out.println("  ✘ Employee already has a performance review today.");
            System.out.println("  Multiple reviews on the same day are not allowed.");
            return false;
        }

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


    public List<project.dto.EmployeeDTO> getEmployeesWithoutReview() {
        java.util.Set<Integer> reviewed = perfRepo.findEmployeeIdsWithReviews();
        return empRepo.findAllOrdered(1, Integer.MAX_VALUE, "ID").stream()
                .filter(e -> !reviewed.contains(e.getEmployeeId()))
                .map(project.mapper.EntityMapper::toEmployeeDTO)
                .collect(Collectors.toList());
    }


    public java.util.Set<Integer> getEmployeeIdsWithReviews() {
        return perfRepo.findEmployeeIdsWithReviews();
    }


    public boolean updateReview(int performanceId, double score, String comments) {
        return perfRepo.update(performanceId, score, comments);
    }


    public boolean deleteReview(int performanceId) {
        return perfRepo.delete(performanceId);
    }


    public PerformanceDTO getReviewById(int performanceId) {
        Performance p = perfRepo.findById(performanceId);
        if (p == null) return null;
        PerformanceDTO dto = EntityMapper.toPerformanceDTO(p);
        Employee emp = empRepo.findById(p.getEmployeeId());
        if (emp != null) dto.setEmployeeName(emp.getFullName());
        return dto;
    }
}
