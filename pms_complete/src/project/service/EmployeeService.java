package project.service;

import project.dto.EmployeeDTO;
import project.mapper.EntityMapper;
import project.model.Employee;
import project.repository.EmployeeRepository;
import project.util.SalaryCalculator;

import java.util.List;
import java.util.stream.Collectors;

public class EmployeeService {

    private final EmployeeRepository repo = new EmployeeRepository();

    public boolean addEmployee(Employee emp) {
        if (repo.emailExists(emp.getEmail())) {
            System.out.println("  Email already exists.");
            return false;
        }

        if (!validateSalaryForPosition(emp.getPosition(), emp.getBaseSalary())) {
            return false;
        }
        
        return repo.save(emp);
    }

    public boolean updateEmployee(Employee emp) {

        if (!validateSalaryForPosition(emp.getPosition(), emp.getBaseSalary())) {
            return false;
        }
        
        return repo.update(emp);
    }

    public boolean updateEmployeeWithEmailCheck(Employee emp, String originalEmail) {
        if (!emp.getEmail().equalsIgnoreCase(originalEmail) && repo.emailExists(emp.getEmail())) {
            System.out.println("  Email already in use by another employee.");
            return false;
        }
        

        if (!validateSalaryForPosition(emp.getPosition(), emp.getBaseSalary())) {
            return false;
        }
        
        return repo.update(emp);
    }

    public boolean updateEmployeeWithPasswordChange(Employee emp, String originalEmail) {
        if (!emp.getEmail().equalsIgnoreCase(originalEmail) && repo.emailExists(emp.getEmail())) {
            System.out.println("  Email already in use by another employee.");
            return false;
        }
        

        if (!validateSalaryForPosition(emp.getPosition(), emp.getBaseSalary())) {
            return false;
        }
        
        return repo.updateWithPassword(emp);
    }


    private boolean validateSalaryForPosition(String position, java.math.BigDecimal salary) {
        if (position == null || position.trim().isEmpty()) {

            return true;
        }
        
        if (!SalaryCalculator.isValidSalaryForPosition(position, salary)) {
            String range = SalaryCalculator.getSalaryRange(position);
            if (range != null) {
                System.out.printf("  ✘ Salary $%,.2f is not valid for position '%s'.%n", salary, position);
                System.out.printf("  Allowed range: %s%n", range);
            } else {
                System.out.printf("  ℹ No salary rules defined for position '%s'.%n", position);
            }
            return false;
        }
        
        return true;
    }

    public boolean disableEmployee(int id) {
        return repo.disable(id);
    }

    public boolean enableEmployee(int id) {
        return repo.enable(id);
    }

    public EmployeeDTO getById(int id) {
        Employee e = repo.findById(id);
        if (e == null || !e.isActive()) return null;
        return EntityMapper.toEmployeeDTO(e);
    }

    public Employee getModelById(int id) {
        Employee e = repo.findById(id);
        if (e == null || !e.isActive()) return null;
        return e;
    }

    public Employee getModelByIdIgnoreStatus(int id) {
        return repo.findById(id);
    }

    public List<EmployeeDTO> getAllPaged(int page, int size) {
        return getAllPagedSorted(page, size, "ID");
    }

    public List<EmployeeDTO> getAllPagedSorted(int page, int size, String sortKey) {
        return repo.findAllOrdered(page, size, sortKey).stream()
                .map(EntityMapper::toEmployeeDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeeDTO> getByActiveStatus(boolean isActive) {
        return repo.findByActiveStatus(isActive).stream()
                .map(EntityMapper::toEmployeeDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeeDTO> search(String keyword, int page, int size) {
        return repo.searchByName(keyword, page, size).stream()
                .map(EntityMapper::toEmployeeDTO)
                .collect(Collectors.toList());
    }


    public List<EmployeeDTO> searchByPositionOrDepartment(String keyword, int page, int size) {
        return repo.searchByPositionOrDepartment(keyword, page, size).stream()
                .map(EntityMapper::toEmployeeDTO)
                .collect(Collectors.toList());
    }


    public List<EmployeeDTO> getWithoutPayroll(java.time.LocalDate from, java.time.LocalDate to) {
        return repo.findActiveWithoutPayroll(from, to).stream()
                .map(EntityMapper::toEmployeeDTO)
                .collect(Collectors.toList());
    }

    public int countAll() { return repo.countAll(); }
}
