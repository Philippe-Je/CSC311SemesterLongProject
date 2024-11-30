package dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Person;
import service.MyLogger;
import at.favre.lib.crypto.bcrypt.BCrypt;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * This class handles all database operations for the Employee Data Management System.
 * It provides methods for connecting to the database, retrieving data, and performing CRUD operations.
 */
public class DbConnectivityClass {

    /**
     * The name of the database
     */
    final static String DB_NAME = "CSC311_BD_TEMP";
    /**
     * Logger for recording operations
     */
    MyLogger lg = new MyLogger();
    /**
     * The URL of the SQL server
     */
    final static String SQL_SERVER_URL = "jdbc:mysql://csc311jeanserver.mysql.database.azure.com/";
    /**
     * The URL of the database
     */
    final static String DB_URL = "jdbc:mysql://csc311jeanserver.mysql.database.azure.com/" + DB_NAME;
    /**
     * The username for database access
     */
    final static String USERNAME = "philippejean0429";
    /**
     * The password for database access
     */
    final static String PASSWORD = "Cscserver0429";// update this password

    /**
     * Observable list to store Person objects
     */
    private final ObservableList<Person> data = FXCollections.observableArrayList();

    /**
     * Connects to the database and creates necessary tables if they don't exist.
     *
     * @return true if there are registered users in the database, false otherwise
     */
    public boolean connectToDatabase() {
        boolean hasRegistredUsers = false;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            //First, connect to MYSQL server and create the database if not created
            Connection conn = DriverManager.getConnection(SQL_SERVER_URL, USERNAME, PASSWORD);
            Statement statement = conn.createStatement();
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME + "");
            statement.close();
            conn.close();

            //Second, connect to the database and create the table "users" if cot created
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            statement = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT(10) NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                    "first_name VARCHAR(200) NOT NULL," +
                    "last_name VARCHAR(200) NOT NULL," +
                    "username VARCHAR(200) NOT NULL UNIQUE," +
                    "password VARCHAR(200) NOT NULL," +
                    "department VARCHAR(200)," +
                    "performance_rating DOUBLE," +
                    "email VARCHAR(200) NOT NULL UNIQUE," +
                    "profile_picture LONGBLOB)";
            statement.executeUpdate(sql);

            //check if we have users in the table users
            statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM users");

            if (resultSet.next()) {
                int numUsers = resultSet.getInt(1);
                if (numUsers > 0) {
                    hasRegistredUsers = true;
                }
            }

            statement.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return hasRegistredUsers;
    }

    /**
     * Retrieves all user data from the database.
     *
     * @return An ObservableList of Person objects representing all users
     */
    public ObservableList<Person> getData() {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM users";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.isBeforeFirst()) {
                lg.makeLog("No data");
            }
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String department = resultSet.getString("department");
                Double performanceRating = resultSet.getDouble("performance_rating");
                String email = resultSet.getString("email");
                byte[] profilePicture = resultSet.getBytes("profile_picture");

                Person person = new Person(id, first_name, last_name, department, performanceRating, email);
                person.setProfilePicture(profilePicture);
                data.add(person);
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * Registers a new user in the database.
     *
     * @param firstName      The first name of the user
     * @param lastName       The last name of the user
     * @param username       The username of the user
     * @param email          The email of the user
     * @param password       The password of the user
     * @param profilePicture The profile picture of the user as a byte array
     * @return true if registration is successful, false otherwise
     */
    public boolean registerUser(String firstName, String lastName, String username, String email, String password, byte[] profilePicture) {
        connectToDatabase();
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            conn.setAutoCommit(false);

            if (emailExists(email) || usernameExists(username)) {
                return false;
            }

            String sql = "INSERT INTO users (first_name, last_name, username, email, password, profile_picture) VALUES (?, ?, ?, ?, ?, ?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, username);
            preparedStatement.setString(4, email);

            String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
            preparedStatement.setString(5, hashedPassword);

            if (profilePicture != null) {
                preparedStatement.setBytes(6, profilePicture);
            } else {
                preparedStatement.setNull(6, java.sql.Types.BLOB);
            }

            int affectedRows = preparedStatement.executeUpdate();
            conn.commit();
            return affectedRows > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Checks if a username already exists in the database.
     *
     * @param username The username to check
     * @return true if the username exists, false otherwise
     */
    public boolean usernameExists(String username) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);
            preparedStatement.close();
            conn.close();
            return count > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the count of employees for each department.
     *
     * @return A Map where the key is the department name and the value is the number of employees in that department
     */
    public Map<String, Integer> getEmployeeCountByDepartment() {
        Map<String, Integer> departmentCounts = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT department, COUNT(*) as count FROM users GROUP BY department")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String department = rs.getString("department");
                int count = rs.getInt("count");
                departmentCounts.put(department, count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return departmentCounts;
    }

    /**
     * Queries users by their last name and logs the results.
     *
     * @param name The last name to search for
     */
    public void queryUserByLastName(String name) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM users WHERE last_name = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, name);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String department = resultSet.getString("department");
                double performanceRating = resultSet.getDouble("performance_rating");
                byte[] profilePicture = resultSet.getBytes("profile_picture");

                lg.makeLog("ID: " + id + ", Name: " + first_name + " " + last_name +
                        ", Department: " + department + ", Performance Rating: " + performanceRating +
                        ", Has Profile Picture: " + (profilePicture != null && profilePicture.length > 0));
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if an email already exists in the database.
     *
     * @param email The email to check
     * @return true if the email exists, false otherwise
     */
    public boolean emailExists(String email) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);
            preparedStatement.close();
            conn.close();
            return count > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lists all users in the database and logs their details.
     */
    public void listAllUsers() {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM users";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String department = resultSet.getString("department");
                double performanceRating = resultSet.getDouble("performance_rating");
                String email = resultSet.getString("email");
                byte[] profilePicture = resultSet.getBytes("profile_picture");

                lg.makeLog("ID: " + id + ", Name: " + first_name + " " + last_name +
                        ", Department: " + department + ", Performance Rating: " + performanceRating +
                        ", Email: " + email + ", Has Profile Picture: " + (profilePicture != null && profilePicture.length > 0));
            }

            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifies a user's credentials.
     *
     * @param username The username to verify
     * @param password The password to verify
     * @return true if the credentials are valid, false otherwise
     */
    public boolean verifyUser(String username, String password) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT password FROM users WHERE username = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, username);

            System.out.println("Attempting login with username: " + username);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String storedHash = resultSet.getString("password");
                BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), storedHash);
                System.out.println("Password verification result: " + result.verified);
                return result.verified;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Login verification error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inserts a new user into the database.
     *
     * @param person The Person object containing user details
     * @return true if the insertion was successful, false otherwise
     */
    public boolean insertUser(Person person) {
        connectToDatabase();
        try {
            if (emailExists(person.getEmail())) {
                return false;
            }
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "INSERT INTO users (first_name, last_name, department, performance_rating, email, profile_picture) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, person.getFirstName());
            preparedStatement.setString(2, person.getLastName());
            preparedStatement.setString(3, person.getDepartment());
            preparedStatement.setDouble(4, person.getPerformanceRating());
            preparedStatement.setString(5, person.getEmail());
            preparedStatement.setBytes(6, person.getProfilePicture());
            int row = preparedStatement.executeUpdate();
            preparedStatement.close();
            conn.close();
            return row > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Edits an existing user's information in the database.
     *
     * @param id The ID of the user to edit
     * @param p  The Person object containing updated user details
     */
    public void editUser(int id, Person p) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "UPDATE users SET first_name=?, last_name=?, department=?, performance_rating=?, email=?, profile_picture=? WHERE id=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, p.getFirstName());
            preparedStatement.setString(2, p.getLastName());
            preparedStatement.setString(3, p.getDepartment());
            preparedStatement.setDouble(4, p.getPerformanceRating());
            preparedStatement.setString(5, p.getEmail());
            preparedStatement.setBytes(6, p.getProfilePicture());
            preparedStatement.setInt(7, id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes a user record from the database.
     *
     * @param person The Person object representing the user to delete
     */
    public void deleteRecord(Person person) {
        connectToDatabase();
        int id = person.getId();
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "DELETE FROM users WHERE id=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Uploads a profile picture for a user.
     *
     * @param userName    The first name of the user
     * @param pictureData The profile picture data as a byte array
     */
    public void uploadProfilePicture(String userName, byte[] pictureData) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement preparedStatement = conn.prepareStatement(
                     "UPDATE users SET profile_picture = ? WHERE first_name = ?")) {
            preparedStatement.setBytes(1, pictureData);
            preparedStatement.setString(2, userName);
            int updatedRows = preparedStatement.executeUpdate();
            if (updatedRows == 0) {
                System.out.println("No user found with the name: " + userName);
            } else {
                System.out.println("Profile picture updated successfully for user: " + userName);
            }
        } catch (SQLException e) {
            System.err.println("Error uploading profile picture: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Deletes the profile picture of a user.
     *
     * @param userName The first name of the user
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deleteProfilePicture(String userName) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement preparedStatement = conn.prepareStatement(
                     "UPDATE users SET profile_picture = NULL WHERE first_name = ?")) {
            preparedStatement.setString(1, userName);
            int updatedRows = preparedStatement.executeUpdate();
            return updatedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the profile picture of a user.
     *
     * @param userName The first name of the user
     * @return The profile picture as a byte array, or null if not found
     */
    public byte[] getProfilePicture(String userName) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement preparedStatement = conn.prepareStatement(
                     "SELECT profile_picture FROM users WHERE first_name = ?")) {
            preparedStatement.setString(1, userName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBytes("profile_picture");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving profile picture: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves the auto-incremented ID for a newly inserted user.
     *
     * @param p The Person object representing the user
     * @return The auto-incremented ID
     */
    public int retrieveId(Person p) {
        connectToDatabase();
        int id;
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT id FROM users WHERE email=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, p.getEmail());

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            id = resultSet.getInt("id");
            preparedStatement.close();
            conn.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        lg.makeLog(String.valueOf(id));
        return id;
    }
}