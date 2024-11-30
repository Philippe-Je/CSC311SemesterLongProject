package viewmodel;

import dao.DbConnectivityClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import service.UserSession;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;

/**
 * Controller class for the sign-up view.
 * Handles user registration and input validation.
 */
public class SignUpController {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;
    @FXML
    private ImageView profileImageView;
    @FXML
    private Button selectImageButton;

    // Regular expression patterns for input validation
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z\\s]{1,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@(gmail|yahoo|hotmail|outlook|aol|icloud|protonmail|zoho|yandex|mail)\\.(com|edu|gov|org|net|io|co)$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{4,20}$");

    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private byte[] profileImageData;

    /**
     * Initializes the controller.
     * Sets up the event handler for the select image button.
     */
    @FXML
    public void initialize() {
        selectImageButton.setOnAction(event -> selectProfileImage());
    }

    /**
     * Handles the sign-up process when the sign-up button is clicked.
     * Validates input fields and registers the user if all validations pass.
     *
     * @param event The action event triggered by clicking the sign-up button.
     */
    @FXML
    public void handleSignUp(ActionEvent event) {
        if (validateFields()) {
            try {
                if (cnUtil.usernameExists(usernameField.getText())) {
                    updateStatusMessage("Username already exists. Please choose a different username.");
                    return;
                }

                if (cnUtil.emailExists(emailField.getText())) {
                    updateStatusMessage("Email already exists. Please use a different email address.");
                    return;
                }

                boolean success = cnUtil.registerUser(
                        firstNameField.getText(),
                        lastNameField.getText(),
                        usernameField.getText(),
                        emailField.getText(),
                        passwordField.getText(),
                        profileImageData
                );

                if (success) {
                    System.out.println("New user registered successfully:");
                    System.out.println("Username: " + usernameField.getText());
                    System.out.println("Email: " + emailField.getText());
                    System.out.println("First Name: " + firstNameField.getText());
                    System.out.println("Last Name: " + lastNameField.getText());

                    UserSession userSession = UserSession.getInstance(usernameField.getText(), "USER");
                    userSession.saveCredentials(usernameField.getText(), passwordField.getText());
                    updateStatusMessage("Account created successfully!");
                    goBack(event);
                } else {
                    updateStatusMessage("Failed to create account. Please try again.");
                }
            } catch (Exception e) {
                updateStatusMessage("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Opens a file chooser dialog for selecting a profile image.
     * Updates the profile image view with the selected image.
     */
    private void selectProfileImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                profileImageData = Files.readAllBytes(selectedFile.toPath());
                Image image = new Image(selectedFile.toURI().toString());
                profileImageView.setImage(image);
            } catch (IOException e) {
                updateStatusMessage("Error reading image file: " + e.getMessage());
            }
        }
    }

    /**
     * Validates all input fields.
     *
     * @return true if all fields are valid, false otherwise.
     */
    private boolean validateFields() {
        if (isEmptyField(firstNameField) || isEmptyField(lastNameField) ||
                isEmptyField(usernameField) || isEmptyField(emailField) ||
                isEmptyField(passwordField)) {
            updateStatusMessage("All fields are required");
            return false;
        }

        if (!NAME_PATTERN.matcher(firstNameField.getText()).matches()) {
            updateStatusMessage("First name must contain only letters and spaces");
            return false;
        }

        if (!NAME_PATTERN.matcher(lastNameField.getText()).matches()) {
            updateStatusMessage("Last name must contain only letters and spaces");
            return false;
        }

        if (!USERNAME_PATTERN.matcher(usernameField.getText()).matches()) {
            updateStatusMessage("Username must be 4-20 characters and contain only letters, numbers, and underscores");
            return false;
        }

        if (!EMAIL_PATTERN.matcher(emailField.getText()).matches()) {
            updateStatusMessage("Please enter a valid email address from a supported provider");
            return false;
        }

        if (passwordField.getText().length() < 8) {
            updateStatusMessage("Password must be at least 8 characters long");
            return false;
        }

        return true;
    }

    /**
     * Checks if a text field is empty.
     *
     * @param field The text field to check.
     * @return true if the field is empty, false otherwise.
     */
    private boolean isEmptyField(TextField field) {
        return field.getText().trim().isEmpty();
    }

    /**
     * Updates the status message with a fade-out effect.
     *
     * @param message The message to display.
     */
    private void updateStatusMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setOpacity(1);
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), statusLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            statusLabel.setText("");
            statusLabel.setOpacity(0);
        });
        fadeOut.play();
    }

    /**
     * Navigates back to the login screen.
     *
     * @param event The action event triggered by clicking the back button.
     */
    @FXML
    public void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 500, 600);
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setResizable(false);
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}