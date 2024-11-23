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


import java.io.*;

import javafx.stage.FileChooser;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class DB_GUI_Controller implements Initializable {

    StorageUploader store = new StorageUploader();
    @FXML
    private MenuItem importCSVMenuItem;
    @FXML
    private MenuItem exportCSVMenuItem;
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

    @FXML
    private void importCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File to Import");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try (CSVReader reader = new CSVReader(new FileReader(file))) {
                String[] nextLine;
                reader.readNext(); // Skip header row
                while ((nextLine = reader.readNext()) != null) {
                    Person person = new Person(
                            nextLine[0], // firstName
                            nextLine[1], // lastName
                            nextLine[2], // department
                            nextLine[3], // major
                            nextLine[4], // email
                            nextLine[5]  // imageURL
                    );
                    cnUtil.insertUser(person);
                    data.add(person);
                }
                tv.refresh();
                showAlert("Success", "CSV Import", "CSV file imported successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to import CSV file", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void exportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
                String[] header = {"First Name", "Last Name", "Department", "Major", "Email", "Image URL"};
                writer.writeNext(header);

                for (Person person : data) {
                    String[] row = {
                            person.getFirstName(),
                            person.getLastName(),
                            person.getDepartment(),
                            person.getMajor(),
                            person.getEmail(),
                            person.getImageURL()
                    };
                    writer.writeNext(row);
                }
                showAlert("Success", "CSV Export", "CSV file exported successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to export CSV file", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String header, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
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
        try {
            Person p = new Person(first_name.getText(), last_name.getText(), department.getText(),
                    majorComboBox.getValue().toString(), email.getText(), imageURL.getText());

            if (cnUtil.emailExists(p.getEmail())) {
                updateStatusMessage("Error: A user with this email already exists.");
                return;
            }

            boolean insertSuccess = cnUtil.insertUser(p);
            if (insertSuccess) {
                int id = cnUtil.retrieveId(p);
                p.setId(id);
                data.add(p);
                clearForm();
                updateStatusMessage("New record added successfully.");
            } else {
                updateStatusMessage("Error: Failed to add new record.");
            }
        } catch (Exception e) {
            updateStatusMessage("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    protected void clearForm() {
        first_name.clear();
        last_name.clear();
        department.clear();
        majorComboBox.getSelectionModel().selectFirst(); // Select the first item instead of clearing
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
        if (p != null) {
            try {
                int index = data.indexOf(p);
                Person p2 = new Person(p.getId(), first_name.getText(), last_name.getText(), department.getText(),
                        majorComboBox.getValue().toString(), email.getText(), imageURL.getText());
                cnUtil.editUser(p.getId(), p2);
                data.set(index, p2);
                tv.getSelectionModel().select(index);
                updateStatusMessage("Record updated successfully.");
            } catch (Exception e) {
                updateStatusMessage("Error: Failed to update record - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            try {
                cnUtil.deleteRecord(p);
                data.remove(p);
                clearForm();
                updateStatusMessage("Record deleted successfully.");
            } catch (Exception e) {
                updateStatusMessage("Error: Failed to delete record - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
            Task<Void> uploadTask = createUploadTask(file, progressBar);
            progressBar.progressProperty().bind(uploadTask.progressProperty());
            new Thread(uploadTask).start();
        }
    }

    private Task<Void> createUploadTask(File file, ProgressBar progressBar) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                BlobClient blobClient = store.getContainerClient().getBlobClient(file.getName());
                long fileSize = Files.size(file.toPath());
                long uploadedBytes = 0;

                try (FileInputStream fileInputStream = new FileInputStream(file);
                     OutputStream blobOutputStream = blobClient.getBlockBlobClient().getBlobOutputStream()) {

                    byte[] buffer = new byte[1024 * 1024]; // 1 MB buffer size
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        blobOutputStream.write(buffer, 0, bytesRead);
                        uploadedBytes += bytesRead;

                        // Calculate and update progress as a percentage
                        int progress = (int) ((double) uploadedBytes / fileSize * 100);
                        updateProgress(progress, 100);
                    }
                }

                return null;
            }
        };
    }


    @FXML
    protected void addRecord() {
        showSomeone();
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        try {
            Person p = tv.getSelectionModel().getSelectedItem();
            if (p != null) {
                first_name.setText(p.getFirstName());
                last_name.setText(p.getLastName());
                department.setText(p.getDepartment());

                // Safe handling of major value
                String majorStr = p.getMajor();
                if (majorStr != null && !majorStr.isEmpty()) {
                    try {
                        Major major = Major.valueOf(majorStr.toUpperCase());
                        majorComboBox.setValue(major);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid major value: " + majorStr);
                        majorComboBox.setValue(null);
                    }
                } else {
                    majorComboBox.setValue(null);
                }

                email.setText(p.getEmail());
                imageURL.setText(p.getImageURL());
            } else {
                clearForm();
            }
        } catch (Exception e) {
            System.err.println("Error in selectedItemTV: " + e.getMessage());
            e.printStackTrace();
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

    public enum Major {
        CS, CPIS, ENGLISH;

        public static Major fromString(String text) {
            if (text != null && !text.isEmpty()) {
                try {
                    return valueOf(text.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
            return null;
        }
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