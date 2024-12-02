package viewmodel;

import com.azure.storage.blob.BlobClient;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Person;
import service.MyLogger;

import java.io.*;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.net.URL;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Controller class for the Employee Data Management System GUI.
 * This class handles user interactions, database operations, and UI updates.
 */
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
    private Button addImageBttn;
    @FXML
    private Button deleteImageBttn;
    @FXML
    TextField first_name, last_name, email, performanceRating;
    @FXML
    ComboBox<Department> departmentComboBox;
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
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_performanceRating, tv_email;
    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z\\s]{1,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@(gmail|yahoo|hotmail|outlook|aol|icloud|protonmail|zoho|yandex|mail)\\.(com|edu|gov|org|net|io|co)$", Pattern.CASE_INSENSITIVE

    );

    private static final Pattern PERFORMANCE_RATING_PATTERN = Pattern.compile("^(10(\\.0{1,2})?|[1-9](\\.\\d{1,2})?)$");

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     *
     * @param url            The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resourceBundle The resources used to localize the root object, or null if the root object was not localized.
     */
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {

            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_performanceRating.setCellValueFactory(new PropertyValueFactory<>("performanceRating"));

            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);

            departmentComboBox.setItems(FXCollections.observableArrayList(Department.values()));

            editBtn.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            deleteBtn.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            editItem.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            deleteItem.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            addImageBttn.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            deleteImageBttn.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());

            addValidationListener(first_name, NAME_PATTERN);
            addValidationListener(last_name, NAME_PATTERN);
            addValidationListener(email, EMAIL_PATTERN);
            addValidationListener(performanceRating, PERFORMANCE_RATING_PATTERN);

            img_view.setOnMouseClicked(event -> {
                if (tv.getSelectionModel().getSelectedItem() != null) {
                    addImage();
                }
            });


            addBtn.disableProperty().bind(Bindings.isEmpty(first_name.textProperty()).or(Bindings.isEmpty(last_name.textProperty())).or(departmentComboBox.valueProperty().isNull()).or(Bindings.isEmpty(email.textProperty())).or(Bindings.isEmpty(performanceRating.textProperty())).or(first_name.styleProperty().isEqualTo("-fx-border-color: red;")).or(last_name.styleProperty().isEqualTo("-fx-border-color: red;")).or(email.styleProperty().isEqualTo("-fx-border-color: red;")).or(performanceRating.styleProperty().isEqualTo("-fx-border-color: red;")));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the window title for the application.
     *
     * @param stage The primary stage for this application.
     */
    private void setWindowTitle(Stage stage) {
        stage.setTitle("Employee Data Management System");
    }

    /**
     * Handles the import of CSV files containing employee data.
     * This method opens a file chooser dialog, reads the selected CSV file,
     * and imports the data into the application's database.
     */
    @FXML
    private void importCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File to Import");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            Task<ImportResult> importTask = new Task<>() {
                @Override
                protected ImportResult call() throws Exception {
                    int importedCount = 0;
                    int skippedCount = 0;
                    try (CSVReader reader = new CSVReader(new FileReader(file))) {
                        String[] nextLine;
                        reader.readNext(); // Skip header row
                        while ((nextLine = reader.readNext()) != null) {
                            String email = nextLine[5]; // Assuming email is the 6th column
                            if (!cnUtil.emailExists(email)) {
                                Person person = new Person(nextLine[1], // firstName
                                        nextLine[2], // lastName
                                        nextLine[3], // department
                                        Double.parseDouble(nextLine[4]), // performanceRating
                                        email);
                                // Check if profile picture path is provided in the CSV
                                if (nextLine.length > 6 && !nextLine[6].isEmpty()) {
                                    File imageFile = new File(nextLine[6]);
                                    if (imageFile.exists()) {
                                        byte[] imageData = Files.readAllBytes(imageFile.toPath());
                                        person.setProfilePicture(imageData);
                                    }
                                }
                                cnUtil.insertUser(person);
                                Platform.runLater(() -> data.add(person));
                                importedCount++;
                            } else {
                                skippedCount++;
                            }
                            updateProgress(importedCount + skippedCount, reader.getLinesRead());
                        }
                    }
                    return new ImportResult(importedCount, skippedCount);
                }
            };

            importTask.setOnSucceeded(e -> {
                ImportResult result = importTask.getValue();
                tv.refresh();
                showAlert("Success", "CSV Import", "CSV file imported successfully.\nImported: " + result.importedCount + "\nSkipped (existing users): " + result.skippedCount, Alert.AlertType.INFORMATION);
            });

            importTask.setOnFailed(e -> {
                Throwable exception = importTask.getException();
                showAlert("Error", "Failed to import CSV file", exception.getMessage(), Alert.AlertType.ERROR);
            });

            new Thread(importTask).start();
        }
    }

    private static class ImportResult {
        final int importedCount;
        final int skippedCount;

        ImportResult(int importedCount, int skippedCount) {
            this.importedCount = importedCount;
            this.skippedCount = skippedCount;
        }
    }

    /**
     * Handles the export of employee data to a CSV file.
     * This method opens a file chooser dialog for saving the CSV file
     * and writes the current employee data to the selected file.
     */
    @FXML
    private void exportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
                String[] header = {"ID", "First Name", "Last Name", "Department", "Performance Rating", "Email", "Has Profile Picture"};
                writer.writeNext(header);

                for (Person person : data) {
                    byte[] profilePicture = cnUtil.getProfilePicture(person.getFirstName());
                    String[] row = {String.valueOf(person.getId()), person.getFirstName(), person.getLastName(), person.getDepartment(), String.valueOf(person.getPerformanceRating()), person.getEmail(), profilePicture != null && profilePicture.length > 0 ? "Yes" : "No"};
                    writer.writeNext(row);
                }
                showAlert("Success", "CSV Export", "CSV file exported successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to export CSV file", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Generates a PDF report showing the count of employees by department.
     * This method creates a PDF document with a table containing department names
     * and their corresponding employee counts.
     */
    @FXML
    private void generateDepartmentReport() {
        Map<String, Integer> departmentCounts = cnUtil.getEmployeeCountByDepartment();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Department Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                Paragraph title = new Paragraph("Employee Count by Department", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);

                document.add(Chunk.NEWLINE);

                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.addCell("Department");
                table.addCell("Employee Count");

                for (Map.Entry<String, Integer> entry : departmentCounts.entrySet()) {
                    table.addCell(entry.getKey());
                    table.addCell(entry.getValue().toString());
                }

                document.add(table);
                document.close();

                updateStatusMessage("PDF report generated successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                updateStatusMessage("Error generating PDF report: " + e.getMessage());
            }
        }
    }

    /**
     * Displays an alert dialog with the specified parameters.
     *
     * @param title     The title of the alert dialog.
     * @param header    The header text of the alert dialog.
     * @param content   The content text of the alert dialog.
     * @param alertType The type of the alert (e.g., INFORMATION, ERROR).
     */
    private void showAlert(String title, String header, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Adds a validation listener to a text field using a specified pattern.
     *
     * @param textField The text field to add the validation listener to.
     * @param pattern   The regular expression pattern to use for validation.
     */
    private void addValidationListener(TextField textField, Pattern pattern) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (pattern.matcher(newValue).matches()) {
                textField.setStyle("-fx-border-color: green;");
            } else {
                textField.setStyle("-fx-border-color: red;");
            }
        });
    }

    /**
     * Updates the status message displayed in the UI.
     * The message fades out after a short duration.
     *
     * @param message The status message to display.
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
     * Handles the addition of a profile picture for the selected employee.
     * Opens a file chooser dialog for image selection and uploads the chosen image.
     */
    @FXML
    protected void addImage() {
        Person selectedPerson = tv.getSelectionModel().getSelectedItem();
        if (selectedPerson == null) {
            updateStatusMessage("Please select a user first.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                if (fileContent.length > 5 * 1024 * 1024) {
                    updateStatusMessage("Error: Image size exceeds 5MB limit.");
                    return;
                }
                cnUtil.uploadProfilePicture(selectedPerson.getFirstName(), fileContent);
                updateProfilePicture(selectedPerson.getFirstName());
                updateStatusMessage("Image uploaded successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                updateStatusMessage("Error reading image file: " + e.getMessage());
            }
        } else {
            updateStatusMessage("No file selected.");
        }
    }

    /**
     * Deletes the profile picture of the currently selected employee.
     */
    @FXML
    protected void deleteIMAGE() {
        Person selectedPerson = tv.getSelectionModel().getSelectedItem();
        if (selectedPerson != null) {
            cnUtil.deleteProfilePicture(selectedPerson.getFirstName());
            updateProfilePicture(selectedPerson.getFirstName());
            updateStatusMessage("Image deleted successfully.");
        } else {
            updateStatusMessage("Please select a user first.");
        }
    }

    /**
     * Updates the profile picture displayed in the UI for a given user.
     *
     * @param userName The name of the user whose profile picture should be updated.
     */
    private void updateProfilePicture(String userName) {
        byte[] imageData = cnUtil.getProfilePicture(userName);
        if (imageData != null && imageData.length > 0) {
            Image image = new Image(new ByteArrayInputStream(imageData));
            img_view.setImage(image);
        } else {
            setDefaultProfilePicture();
        }
    }

    /**
     * Sets the default profile picture in the UI.
     */
    private void setDefaultProfilePicture() {
        Image defaultImage = new Image(getClass().getResourceAsStream("/images/profile.png"));
        img_view.setImage(defaultImage);
    }

    /**
     * Handles the addition of a new employee record to the database and UI.
     */
    @FXML
    protected void addNewRecord() {
        try {
            Department selectedDepartment = departmentComboBox.getValue();
            if (selectedDepartment == null) {
                updateStatusMessage("Error: Please select a department.");
                return;
            }

            Person p = new Person(first_name.getText(), last_name.getText(), selectedDepartment.name(), Double.parseDouble(performanceRating.getText()), email.getText());

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
                updateProfilePicture(p.getFirstName());
            } else {
                updateStatusMessage("Error: Failed to add new record.");
            }
        } catch (Exception e) {
            updateStatusMessage("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clears all input fields and resets the UI to its default state.
     */
    @FXML
    protected void clearForm() {
        first_name.clear();
        last_name.clear();
        departmentComboBox.getSelectionModel().selectFirst();
        email.clear();
        performanceRating.clear();
        tv.getSelectionModel().clearSelection();
        setDefaultProfilePicture();
    }

    /**
     * Handles the user logout process.
     * Switches the scene to the login view.
     *
     * @param actionEvent The event that triggered the logout action.
     */
    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 500, 600);
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").getFile());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the application.
     */
    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    /**
     * Displays the "About" dialog of the application.
     */
    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 400);

            // Determine the current theme
            boolean isDarkTheme = menuBar.getScene().getStylesheets().contains(getClass().getResource("/css/darkTheme.css").toExternalForm());

            // Apply the appropriate CSS
            if (isDarkTheme) {
                scene.getStylesheets().add(getClass().getResource("/css/aboutDark.css").toExternalForm());
            } else {
                scene.getStylesheets().add(getClass().getResource("/css/aboutLight.css").toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("About Employee Data Management System");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the editing of an existing employee record.
     */
    @FXML
    protected void editRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            try {
                Department selectedDepartment = departmentComboBox.getValue();
                if (selectedDepartment == null) {
                    updateStatusMessage("Error: Please select a department.");
                    return;
                }
                int index = data.indexOf(p);
                Person p2 = new Person(
                        p.getId(),
                        first_name.getText(),
                        last_name.getText(),
                        selectedDepartment.name(),
                        Double.parseDouble(performanceRating.getText()),
                        email.getText()
                );
                // Preserve the existing profile picture
                p2.setProfilePicture(p.getProfilePicture());

                cnUtil.editUser(p.getId(), p2);
                data.set(index, p2);
                tv.getSelectionModel().select(index);
                updateStatusMessage("Record updated successfully.");
                updateProfilePicture(p2.getFirstName());
            } catch (Exception e) {
                updateStatusMessage("Error: Failed to update record - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles the deletion of the selected employee record.
     */
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

    /**
     * Handles the keyboard shortcut for editing a record.
     */
    @FXML
    private void handleEditShortcut() {
        if (tv.getSelectionModel().getSelectedItem() != null) {
            editRecord();
        }
    }

    /**
     * Handles the keyboard shortcut for deleting a record.
     */
    @FXML
    private void handleDeleteShortcut() {
        if (tv.getSelectionModel().getSelectedItem() != null) {
            deleteRecord();
        }
    }

    /**
     * Handles the keyboard shortcut for copying employee information to clipboard.
     */
    @FXML
    private void handleCopyShortcut() {
        Person selectedPerson = tv.getSelectionModel().getSelectedItem();
        if (selectedPerson != null) {
            String personInfo = selectedPerson.getFirstName() + " " + selectedPerson.getLastName() + ", " + selectedPerson.getDepartment();
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(personInfo);
            clipboard.setContent(content);
            updateStatusMessage("Person info copied to clipboard");
        }
    }

    /**
     * Handles the keyboard shortcut for clearing the form.
     */
    @FXML
    private void handleClearShortcut() {
        clearForm();
    }


    /**
     * Creates a task for uploading a file to Azure Blob Storage.
     *
     * @param file The file to be uploaded.
     * @return A Task object representing the upload operation.
     */
    private Task<Void> createUploadTask(File file) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                BlobClient blobClient = store.getContainerClient().getBlobClient(file.getName());
                long fileSize = Files.size(file.toPath());
                long uploadedBytes = 0;

                try (FileInputStream fileInputStream = new FileInputStream(file); OutputStream blobOutputStream = blobClient.getBlockBlobClient().getBlobOutputStream()) {

                    byte[] buffer = new byte[1024 * 1024]; // 1 MB buffer size
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        blobOutputStream.write(buffer, 0, bytesRead);
                        uploadedBytes += bytesRead;

                        // Update progress
                        double progress = (double) uploadedBytes / fileSize;
                        updateProgress(progress, 1.0);
                    }
                }

                return null;
            }
        };
    }

    /**
     * Handles the addition of a new record through a dialog.
     */
    @FXML
    protected void addRecord() {
        showSomeone();
    }

    /**
     * Handles the selection of an item in the TableView.
     * Updates the form fields with the selected employee's information.
     *
     * @param mouseEvent The mouse event that triggered the selection.
     */
    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        try {
            Person p = tv.getSelectionModel().getSelectedItem();
            if (p != null) {
                first_name.setText(p.getFirstName());
                last_name.setText(p.getLastName());
                performanceRating.setText(String.valueOf(p.getPerformanceRating()));
                email.setText(p.getEmail());

                // Update profile picture
                updateProfilePicture(p.getFirstName());

                // Set department in ComboBox
                String departmentStr = p.getDepartment();
                if (departmentStr != null && !departmentStr.isEmpty()) {
                    try {
                        Department dept = Department.valueOf(departmentStr.toUpperCase());
                        departmentComboBox.setValue(dept);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid department value: " + departmentStr);
                        departmentComboBox.setValue(null);
                    }
                } else {
                    departmentComboBox.setValue(null);
                }
            } else {
                clearForm();
            }
        } catch (Exception e) {
            System.err.println("Error in selectedItemTV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Applies the light theme to the application.
     *
     * @param actionEvent The event that triggered the theme change.
     */
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

    /**
     * Applies the dark theme to the application.
     *
     * @param actionEvent The event that triggered the theme change.
     */
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

    /**
     * Displays a dialog for adding a new user.
     */
    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email ");
        ObservableList<Department> options = FXCollections.observableArrayList(Department.values());
        ComboBox<Department> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();
        dialogPane.setContent(new VBox(8, textField1, textField2, textField3, comboBox));
        Platform.runLater(textField1::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(), textField2.getText(), comboBox.getValue());
            }
            return null;
        });
        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((Results results) -> {
            MyLogger.makeLog(results.fname + " " + results.lname + " " + results.major);
        });
    }

    /**
     * Enumeration of departments in the organization.
     */
    public enum Department {
        HR, IT, FINANCE, MARKETING, OPERATIONS;

        /**
         * Converts a string to a Department enum value.
         *
         * @param text The string representation of the department.
         * @return The corresponding Department enum value, or null if not found.
         */
        public static Department fromString(String text) {
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

    /**
     * Inner class to hold the results of the new user dialog.
     */
    private static class Results {

        String fname;
        String lname;
        Department major;

        /**
         * Constructs a Results object with the given parameters.
         *
         * @param name  The first name of the new user.
         * @param date  The last name of the new user.
         * @param venue The department of the new user.
         */
        public Results(String name, String date, Department venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }

}