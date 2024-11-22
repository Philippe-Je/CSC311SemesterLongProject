package service;

import java.util.prefs.Preferences;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserSession {
    private static volatile UserSession instance;
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final String userName;
    private final String password;
    private final String privileges;

    private UserSession(String userName, String password, String privileges) {
        this.userName = userName;
        this.password = password;
        this.privileges = privileges;
        saveToPreferences();
    }


    public static UserSession getInstance(String userName, String password, String privileges) {
        UserSession result = instance;
        if (result == null) {
            lock.writeLock().lock();
            try {
                result = instance;
                if (result == null) {
                    instance = result = new UserSession(userName, password, privileges);
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        return result;
    }

    public static UserSession getInstance(String userName, String password) {
        return getInstance(userName, password, "NONE");
    }

    private void saveToPreferences() {
        Preferences userPreferences = Preferences.userRoot().node(this.getClass().getName());
        userPreferences.put("USERNAME", userName);
        userPreferences.put("PASSWORD", password);
        userPreferences.put("PRIVILEGES", privileges);
    }

    public static void cleanUserSession() {
        lock.writeLock().lock();
        try {
            if (instance != null) {
                Preferences userPreferences = Preferences.userRoot().node(instance.getClass().getName());
                userPreferences.remove("USERNAME");
                userPreferences.remove("PASSWORD");
                userPreferences.remove("PRIVILEGES");
                instance = null;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String getUserName() {
        return this.userName;
    }

    public String getPrivileges() {
        return this.privileges;
    }
    @Override
    public String toString() {
        return "UserSession{userName='" + this.userName + "', privileges=" + this.privileges + '}';
    }
}