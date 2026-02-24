package project.dao;

import project.modal.User;
import project.modal.Employee;

/**
 DAO interface for user authentication operations.
 Covers both Admin (admins table) and Employee (employees table) login flows.
 Implemented by UserDaoImpl using JDBC + PreparedStatement.
 Called by: AuthService / LoginController
 */
public interface UserDao {

    /**
      Authenticate an admin by username + password.
      Returns the User (admin) if credentials match, null otherwise.

      @param username  plain-text username from console
      @param password  plain-text password (compared against stored hash)
      @return          User model if authenticated, null if not found / wrong password
     */
    User adminLogin(String username, String password);

    /**
      Authenticate an employee by email + password.
      Returns the Employee if credentials match, null otherwise.

      @param email     employee email used as login identifier
      @param password  plain-text password
      @return          Employee model if authenticated, null if not found / inactive / wrong password
     */
    Employee employeeLogin(String email, String password);

    /**
      Find an admin record by username (without password check).
      Used for display or session resolution.

      @param username  admin username
      @return          User model or null
     */
    User findAdminByUsername(String username);

    /**
      Find an employee record by email.

      @param email  employee email
      @return       Employee model or null
     */
    Employee findEmployeeByEmail(String email);

    /**
      Update the last_login timestamp for an admin to NOW().

      @param adminId  PK of the admins table
     */
    void updateAdminLastLogin(int adminId);

    /**
      Update the last_login timestamp for an employee to NOW().

      @param employeeId  PK of the employees table
     */
    void updateEmployeeLastLogin(int employeeId);

    /**
      Check whether a given username already exists in the admins table.
      Useful for registration / duplication guard.

      @param username  username to check
      @return          true if exists
     */
    boolean adminUsernameExists(String username);

    /**
      Check whether a given email already exists in the employees table.

      @param email  email to check
      @return       true if exists
     */
    boolean employeeEmailExists(String email);
}