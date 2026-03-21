package project.service;

import project.dao.UserDao;
import project.dao.UserDaoImpl;
import project.model.Employee;
import project.model.User;


public class AuthService {

    private final UserDao userDao = new UserDaoImpl();

    private User     currentAdmin    = null;
    private Employee currentEmployee = null;


    public User adminLogin(String username, String password) {
        currentAdmin = userDao.adminLogin(username, password);
        return currentAdmin;
    }


    public Employee employeeLogin(String email, String password) {
        currentEmployee = userDao.employeeLogin(email, password);
        return currentEmployee;
    }

    public User     getCurrentAdmin()        { return currentAdmin; }
    public Employee getCurrentEmployee()     { return currentEmployee; }
    public boolean  isAdminLoggedIn()        { return currentAdmin    != null; }
    public boolean  isEmployeeLoggedIn()     { return currentEmployee != null; }


    public void logout() {
        currentAdmin    = null;
        currentEmployee = null;
    }
}
