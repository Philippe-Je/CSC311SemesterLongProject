package viewmodel;

import dao.DbConnectivityClass;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import service.UserSession;

public class LoginController {
    @FXML private VBox loginContainer;
    @FXML private TextField usernameTextField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private final DbConnectivityClass cnUtil = new DbConnectivityClass();

    @FXML
    public void initialize() {
        if (loginContainer != null) {
            setBackground();
        }
    }

    private void setBackground() {
        BackgroundImage backgroundImage = new BackgroundImage(
                new Image("https://edencoding.com/wp-content/uploads/2021/03/layer_06_1920x1080.png", true),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );
        loginContainer.setBackground(new Background(backgroundImage));

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(2), loginContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    @FXML
    public void login(ActionEvent actionEvent) {
        String username = usernameTextField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            updateStatusMessage("Please enter both username and password");
            return;
        }

        try {
            if (verifyCredentials(username, password)) {
                UserSession userSession = UserSession.getInstance(username, password, "USER");
                loadMainInterface(actionEvent);
            } else {
                updateStatusMessage("Invalid username or password");
            }
        } catch (Exception e) {
            updateStatusMessage("Login error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean verifyCredentials(String username, String password) {
        return cnUtil.verifyUser(username, password);
    }

    private void loadMainInterface(ActionEvent actionEvent) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/db_interface_gui.fxml"));
        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
        Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    private void updateStatusMessage(String message) {
        statusLabel.setText(message);
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), statusLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> statusLabel.setText(""));
        fadeOut.play();
    }

    @FXML
    public void signUp(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/signUp.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            updateStatusMessage("Error loading signup page: " + e.getMessage());
            e.printStackTrace();
        }
    }
}