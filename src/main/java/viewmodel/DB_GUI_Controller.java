package viewmodel;

import com.azure.storage.blob.BlobClient;
import dao.DbConnectivityClass;
import dao.StorageUploader;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Person;
import service.MyLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class DB_GUI_Controller implements Initializable {

    StorageUploader store = new StorageUploader();
    @FXML
    StorageUploader storageUploader;
    @FXML
    ProgressBar progressBar;
    @FXML
    TextField first_name, last_name, department, email, imageURL;
    @FXML
    ComboBox<Major> majorComboBox;
    @FXML
    private Label statusLabel;
    @FXML
    ImageView img_view;
    @FXML
    MenuBar menuBar;
    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private Button addBtn, deleteBtn, editBtn;
    @FXML
    private MenuItem editItem, deleteItem;
    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;
    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z\\s]{1,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@(gmail|yahoo|hotmail|outlook|aol|icloud|protonmail|zoho|yandex|mail)\\.(com|edu|gov|org|net|io|co)$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern URL_PATTERN = Pattern.compile("^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?$");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);

            majorComboBox.setItems(FXCollections.observableArrayList(Major.values()));

            editBtn.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            deleteBtn.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            editItem.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            deleteItem.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());

            addValidationListener(first_name, NAME_PATTERN);
            addValidationListener(last_name, NAME_PATTERN);
            addValidationListener(department, NAME_PATTERN);
            addValidationListener(email, EMAIL_PATTERN);
            addValidationListener(imageURL, URL_PATTERN);

            addBtn.disableProperty().bind(
                    Bindings.isEmpty(first_name.textProperty())
                            .or(Bindings.isEmpty(last_name.textProperty()))
                            .or(Bindings.isEmpty(department.textProperty()))
                            .or(majorComboBox.valueProperty().isNull())
                            .or(Bindings.isEmpty(email.textProperty()))
                            .or(Bindings.isEmpty(imageURL.textProperty()))
                            .or(first_name.styleProperty().isEqualTo("-fx-border-color: red;"))
                            .or(last_name.styleProperty().isEqualTo("-fx-border-color: red;"))
                            .or(department.styleProperty().isEqualTo("-fx-border-color: red;"))
                            .or(email.styleProperty().isEqualTo("-fx-border-color: red;"))
                            .or(imageURL.styleProperty().isEqualTo("-fx-border-color: red;"))
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addValidationListener(TextField textField, Pattern pattern) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (pattern.matcher(newValue).matches()) {
                textField.setStyle("-fx-border-color: green;");
            } else {
                textField.setStyle("-fx-border-color: red;");
            }
        });
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
    protected void addNewRecord() {
        Person p = new Person(first_name.getText(), last_name.getText(), department.getText(),
                majorComboBox.getValue().toString(), email.getText(), imageURL.getText());
        cnUtil.insertUser(p);
        cnUtil.retrieveId(p);
        p.setId(cnUtil.retrieveId(p));
        data.add(p);
        clearForm();
        updateStatusMessage("New record added successfully.");
    }

    @FXML
    protected void clearForm() {
        first_name.clear();
        last_name.clear();
        department.clear();
        majorComboBox.getSelectionModel().clearSelection();
        email.clear();
        imageURL.clear();
        tv.getSelectionModel().clearSelection();
    }

    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").getFile());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void editRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        Person p2 = new Person(index + 1, first_name.getText(), last_name.getText(), department.getText(),
                majorComboBox.getValue().toString(), email.getText(), imageURL.getText());
        cnUtil.editUser(p.getId(), p2);
        data.remove(p);
        data.add(index, p2);
        tv.getSelectionModel().select(index);
        updateStatusMessage("Record updated successfully.");
    }

    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        cnUtil.deleteRecord(p);
        data.remove(index);
        tv.getSelectionModel().select(index);
        updateStatusMessage("Record deleted successfully.");
    }

    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    protected void addRecord() {
        showSomeone();
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            first_name.setText(p.getFirstName());
            last_name.setText(p.getLastName());
            department.setText(p.getDepartment());
            majorComboBox.setValue(Major.valueOf(p.getMajor()));
            email.setText(p.getEmail());
            imageURL.setText(p.getImageURL());
        } else {
            clearForm();
        }
    }

    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            System.out.println("light " + scene.getStylesheets());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void darkTheme(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email ");
        ObservableList<Major> options =
                FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();
        dialogPane.setContent(new VBox(8, textField1, textField2, textField3, comboBox));
        Platform.runLater(textField1::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(),
                        textField2.getText(), comboBox.getValue());
            }
            return null;
        });
        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((Results results) -> {
            MyLogger.makeLog(
                    results.fname + " " + results.lname + " " + results.major);
        });
    }

    private enum Major {
        CS, CPIS, English
    }

    private static class Results {

        String fname;
        String lname;
        Major major;

        public Results(String name, String date, Major venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }

}