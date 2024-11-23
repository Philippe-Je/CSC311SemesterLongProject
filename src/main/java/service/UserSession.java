package service;

import java.util.prefs.Preferences;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserSession {
    private static volatile UserSession instance;
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();


    private final String username;
    private final String role;
    private final Preferences prefs;

    private UserSession(String username, String role) {
        this.username = username;
        this.role = role;
        this.prefs = Preferences.userRoot().node(this.getClass().getName());
    }

    public static UserSession getInstance(String username, String role) {
        UserSession result = instance;
        if (result == null) {
            lock.writeLock().lock();
            try {
                result = instance;
                if (result == null) {
                    instance = result = new UserSession(username, role);
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        return result;
    }

    public void saveCredentials(String username, String password) {
        prefs.put("username", username);
        prefs.put("password", password);
    }

    public String getUsername() {
        return prefs.get("username", null);
    }

    public String getPassword() {
        return prefs.get("password", null);
    }

    public void clearCredentials() {
        prefs.remove("username");
        prefs.remove("password");
    }

    public String getRole() {
        return role;
    }

    public static void cleanUserSession() {
        lock.writeLock().lock();
        try {
            if (instance != null) {
                instance.clearCredentials();
                instance = null;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public String toString() {
        return "UserSession{username='" + this.username + "', role=" + this.role + '}';
    }
}