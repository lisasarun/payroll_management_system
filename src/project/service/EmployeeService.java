package project.service;



import project.dao.EmployeeDao;
import project.dao.EmlpoyeeDaoImpl;
import project.dto.EmployeeDTO;
import project.mapper.EntityMapper;
import project.model.Employee;
import project.model.Pagination;
import project.util.InputUtil;

import java.util.List;
import java.util.stream.Collectors;

public class EmployeeService {
    private final EmployeeDao employeeDao;

    public EmployeeService() {
        this.employeeDao = new EmlpoyeeDaoImpl();
    }

    public boolean addEmployee(EmployeeDTO employeeDTO) {
        try {
            Employee employee = EntityMapper.toEmployee(employeeDTO);
            return employeeDao.save(employee);
        } catch (Exception e) {
            System.err.println("Error adding employee: " + e.getMessage());
            return false;
        }
    }

    public boolean updateEmployee(EmployeeDTO employeeDTO) {
        try {
            Employee employee = EntityMapper.toEmployee(employeeDTO);
            return employeeDao.update(employee);
        } catch (Exception e) {
            System.err.println("Error updating employee: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteEmployee(int employeeId) {
        try {
            return employeeDao.delete(employeeId);
        } catch (Exception e) {
            System.err.println("Error deleting employee: " + e.getMessage());
            return false;
        }
    }

    public EmployeeDTO getEmployeeById(int employeeId) {
        try {
            Employee employee = employeeDao.findById(employeeId);
            return EntityMapper.toEmployeeDTO(employee);
        } catch (Exception e) {
            System.err.println("Error finding employee: " + e.getMessage());
            return null;
        }
    }

    public List<EmployeeDTO> getAllEmployees(Pagination pagination) {
        try {
            List<Employee> employees = employeeDao.findAll(pagination);
            return employees.stream()
                    .map(EntityMapper::toEmployeeDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching employees: " + e.getMessage());
            return null;
        }
    }

    public List<EmployeeDTO> searchEmployees(String keyword) {
        try {
            List<Employee> employees = employeeDao.search(keyword);
            return employees.stream()
                    .map(EntityMapper::toEmployeeDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error searching employees: " + e.getMessage());
            return null;
        }
    }

    public int getTotalEmployeeCount() {
        try {
            return employeeDao.getTotalCount();
        } catch (Exception e) {
            System.err.println("Error getting total count: " + e.getMessage());
            return 0;
        }
    }
}