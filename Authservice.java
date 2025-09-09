AuthService.java-// src/main/java/org/example/service/AuthService.java
package org.example.service;

import org.example.dao.UserDAO;
import org.example.model.User;
import org.example.util.PasswordHasher;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class AuthService {
    private final UserDAO userDAO;
    private User currentUser;

    public AuthService(Connection connection) {
        this.userDAO = new UserDAO(connection);
    }

    public boolean register(String username, String password, String email) throws SQLException {
        if (username == null || username.trim().isEmpty() || 
            password == null || password.isEmpty() || 
            email == null || email.trim().isEmpty()) {
            return false;
        }

        if (userDAO.findByUsername(username).isPresent()) {
            return false; // Username already exists
        }

        String hashedPassword = PasswordHasher.hashPassword(password);
        User newUser = new User(username, hashedPassword, email);
        
        if (userDAO.createUser(newUser)) {
            this.currentUser = newUser;
            return true;
        }
        return false;
    }

    public boolean login(String username, String password) throws SQLException {
        Optional<User> userOpt = userDAO.findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (PasswordHasher.verifyPassword(password, user.getPasswordHash())) {
                user.setLastLogin(java.time.LocalDateTime.now());
                userDAO.updateUser(user);
                this.currentUser = user;
                return true;
            }
        }
        return false;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}