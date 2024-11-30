package viewmodel;

import dao.DbConnectivityClass;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Main application class for the Employee Database Manager.
 * Handles the application startup, scene transitions, and initial setup.
 */
public class MainApplication extends Application {

    private static Scene scene;
    private static DbConnectivityClass cnUtil;
    private Stage primaryStage;

    /**
     * Entry point of the application.
     * Initializes the database connection and launches the JavaFX application.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        cnUtil = new DbConnectivityClass();
        launch(args);

    }

    /**
     * Starts the JavaFX application.
     * Sets up the primary stage and shows the initial scene.
     *
     * @param primaryStage The primary stage for this application.
     */
    public void start(Stage primaryStage) {
        Image icon = new Image(getClass().getResourceAsStream("/images/DollarClouddatabase.png"));
        this.primaryStage = primaryStage;
        this.primaryStage.setResizable(false);
        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("Employee Database Manager");
        showScene1();
    }

    /**
     * Displays the initial splash screen.
     * Loads the splash screen FXML and applies the dark theme.
     */
    private void showScene1() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/splashscreen.fxml"));
            Scene scene = new Scene(root, 500, 600);
            scene.getStylesheets().clear(); // Clear existing stylesheets
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
            changeScene();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Transitions from the splash screen to the login screen.
     * Applies a fade-out effect to the current scene before switching.
     */
    public void changeScene() {
        try {
            Parent newRoot = FXMLLoader.load(getClass().getResource("/view/login.fxml").toURI().toURL());
            Scene currentScene = primaryStage.getScene();
            Parent currentRoot = currentScene.getRoot();
            currentScene.getStylesheets().clear(); // Clear existing stylesheets
            currentScene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), currentRoot);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                Scene newScene = new Scene(newRoot, 500, 600);
                newScene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
                primaryStage.setScene(newScene);
                primaryStage.show();
            });
            fadeOut.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}