package dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Person;
import service.MyLogger;
import at.favre.lib.crypto.bcrypt.BCrypt;

import java.sql.*;

public class DbConnectivityClass {

    final static String DB_NAME = "CSC311_BD_TEMP";
    MyLogger lg = new MyLogger();
    final static String SQL_SERVER_URL = "jdbc:mysql://csc311jeanserver.mysql.database.azure.com/";//update this server name
    final static String DB_URL = "jdbc:mysql://csc311jeanserver.mysql.database.azure.com/" + DB_NAME;//update this database name
    final static String USERNAME = "philippejean0429";// update this username
    final static String PASSWORD = "Cscserver0429";// update this password


    private final ObservableList<Person> data = FXCollections.observableArrayList();



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
                    "major VARCHAR(200)," +
                    "email VARCHAR(200) NOT NULL UNIQUE," +
                    "imageURL VARCHAR(200))";
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

    public ObservableList<Person> getData() {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM users ";
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
                String major = resultSet.getString("major");
                String email = resultSet.getString("email");
                String imageURL = resultSet.getString("imageURL");
                data.add(new Person(id, first_name, last_name, department, major, email, imageURL));
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public boolean registerUser(String firstName, String lastName, String username, String email, String password) {
        connectToDatabase();
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            conn.setAutoCommit(false);

            if (emailExists(email) || usernameExists(username)) {
                return false;
            }

            String sql = "INSERT INTO users (first_name, last_name, username, email, password) VALUES (?, ?, ?, ?, ?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, username);
            preparedStatement.setString(4, email);

            // Use the correct BCrypt method to hash the password
            String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
            preparedStatement.setString(5, hashedPassword);

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
                String major = resultSet.getString("major");
                String department = resultSet.getString("department");

                lg.makeLog("ID: " + id + ", Name: " + first_name + " " + last_name + " "
                        + ", Major: " + major + ", Department: " + department);
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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

    public void listAllUsers() {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM users ";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String department = resultSet.getString("department");
                String major = resultSet.getString("major");
                String email = resultSet.getString("email");

                lg.makeLog("ID: " + id + ", Name: " + first_name + " " + last_name + " "
                        + ", Department: " + department + ", Major: " + major + ", Email: " + email);
            }

            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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

    public boolean insertUser(Person person) {
        connectToDatabase();
        try {
            if (emailExists(person.getEmail())) {
                return false;
            }
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "INSERT INTO users (first_name, last_name, department, major, email, imageURL) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, person.getFirstName());
            preparedStatement.setString(2, person.getLastName());
            preparedStatement.setString(3, person.getDepartment());
            preparedStatement.setString(4, person.getMajor());
            preparedStatement.setString(5, person.getEmail());
            preparedStatement.setString(6, person.getImageURL());
            int row = preparedStatement.executeUpdate();
            preparedStatement.close();
            conn.close();
            return row > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void editUser(int id, Person p) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "UPDATE users SET first_name=?, last_name=?, department=?, major=?, email=?, imageURL=? WHERE id=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, p.getFirstName());
            preparedStatement.setString(2, p.getLastName());
            preparedStatement.setString(3, p.getDepartment());
            preparedStatement.setString(4, p.getMajor());
            preparedStatement.setString(5, p.getEmail());
            preparedStatement.setString(6, p.getImageURL());
            preparedStatement.setInt(7, id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

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

    //Method to retrieve id from database where it is auto-incremented.
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