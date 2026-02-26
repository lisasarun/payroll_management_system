package project.dao;

import project.model.Employee;
import project.model.User;

public interface UserDao {

    User adminLogin(String username, String password);

    Employee employeeLogin(String email, String password);

    User findAdminByUsername(String username);

    Employee findEmployeeByEmail(String email);

    void updateAdminLastLogin(int adminId);

    void updateEmployeeLastLogin(int employeeId);

    boolean adminUsernameExists(String username);

    boolean employeeEmailExists(String email);
}
