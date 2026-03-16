package project.service;

import project.dao.UserDao;
import project.dao.UserDaoImpl;
import project.model.Employee;
import project.model.User;

/**
 Stateful authentication service.
 Tracks the currently logged-in admin or employee for the session.
 Used by controllers that need to know who is currently logged in.
 */
public class AuthService {

    private final UserDao userDao = new UserDaoImpl();

    private User     currentAdmin    = null;
    private Employee currentEmployee = null;

    /** Authenticates admin credentials. Returns User on success, null on failure. */
    public User adminLogin(String username, String password) {
        currentAdmin = userDao.adminLogin(username, password);
        return currentAdmin;
    }

    /** Authenticates employee credentials. Returns Employee on success, null on failure. */
    public Employee employeeLogin(String email, String password) {
        currentEmployee = userDao.employeeLogin(email, password);
        return currentEmployee;
    }

    public User     getCurrentAdmin()        { return currentAdmin; }
    public Employee getCurrentEmployee()     { return currentEmployee; }
    public boolean  isAdminLoggedIn()        { return currentAdmin    != null; }
    public boolean  isEmployeeLoggedIn()     { return currentEmployee != null; }

    /** Clears session on logout. */
    public void logout() {
        currentAdmin    = null;
        currentEmployee = null;
    }
}
