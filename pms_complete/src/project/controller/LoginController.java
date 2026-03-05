package project.controller;

import project.dao.UserDao;
import project.dao.UserDaoImpl;
import project.model.Employee;
import project.model.User;

/**
 * Handles authentication logic for Admin and Employee login.
 * Delegates to UserDao for credential verification.
 */
public class LoginController {

    private static final int MAX_ATTEMPTS = 3;
    private final UserDao userDao = new UserDaoImpl();

    /**
     * Attempts admin login. Returns the User on success, null after max failed attempts.
     */
    public User adminLogin(String username, String password) {
        return userDao.adminLogin(username, password);
    }

    /**
     * Attempts employee login. Returns the Employee on success, null on failure.
     */
    public Employee employeeLogin(String email, String password) {
        return userDao.employeeLogin(email, password);
    }

    public int getMaxAttempts() { return MAX_ATTEMPTS; }
}
