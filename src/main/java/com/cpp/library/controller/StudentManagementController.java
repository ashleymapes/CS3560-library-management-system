package com.cpp.library.controller;

import com.cpp.library.dao.StudentDAO;
import com.cpp.library.dao.impl.StudentDAOImpl;
import com.cpp.library.model.Student;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;

public class StudentManagementController {
    
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> broncoIdColumn;
    @FXML private TableColumn<Student, String> nameColumn;
    @FXML private TableColumn<Student, String> addressColumn;
    @FXML private TableColumn<Student, String> degreeColumn;
    @FXML private TableColumn<Student, String> activeLoansColumn;
    @FXML private TableColumn<Student, String> overdueColumn;
    @FXML private TableColumn<Student, Void> actionsColumn;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> degreeFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label studentCountLabel;
    
    private Dialog<Student> studentDialog;
    private TextField dialogBroncoId;
    private TextField dialogName;
    private TextArea dialogAddress;
    private ComboBox<String> dialogDegree;
    
    private final StudentDAO studentDAO = new StudentDAOImpl();
    private ObservableList<Student> studentsList;
    private FilteredList<Student> filteredStudents;
    private Student selectedStudent;
    private boolean isEditMode = false;
    
    @FXML
    public void initialize() {
        try {
            System.out.println("StudentManagementController: initialize start");
            setupTableColumns();
            System.out.println("setupTableColumns done");
            setupFilters();
            System.out.println("setupFilters done");
            loadStudents();
            System.out.println("loadStudents done");
            setupDialog();
            System.out.println("setupDialog done");
            System.out.println("StudentManagementController: initialize end");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Initialization Error", e.getMessage(), e);
        }
    }
    
    private void setupTableColumns() {
        // Set up cell value factories
        broncoIdColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getBroncoId()));
        nameColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getName()));
        addressColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getAddress()));
        degreeColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDegree()));
        activeLoansColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().getActiveLoansCount())));
        overdueColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().hasOverdueLoans() ? "Yes" : "No"));
        
        // Set up actions column
        actionsColumn.setCellFactory(createActionsCellFactory());
        
        // Set up table selection
        studentTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> selectedStudent = newValue);
    }
    
    private Callback<TableColumn<Student, Void>, TableCell<Student, Void>> createActionsCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Student, Void> call(TableColumn<Student, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    private final HBox buttonBox = new HBox(5, editBtn, deleteBtn);
                    
                    {
                        editBtn.setOnAction(event -> {
                            Student student = getTableView().getItems().get(getIndex());
                            showEditStudentDialog(student);
                        });
                        
                        deleteBtn.setOnAction(event -> {
                            Student student = getTableView().getItems().get(getIndex());
                            deleteStudent(student);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : buttonBox);
                    }
                };
            }
        };
    }
    
    private void setupFilters() {
        // Set up status filter options
        statusFilter.getItems().addAll("All", "With Active Loans", "With Overdue Loans", "No Active Loans");
        statusFilter.setValue("All");
        
        // Set up degree filter (will be populated when students are loaded)
        degreeFilter.getItems().add("All");
        degreeFilter.setValue("All");
    }
    
    private void setupDialog() {
        studentDialog = new Dialog<>();
        studentDialog.setTitle("Student Information");
        studentDialog.setResizable(false);
        
        dialogBroncoId = new TextField();
        dialogName = new TextField();
        dialogAddress = new TextArea();
        dialogAddress.setPrefRowCount(3);
        dialogAddress.setWrapText(true);
        dialogDegree = new ComboBox<>();
        dialogDegree.setEditable(true);
        dialogDegree.getItems().addAll(
            "Computer Science", "Engineering", "Business", "Arts", "Sciences", 
            "Education", "Health Sciences", "Other"
        );
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Bronco ID:"), 0, 0);
        grid.add(dialogBroncoId, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(dialogName, 1, 1);
        grid.add(new Label("Address:"), 0, 2);
        grid.add(dialogAddress, 1, 2);
        grid.add(new Label("Degree:"), 0, 3);
        grid.add(dialogDegree, 1, 3);
        
        studentDialog.getDialogPane().setContent(grid);
        studentDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        studentDialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return createStudentFromDialog();
            }
            return null;
        });
    }
    
    @FXML
    private void loadStudents() {
        try {
            System.out.println("loadStudents: start");
            List<Student> students = studentDAO.findAll();
            System.out.println("loadStudents: students loaded: " + students.size());
            studentsList = FXCollections.observableArrayList(students);
            filteredStudents = new FilteredList<>(studentsList, p -> true);
            studentTable.setItems(filteredStudents);
            System.out.println("loadStudents: table set");
            updateStudentCount();
            System.out.println("loadStudents: updateStudentCount done");
            populateDegreeFilter();
            System.out.println("loadStudents: populateDegreeFilter done");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading students", e.getMessage(), e);
        }
    }
    
    private void populateDegreeFilter() {
        degreeFilter.getItems().clear();
        degreeFilter.getItems().add("All");
        
        studentsList.stream()
            .map(Student::getDegree)
            .distinct()
            .sorted()
            .forEach(degree -> degreeFilter.getItems().add(degree));
        
        degreeFilter.setValue("All");
    }
    
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        String degreeFilterValue = degreeFilter.getValue();
        String statusFilterValue = statusFilter.getValue();
        
        filteredStudents.setPredicate(student -> {
            // Search filter
            boolean matchesSearch = searchText.isEmpty() ||
                student.getName().toLowerCase().contains(searchText) ||
                student.getBroncoId().toLowerCase().contains(searchText) ||
                student.getDegree().toLowerCase().contains(searchText);
            
            // Degree filter
            boolean matchesDegree = "All".equals(degreeFilterValue) ||
                student.getDegree().equals(degreeFilterValue);
            
            // Status filter
            boolean matchesStatus = "All".equals(statusFilterValue) ||
                ("With Active Loans".equals(statusFilterValue) && student.getActiveLoansCount() > 0) ||
                ("With Overdue Loans".equals(statusFilterValue) && student.hasOverdueLoans()) ||
                ("No Active Loans".equals(statusFilterValue) && student.getActiveLoansCount() == 0);
            
            return matchesSearch && matchesDegree && matchesStatus;
        });
        
        updateStudentCount();
    }
    
    @FXML
    private void clearFilters() {
        searchField.clear();
        degreeFilter.setValue("All");
        statusFilter.setValue("All");
        handleSearch();
    }
    
    @FXML
    private void refreshStudents() {
        loadStudents();
    }
    
    @FXML
    private void showAddStudentDialog() {
        isEditMode = false;
        clearDialog();
        studentDialog.setTitle("Add New Student");
        studentDialog.showAndWait().ifPresent(this::saveStudent);
    }
    
    @FXML
    private void showEditStudentDialog() {
        if (selectedStudent == null) {
            showAlert("No Selection", "Please select a student to edit.");
            return;
        }
        showEditStudentDialog(selectedStudent);
    }
    
    private void showEditStudentDialog(Student student) {
        isEditMode = true;
        populateDialog(student);
        studentDialog.setTitle("Edit Student");
        studentDialog.showAndWait().ifPresent(this::saveStudent);
    }
    
    private void clearDialog() {
        dialogBroncoId.clear();
        dialogName.clear();
        dialogAddress.clear();
        dialogDegree.setValue(null);
        
        if (!isEditMode) {
            dialogBroncoId.setDisable(false);
        }
    }
    
    private void populateDialog(Student student) {
        dialogBroncoId.setText(student.getBroncoId());
        dialogName.setText(student.getName());
        dialogAddress.setText(student.getAddress());
        dialogDegree.setValue(student.getDegree());
        
        if (isEditMode) {
            dialogBroncoId.setDisable(true); // Can't change Bronco ID
        }
    }
    
    private Student createStudentFromDialog() {
        String broncoId = dialogBroncoId.getText().trim();
        String name = dialogName.getText().trim();
        String address = dialogAddress.getText().trim();
        String degree = dialogDegree.getValue();
        
        if (broncoId.isEmpty() || name.isEmpty() || address.isEmpty() || degree == null) {
            showAlert("Validation Error", "All fields are required.");
            return null;
        }
        
        if (isEditMode) {
            selectedStudent.setName(name);
            selectedStudent.setAddress(address);
            selectedStudent.setDegree(degree);
            return selectedStudent;
        } else {
            return new Student(broncoId, name, address, degree);
        }
    }
    
    @FXML
    private void saveStudent(Student student) {
        if (student == null) return;
        
        try {
            if (isEditMode) {
                studentDAO.update(student);
                showInfo("Success", "Student updated successfully.");
            } else {
                if (studentDAO.existsByBroncoId(student.getBroncoId())) {
                    showAlert("Error", "A student with this Bronco ID already exists.");
                    return;
                }
                studentDAO.save(student);
                showInfo("Success", "Student added successfully.");
            }
            
            loadStudents();
            studentDialog.close();
            
        } catch (Exception e) {
            showError("Error saving student", e.getMessage(), e);
        }
    }
    
    @FXML
    private void cancelStudentDialog() {
        studentDialog.close();
    }
    
    @FXML
    private void deleteStudent() {
        if (selectedStudent == null) {
            showAlert("No Selection", "Please select a student to delete.");
            return;
        }
        deleteStudent(selectedStudent);
    }
    
    private void deleteStudent(Student student) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Student");
        alert.setContentText("Are you sure you want to delete student '" + student.getName() + "'?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Check if student has active loans
                if (student.getActiveLoansCount() > 0) {
                    showAlert("Cannot Delete", 
                        "Cannot delete student with active loans. Please return all books first.");
                    return;
                }
                
                studentDAO.delete(student);
                showInfo("Success", "Student deleted successfully.");
                loadStudents();
                
            } catch (Exception e) {
                showError("Error deleting student", e.getMessage(), e);
            }
        }
    }
    
    @FXML
    private void viewStudentLoans() {
        if (selectedStudent == null) {
            showAlert("No Selection", "Please select a student to view loans.");
            return;
        }
        
        try {
            // Open loan management screen filtered for this student
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoanManagement.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the student filter
            // This will be implemented when we create the LoanManagementController
            
            Stage stage = new Stage();
            stage.setTitle("Loans for " + selectedStudent.getName());
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();
            
        } catch (IOException e) {
            showError("Error opening loan management", e.getMessage(), e);
        }
    }
    
    private void updateStudentCount() {
        int total = studentsList.size();
        int filtered = filteredStudents.size();
        studentCountLabel.setText(String.format("Showing: %d of %d", filtered, total));
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showError(String title, String content, Throwable t) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        String exceptionText = sw.toString();

        TextArea textArea = new TextArea(content + "\n\n" + exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }
} 