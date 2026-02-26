package project.service;

import project.dto.EmployeeDTO;
import project.mapper.EntityMapper;
import project.model.Employee;
import project.repository.EmployeeRepository;

import java.util.List;
import java.util.stream.Collectors;

public class EmployeeService {

    private final EmployeeRepository repo = new EmployeeRepository();

    public boolean addEmployee(Employee emp) {
        if (repo.findByEmail(emp.getEmail()) != null) {
            System.out.println("  Email already exists.");
            return false;
        }
        return repo.save(emp);
    }

    public boolean updateEmployee(Employee emp) {
        return repo.update(emp);
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
