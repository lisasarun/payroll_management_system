package project.service;

import project.dto.EmployeeDTO;
import project.mapper.EntityMapper;
import project.model.Employee;
import project.repository.EmployeeRepository;

import java.util.List;
import java.util.stream.Collectors;

public class EmployeeService {

    private final EmployeeRepository repo = new EmployeeRepository();

    /** Add new employee — checks for duplicate email across all Employees including disabled */
    public boolean addEmployee(Employee emp) {
        if (repo.emailExists(emp.getEmail())) {
            System.out.println("  Email already exists.");
            return false;
        }
        return repo.save(emp);
    }

    /** Update Employee without changing Pass word */
    public boolean updateEmployee(Employee emp) {
        return repo.update(emp);
    }

    /** Update employee — only checks email duplicate if email actually changed */
    public boolean updateEmployeeWithEmailCheck(Employee emp, String originalEmail) {
        if (!emp.getEmail().equalsIgnoreCase(originalEmail) && repo.emailExists(emp.getEmail())) {
            System.out.println("  Email already in use by another employee.");
            return false;
        }
        return repo.update(emp);
    }

    /** Update employee including a new hashed password */
    public boolean updateEmployeeWithPasswordChange(Employee emp, String originalEmail) {
        if (!emp.getEmail().equalsIgnoreCase(originalEmail) && repo.emailExists(emp.getEmail())) {
            System.out.println("  Email already in use by another employee.");
            return false;
        }
        return repo.updateWithPassword(emp);
    }

    public boolean disableEmployee(int id) {
        return repo.disable(id);
    }

    public EmployeeDTO getById(int id) {
        return EntityMapper.toEmployeeDTO(repo.findById(id));
    }

    public Employee getModelById(int id) {
        return repo.findById(id);
    }

    public List<EmployeeDTO> getAllPaged(int page, int size) {
        return repo.findAll(page, size).stream()
                .map(EntityMapper::toEmployeeDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeeDTO> search(String keyword, int page, int size) {
        return repo.searchByName(keyword, page, size).stream()
                .map(EntityMapper::toEmployeeDTO)
                .collect(Collectors.toList());
    }

    public int countAll() { return repo.countAll(); }
}
