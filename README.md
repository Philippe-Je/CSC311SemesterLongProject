# Employee Data Management System

## Overview

The Employee Data Management System is a comprehensive JavaFX application designed for efficient management of employee data. It provides a user-friendly interface for adding, editing, and deleting employee records, as well as features for data visualization and reporting.

## Features

- **User Authentication**: Secure login system with username and password.
- **Employee Management**: Add, edit, and delete employee records.
- **Data Visualization**: View employee data in a tabular format.
- **Profile Picture Management**: Upload and manage employee profile pictures.
- **Data Import/Export**: Import and export employee data in CSV format.
- **Reporting**: Generate PDF reports of employee data.
- **Azure Blob Storage Integration**: Store and retrieve profile pictures using Azure Blob Storage.

## Technical Stack

- **Language**: Java
- **UI Framework**: JavaFX
- **Database**: MySQL
- **Cloud Storage**: Azure Blob Storage
- **Additional Libraries**:
    - iText for PDF generation
    - OpenCSV for CSV file handling
    - Azure Storage Blob for cloud storage operations

## Project Structure

The project is organized into several packages:

- `dao`: Data Access Objects for database operations and cloud storage.
- `model`: Contains the `Person` class representing an employee.
- `service`: Utility classes for logging and user session management.
- `viewmodel`: Controllers for different views in the application.

## Key Components

### DbConnectivityClass

Handles all database operations including CRUD operations on employee records.

### StorageUploader

Manages file uploads to Azure Blob Storage, particularly for profile pictures.

### Person

Represents an employee with attributes like name, department, performance rating, etc.

### UserSession

Manages user sessions and credentials using the Singleton pattern.

### LoginController

Handles user authentication and navigation to the main interface.

### DB_GUI_Controller

Main controller for the employee management interface, handling most of the application's functionality.

### MainApplication

The entry point of the application, managing scene transitions and initial setup.

## Setup and Configuration

1. **Database Setup**:
    - Ensure MySQL is installed and running.
    - Update the database connection details in `DbConnectivityClass`.

2. **Azure Blob Storage**:
    - Set up an Azure Blob Storage account.
    - Update the connection string in `StorageUploader`.

3. **Dependencies**:
    - Ensure all required libraries are included in the project's classpath.

4. **Running the Application**:
    - Execute the `main` method in `MainApplication` class.

## Usage

1. **Login**: Use registered credentials to log in.
2. **Main Interface**: Navigate through the tabs to manage employee data.
3. **Adding Employees**: Fill in the form and click 'Add' to create new employee records.
4. **Editing/Deleting**: Select an employee from the table to edit or delete their information.
5. **Profile Pictures**: Upload or delete profile pictures for employees.
6. **Data Import/Export**: Use the menu options to import or export data in CSV format.
7. **Reporting**: Generate PDF reports of employee data as needed.

## Security Considerations

- Passwords are hashed before storage.
- User sessions are managed securely.
- Input validation is implemented to prevent malicious data entry.

## Author
[Philippe-Je](https://github.com/Philippe-Je)