package service;

import model.User;
import storage.JsonStore;

import java.util.List;

public class AuthService {
    private final JsonStore store;

    public AuthService(JsonStore store) {
        this.store = store;
    }

    public void register(String username, String password, String name) {
        username = username == null ? "" : username.trim();
        password = password == null ? "" : password.trim();
        name = name == null ? "" : name.trim();

        if (username.isEmpty() || password.isEmpty()) {
            throw new RuntimeException("username/password is required");
        }

        List<User> users = store.loadUsers();
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                throw new RuntimeException("username already exists");
            }
        }

        users.add(new User(username, password, name));
        store.saveUsers(users);
    }

    public User login(String username, String password) {
        username = username == null ? "" : username.trim();
        password = password == null ? "" : password.trim();

        List<User> users = store.loadUsers();
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                if (u.getPassword().equals(password)) {
                    return u;
                }
                throw new RuntimeException("wrong password");
            }
        }
        throw new RuntimeException("user not found");
    }

    public void updateProfileName(String username, String newName) {
        newName = newName == null ? "" : newName.trim();
        if (newName.isEmpty()) throw new RuntimeException("name cannot be empty");

        List<User> users = store.loadUsers();
        boolean found = false;
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                u.setName(newName);
                found = true;
                break;
            }
        }
        if (!found) throw new RuntimeException("user not found");
        store.saveUsers(users);
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        username = username == null ? "" : username.trim();
        oldPassword = oldPassword == null ? "" : oldPassword.trim();
        newPassword = newPassword == null ? "" : newPassword.trim();

        if (username.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty()) {
            throw new RuntimeException("username/oldPassword/newPassword is required");
        }
        if (newPassword.length() < 4) {
            throw new RuntimeException("new password is too short");
        }
        if (newPassword.equals(oldPassword)) {
            throw new RuntimeException("new password must be different");
        }

        List<User> users = store.loadUsers();
        User target = null;
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                target = u;
                break;
            }
        }
        if (target == null) throw new RuntimeException("user not found");
        if (!target.getPassword().equals(oldPassword)) throw new RuntimeException("wrong password");

        target.setPassword(newPassword);
        store.saveUsers(users);
    }

    public boolean userExists(String username) {
        List<User> users = store.loadUsers();
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) return true;
        }
        return false;
    }
}
