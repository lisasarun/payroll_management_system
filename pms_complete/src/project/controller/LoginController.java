package project.controller;

import project.dao.UserDao;
import project.dao.UserDaoImpl;
import project.model.Employee;
import project.model.User;


public class LoginController {

    private static final int MAX_ATTEMPTS = 3;
    private final UserDao userDao = new UserDaoImpl();


    public User adminLogin(String username, String password) {
        return userDao.adminLogin(username, password);
    }

    public Employee employeeLogin(String email, String password) {
        return userDao.employeeLogin(email, password);
    }

    public int getMaxAttempts() { return MAX_ATTEMPTS; }
}
