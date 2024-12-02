package service;

import java.util.prefs.Preferences;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a user session in the application.
 * This class implements the Singleton pattern with double-checked locking for thread safety.
 */
public class UserSession {
    private static volatile UserSession instance;

    private final String username;
    private final String role;
    private final Preferences prefs;

    /**
     * Private constructor to prevent direct instantiation.
     *
     * @param username The username of the user
     * @param role     The role of the user
     */
    private UserSession(String username, String role) {
            this.username = username;
            this.role = role;
            this.prefs = Preferences.userRoot().node(this.getClass().getName());
    }

    /**
     * Gets the instance of UserSession, creating it if it doesn't exist.
     *
     * @param username The username of the user
     * @param role     The role of the user
     * @return The UserSession instance
     */
    public static synchronized UserSession getInstance(String username, String role) {
        if (instance == null) {
            instance = new UserSession(username, role);
        }
        return instance;
    }

    /**
     * Saves the user's credentials.
     *
     * @param username The username to save
     * @param password The password to save
     */
    public void saveCredentials(String username, String password) {
        prefs.put("username", username);
        prefs.put("password", password);
    }

    /**
     * Retrieves the saved username.
     *
     * @return The saved username, or null if not found
     */
    public String getUsername() {
        return prefs.get("username", null);
    }

    /**
     * Retrieves the saved password.
     *
     * @return The saved password, or null if not found
     */
    public String getPassword() {
        return prefs.get("password", null);
    }

    /**
     * Clears the saved credentials.
     */
    public void clearCredentials() {
        prefs.remove("username");
        prefs.remove("password");
    }

    /**
     * Gets the role of the current user.
     *
     * @return The user's role
     */
    public String getRole() {
        return role;
    }

    /**
     * Cleans up the UserSession by clearing credentials and nullifying the instance.
     */
    public static synchronized void cleanUserSession() {
        if (instance != null) {
            instance.clearCredentials();
            instance = null;
        }
    }

    /**
     * Returns a string representation of the UserSession.
     *
     * @return A string containing the username and role
     */
    @Override
    public String toString() {
        return "UserSession{username='" + this.username + "', role=" + this.role + '}';
    }
}