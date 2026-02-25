package project.service;
import project.dao.EmployeeDao;
import project.dao.EmlpoyeeDaoImpl;
import project.dto.PerformanceDTO;
import project.mapper.EntityMapper;
import project.model.Employee;
import project.model.Performance;

import java.util.List;
import java.util.stream.Collectors;

public class PerformanceService {
    private final EmployeeDao employeeDao;

    public PerformanceService() {
        this.employeeDao = new EmlpoyeeDaoImpl();
    }

    public boolean addPerformanceReview(PerformanceDTO performanceDTO) {
        try {
            Performance performance = EntityMapper.toPerformance(performanceDTO);

            Employee employee = employeeDao.findById(performanceDTO.getEmployeeId());
            if (employee != null) {
                employee.addPerformance(performance);
                return employeeDao.update(employee);
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error adding performance review: " + e.getMessage());
            return false;
        }
    }

    public List<PerformanceDTO> getEmployeePerformances(int employeeId) {
        try {
            Employee employee = employeeDao.findById(employeeId);
            if (employee != null) {
                return employee.getPerformances().stream()
                        .map(EntityMapper::toPerformanceDTO)
                        .collect(Collectors.toList());
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error fetching performances: " + e.getMessage());
            return null;
        }
    }

    public double getAverageRating(int employeeId) {
        try {
            Employee employee = employeeDao.findById(employeeId);
            if (employee != null && !employee.getPerformances().isEmpty()) {
                return employee.getPerformances().stream()
                        .mapToInt(Performance::getRating)
                        .average()
                        .orElse(0.0);
            }
            return 0.0;
        } catch (Exception e) {
            System.err.println("Error calculating average rating: " + e.getMessage());
            return 0.0;
        }
    }

    public List<PerformanceDTO> getTopPerformers(int year) {
        try {
            List<Employee> employees = employeeDao.findAll(null);
            return employees.stream()
                    .flatMap(e -> e.getPerformances().stream())
                    .filter(p -> p.getReviewDate().getYear() == year && p.getRating() >= 4)
                    .map(EntityMapper::toPerformanceDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching top performers: " + e.getMessage());
            return null;
        }
    }
}
