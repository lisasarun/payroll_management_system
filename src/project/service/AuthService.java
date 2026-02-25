package project.service;

import project.dao.UserDao;
import project.dao.UserDaoImpl;
import project.dto.LoginRequest;
import project.dto.UserDTO;
import project.mapper.EntityMapper;
import project.model.User;
import project.util.InputUtil;

public class AuthService {
    private final UserDao userDao;
    private User currentUser;

    public AuthService() {
        this.userDao = new UserDaoImpl();
    }

    public boolean login(LoginRequest loginRequest) {
        try {
            User user = userDao.findByUsername(loginRequest.getUsername());

            if (user != null && user.getPassword().equals(loginRequest.getPassword())) {
                this.currentUser = user;
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            return false;
        }
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return isAuthenticated() && "ADMIN".equals(currentUser.getRole());
    }

    public UserDTO getCurrentUser() {
        return EntityMapper.toUserDTO(currentUser);
    }
}
