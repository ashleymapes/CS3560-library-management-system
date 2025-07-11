package com.cpp.library.controller;

import com.cpp.library.dao.BookDAO;
import com.cpp.library.dao.LoanDAO;
import com.cpp.library.dao.StudentDAO;
import com.cpp.library.dao.impl.BookDAOImpl;
import com.cpp.library.dao.impl.LoanDAOImpl;
import com.cpp.library.dao.impl.StudentDAOImpl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.scene.control.TextArea;
import java.io.PrintWriter;
import java.io.StringWriter;

public class DashboardController {
    
    @FXML private Text totalStudentsText;
    @FXML private Text totalBooksText;
    @FXML private Text activeLoansText;
    @FXML private Text overdueLoansText;
    
    private final StudentDAO studentDAO = new StudentDAOImpl();
    private final BookDAO bookDAO = new BookDAOImpl();
    private final LoanDAO loanDAO = new LoanDAOImpl();
    
    @FXML
    public void initialize() {
        loadStatistics();
    }
    
    private void loadStatistics() {
        try {
            // Load statistics from database
            long totalStudents = studentDAO.findAll().size();
            long totalBooks = bookDAO.findAll().size();
            long activeLoans = loanDAO.findActiveLoans().size();
            long overdueLoans = loanDAO.findOverdueLoans().size();
            
            // Update UI
            totalStudentsText.setText("Students: " + totalStudents);
            totalBooksText.setText("Books: " + totalBooks);
            activeLoansText.setText("Active Loans: " + activeLoans);
            overdueLoansText.setText("Overdue: " + overdueLoans);
            
        } catch (Exception e) {
            showError("Error loading statistics", e.getMessage());
        }
    }
    
    @FXML
    private void openStudentManagement() {
        openScreen("/view/StudentManagement.fxml", "Student Management");
    }
    
    @FXML
    private void openBookManagement() {
        openScreen("/view/BookManagement.fxml", "Book Management");
    }
    
    @FXML
    private void openLoanManagement() {
        openScreen("/view/LoanManagement.fxml", "Loan Management");
    }
    
    @FXML
    private void openReports() {
        openScreen("/view/Reports.fxml", "Reports");
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);

        // Try to get the stack trace from the last exception if available
        String exceptionText = "";
        Throwable lastException = getLastException();
        if (lastException != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            lastException.printStackTrace(pw);
            exceptionText = sw.toString();
        }

        TextArea textArea = new TextArea(content + (exceptionText.isEmpty() ? "" : ("\n\n" + exceptionText)));
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    // Helper to get the last exception if available (for debugging)
    private Throwable lastException = null;
    private void setLastException(Throwable t) { this.lastException = t; }
    private Throwable getLastException() { return this.lastException; }

    // Update openScreen to save the exception for showError
    private void openScreen(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root, 1000, 700));
            stage.setMinWidth(800);
            stage.setMinHeight(600);

            // Set up close handler to refresh dashboard statistics
            stage.setOnCloseRequest(event -> loadStatistics());

            stage.show();

        } catch (IOException e) {
            setLastException(e);
            showError("Error opening " + title, e.getMessage());
        } catch (Exception e) {
            setLastException(e);
            showError("Error opening " + title, e.getMessage());
        }
    }
    
    @FXML
    private void refreshStatistics() {
        loadStatistics();
    }
} 