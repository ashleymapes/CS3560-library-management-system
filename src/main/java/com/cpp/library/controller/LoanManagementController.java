package com.cpp.library.controller;

import com.cpp.library.dao.LoanDAO;
import com.cpp.library.dao.StudentDAO;
import com.cpp.library.dao.BookDAO;
import com.cpp.library.dao.BookCopyDAO;
import com.cpp.library.dao.BookRequestDAO;
import com.cpp.library.dao.impl.LoanDAOImpl;
import com.cpp.library.dao.impl.StudentDAOImpl;
import com.cpp.library.dao.impl.BookDAOImpl;
import com.cpp.library.dao.impl.BookCopyDAOImpl;
import com.cpp.library.dao.impl.BookRequestDAOImpl;
import com.cpp.library.model.Loan;
import com.cpp.library.model.Student;
import com.cpp.library.model.Book;
import com.cpp.library.model.BookCopy;
import com.cpp.library.model.BookRequest;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class LoanManagementController {
    
    @FXML private TableView<Loan> loanTable;
    @FXML private TableColumn<Loan, String> loanIdColumn;
    @FXML private TableColumn<Loan, String> studentColumn;
    @FXML private TableColumn<Loan, String> bookColumn;
    @FXML private TableColumn<Loan, String> copyIdColumn;
    @FXML private TableColumn<Loan, String> checkoutDateColumn;
    @FXML private TableColumn<Loan, String> dueDateColumn;
    @FXML private TableColumn<Loan, String> returnDateColumn;
    @FXML private TableColumn<Loan, String> statusColumn;
    @FXML private TableColumn<Loan, String> overdueColumn;
    @FXML private TableColumn<Loan, Void> actionsColumn;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> overdueFilter;
    @FXML private DatePicker dateFilter;
    @FXML private Label loanCountLabel;
    
    private Dialog<Loan> checkoutDialog;
    @FXML private ComboBox<Student> dialogStudent;
    @FXML private ComboBox<Book> dialogBook;
    @FXML private ComboBox<BookCopy> dialogCopy;
    @FXML private DatePicker dialogDueDate;
    @FXML private TextArea dialogNotes;
    
    private Dialog<ButtonType> returnDialog;
    @FXML private Label returnStudentLabel;
    @FXML private Label returnBookLabel;
    @FXML private Label returnCopyLabel;
    @FXML private Label returnDueLabel;
    @FXML private DatePicker returnDatePicker;
    @FXML private TextArea returnNotes;
    
    private Dialog<ButtonType> extendDialog;
    @FXML private Label extendStudentLabel;
    @FXML private Label extendBookLabel;
    @FXML private Label extendCurrentDueLabel;
    @FXML private DatePicker extendNewDueDate;
    @FXML private TextArea extendReason;
    
    private Dialog<ButtonType> requestDialog;
    @FXML private ComboBox<Student> requestStudent;
    @FXML private ComboBox<Book> requestBook;
    @FXML private TextArea requestNotes;
    
    private final LoanDAO loanDAO = new LoanDAOImpl();
    private final StudentDAO studentDAO = new StudentDAOImpl();
    private final BookDAO bookDAO = new BookDAOImpl();
    private final BookCopyDAO bookCopyDAO = new BookCopyDAOImpl();
    private final BookRequestDAO bookRequestDAO = new BookRequestDAOImpl();
    
    private ObservableList<Loan> loansList;
    private FilteredList<Loan> filteredLoans;
    private ObservableList<Student> studentsList;
    private ObservableList<Book> booksList;
    private ObservableList<BookCopy> copiesList;
    private Loan selectedLoan;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    
    @FXML
    public void initialize() {
        try {
            System.out.println("LoanManagementController: initialize start");
            setupTableColumns();
            setupFilters();
            loadLoans();
            setupDialogs();
            System.out.println("LoanManagementController: initialize end");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Initialization Error", e.getMessage());
        }
    }
    
    private void setupTableColumns() {
        // Set up cell value factories
        loanIdColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        studentColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getStudent().getName()));
        bookColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getBookCopy().getBook().getTitle()));
        copyIdColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().getBookCopy().getId())));
        checkoutDateColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getCheckoutDate().format(dateFormatter)));
        dueDateColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDueDate().format(dateFormatter)));
        returnDateColumn.setCellValueFactory(data -> {
            LocalDate returnDate = data.getValue().getReturnDate();
            return new SimpleStringProperty(returnDate != null ? returnDate.format(dateFormatter) : "");
        });
        statusColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getStatus()));
        overdueColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().isOverdue() ? "Yes" : "No"));
        
        // Set up actions column
        actionsColumn.setCellFactory(createActionsCellFactory());
        
        // Set up table selection
        loanTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> selectedLoan = newValue);
    }
    
    private Callback<TableColumn<Loan, Void>, TableCell<Loan, Void>> createActionsCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Loan, Void> call(TableColumn<Loan, Void> param) {
                return new TableCell<>() {
                    private final Button returnBtn = new Button("Return");
                    private final Button extendBtn = new Button("Extend");
                    private final HBox buttonBox = new HBox(5, returnBtn, extendBtn);
                    
                    {
                        returnBtn.setOnAction(event -> {
                            Loan loan = getTableView().getItems().get(getIndex());
                            if (!loan.isReturned()) {
                                showReturnDialog(loan);
                            }
                        });
                        
                        extendBtn.setOnAction(event -> {
                            Loan loan = getTableView().getItems().get(getIndex());
                            if (!loan.isReturned()) {
                                showExtendDialog(loan);
                            }
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Loan loan = getTableView().getItems().get(getIndex());
                            returnBtn.setDisable(loan.isReturned());
                            extendBtn.setDisable(loan.isReturned());
                            setGraphic(buttonBox);
                        }
                    }
                };
            }
        };
    }
    
    private void setupFilters() {
        // Set up status filter options
        statusFilter.getItems().addAll("All", "Active", "Returned", "Extended");
        statusFilter.setValue("All");
        
        // Set up overdue filter options
        overdueFilter.getItems().addAll("All", "Overdue", "Not Overdue");
        overdueFilter.setValue("All");
    }
    
    private void setupDialogs() {
        // Build the checkout dialog form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Student:"), 0, 0);
        grid.add(dialogStudent, 1, 0);
        grid.add(new Label("Book:"), 0, 1);
        grid.add(dialogBook, 1, 1);
        grid.add(new Label("Copy:"), 0, 2);
        grid.add(dialogCopy, 1, 2);
        grid.add(new Label("Due Date:"), 0, 3);
        grid.add(dialogDueDate, 1, 3);
        grid.add(new Label("Notes:"), 0, 4);
        grid.add(dialogNotes, 1, 4);

        // Add listener to update available copies when a book is selected
        dialogBook.setOnAction(event -> loadAvailableCopies());

        // Set custom cell factory for dialogCopy to avoid lazy loading issues
        dialogCopy.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(BookCopy item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getBarCode());
            }
        });
        dialogCopy.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(BookCopy item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getBarCode());
            }
        });

        checkoutDialog = new Dialog<>();
        checkoutDialog.setTitle("Checkout Book");
        checkoutDialog.getDialogPane().setContent(grid);
        checkoutDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        checkoutDialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return createLoanFromDialog();
            }
            return null;
        });

        // Build the return dialog form
        GridPane returnGrid = new GridPane();
        returnGrid.setHgap(10);
        returnGrid.setVgap(10);
        returnGrid.add(new Label("Student:"), 0, 0);
        returnGrid.add(returnStudentLabel, 1, 0);
        returnGrid.add(new Label("Book:"), 0, 1);
        returnGrid.add(returnBookLabel, 1, 1);
        returnGrid.add(new Label("Copy:"), 0, 2);
        returnGrid.add(returnCopyLabel, 1, 2);
        returnGrid.add(new Label("Due Date:"), 0, 3);
        returnGrid.add(returnDueLabel, 1, 3);
        returnGrid.add(new Label("Notes:"), 0, 4);
        returnGrid.add(returnNotes, 1, 4);
        returnDialog = new Dialog<>();
        returnDialog.setTitle("Return Book");
        returnDialog.getDialogPane().setContent(returnGrid);
        returnDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        extendDialog = new Dialog<>();
        extendDialog.setTitle("Extend Loan");
        GridPane extendGrid = new GridPane();
        extendGrid.setHgap(10);
        extendGrid.setVgap(10);
        extendGrid.add(new Label("Student:"), 0, 0);
        extendGrid.add(extendStudentLabel, 1, 0);
        extendGrid.add(new Label("Book:"), 0, 1);
        extendGrid.add(extendBookLabel, 1, 1);
        extendGrid.add(new Label("Current Due Date:"), 0, 2);
        extendGrid.add(extendCurrentDueLabel, 1, 2);
        extendGrid.add(new Label("New Due Date:"), 0, 3);
        extendGrid.add(extendNewDueDate, 1, 3);
        extendGrid.add(new Label("Reason:"), 0, 4);
        extendGrid.add(extendReason, 1, 4);
        extendDialog.getDialogPane().setContent(extendGrid);
        extendDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Set up request dialog
        requestDialog = new Dialog<>();
        requestDialog.setTitle("Request Book");
        GridPane requestGrid = new GridPane();
        requestGrid.setHgap(10);
        requestGrid.setVgap(10);
        requestGrid.add(new Label("Student:"), 0, 0);
        requestGrid.add(requestStudent, 1, 0);
        requestGrid.add(new Label("Book:"), 0, 1);
        requestGrid.add(requestBook, 1, 1);
        requestGrid.add(new Label("Notes:"), 0, 2);
        requestGrid.add(requestNotes, 1, 2);
        requestDialog.getDialogPane().setContent(requestGrid);
        requestDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Set up combo box listeners for checkout dialog
        dialogStudent.setOnAction(e -> loadAvailableBooks());
        dialogBook.setOnAction(e -> loadAvailableCopies());
        dialogStudent.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName() + " (" + item.getBroncoId() + ")");
            }
        });
        dialogStudent.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName() + " (" + item.getBroncoId() + ")");
            }
        });
        dialogBook.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Book item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getTitle() + " by " + item.getAuthor() + " [" + item.getIsbn() + "]");
            }
        });
        dialogBook.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Book item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getTitle() + " by " + item.getAuthor() + " [" + item.getIsbn() + "]");
            }
        });
        
        // Set up cell factories for request dialog
        requestStudent.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName() + " (" + item.getBroncoId() + ")");
            }
        });
        requestStudent.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName() + " (" + item.getBroncoId() + ")");
            }
        });
        requestBook.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Book item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getTitle() + " by " + item.getAuthor() + " [" + item.getIsbn() + "]");
            }
        });
        requestBook.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Book item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getTitle() + " by " + item.getAuthor() + " [" + item.getIsbn() + "]");
            }
        });
    }
    
    @FXML
    private void loadLoans() {
        try {
            List<Loan> loans = loanDAO.findAll();
            loansList = FXCollections.observableArrayList(loans);
            filteredLoans = new FilteredList<>(loansList, p -> true);
            loanTable.setItems(filteredLoans);
            
            updateLoanCount();
            
        } catch (Exception e) {
            showError("Error loading loans", e.getMessage());
        }
    }
    
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        String statusFilterValue = statusFilter.getValue();
        String overdueFilterValue = overdueFilter.getValue();
        LocalDate dateFilterValue = dateFilter.getValue();
        
        filteredLoans.setPredicate(loan -> {
            // Search filter
            boolean matchesSearch = searchText.isEmpty() ||
                loan.getStudent().getName().toLowerCase().contains(searchText) ||
                loan.getBookCopy().getBook().getTitle().toLowerCase().contains(searchText) ||
                String.valueOf(loan.getId()).contains(searchText);
            
            // Status filter
            boolean matchesStatus = "All".equals(statusFilterValue) ||
                ("Active".equals(statusFilterValue) && "Active".equals(loan.getStatus())) ||
                ("Returned".equals(statusFilterValue) && "Returned".equals(loan.getStatus())) ||
                ("Extended".equals(statusFilterValue) && "Extended".equals(loan.getStatus()));
            
            // Overdue filter
            boolean matchesOverdue = "All".equals(overdueFilterValue) ||
                ("Overdue".equals(overdueFilterValue) && loan.isOverdue()) ||
                ("Not Overdue".equals(overdueFilterValue) && !loan.isOverdue());
            
            // Date filter
            boolean matchesDate = dateFilterValue == null ||
                loan.getCheckoutDate().equals(dateFilterValue) ||
                loan.getDueDate().equals(dateFilterValue) ||
                (loan.getReturnDate() != null && loan.getReturnDate().equals(dateFilterValue));
            
            return matchesSearch && matchesStatus && matchesOverdue && matchesDate;
        });
        
        updateLoanCount();
    }
    
    @FXML
    private void clearFilters() {
        searchField.clear();
        statusFilter.setValue("All");
        overdueFilter.setValue("All");
        dateFilter.setValue(null);
        handleSearch();
    }
    
    @FXML
    private void refreshLoans() {
        loadLoans();
    }
    
    @FXML
    private void showCheckoutDialog() {
        try {
            loadStudents();
            clearCheckoutDialog();
            checkoutDialog.setTitle("Checkout Book");
            checkoutDialog.showAndWait().ifPresent(this::processCheckout);
        } catch (Exception e) {
            showError("Error opening checkout dialog", e.getMessage());
        }
    }
    
    private void loadStudents() {
        try {
            List<Student> students = studentDAO.findAll();
            studentsList = FXCollections.observableArrayList(students);
            dialogStudent.setItems(studentsList);
            requestStudent.setItems(studentsList); // Ensure request dialog is populated
        } catch (Exception e) {
            showError("Error loading students", e.getMessage());
        }
    }
    
    private void loadAvailableBooks() {
        try {
            List<Book> books = bookDAO.findAvailableBooks();
            booksList = FXCollections.observableArrayList(books);
            dialogBook.setItems(booksList);
            dialogBook.setValue(null);
            dialogCopy.setItems(FXCollections.observableArrayList());
        } catch (Exception e) {
            showError("Error loading books", e.getMessage());
        }
    }
    
    private void loadAvailableCopies() {
        Book selectedBook = dialogBook.getValue();
        if (selectedBook == null) return;
        
        try {
            List<BookCopy> copies = bookCopyDAO.findAvailableByBook(selectedBook);
            copiesList = FXCollections.observableArrayList(copies);
            dialogCopy.setItems(copiesList);
            dialogCopy.setValue(null);
        } catch (Exception e) {
            showError("Error loading copies", e.getMessage());
        }
    }
    
    private void clearCheckoutDialog() {
        dialogStudent.setValue(null);
        dialogBook.setValue(null);
        dialogCopy.setValue(null);
        dialogDueDate.setValue(LocalDate.now().plusDays(30)); // Default 30 days
        dialogNotes.clear();
    }
    
    private Loan createLoanFromDialog() {
        Student student = dialogStudent.getValue();
        BookCopy copy = dialogCopy.getValue();
        LocalDate dueDate = dialogDueDate.getValue();
        String notes = dialogNotes.getText().trim();
        
        if (student == null || copy == null || dueDate == null) {
            showAlert("Validation Error", "Student, Book Copy, and Due Date are required.");
            return null;
        }
        
        if (dueDate.isBefore(LocalDate.now())) {
            showAlert("Validation Error", "Due date cannot be in the past.");
            return null;
        }
        
        // Enforce 180-day max loan duration
        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
        if (days > 180) {
            showAlert("Loan Duration Exceeded", "Loan duration cannot exceed 180 days.");
            return null;
        }
        
        // Check business rules
        if (student.getActiveLoansCount() >= 5) {
            showAlert("Loan Limit Exceeded", "Student has reached the maximum of 5 active loans.");
            return null;
        }
        
        if (student.hasOverdueLoans()) {
            showAlert("Overdue Loans", "Student has overdue loans and cannot borrow more books.");
            return null;
        }
        
        // Check if student already has this book copy loaned out
        if (loanDAO.hasActiveLoanForCopy(student.getBroncoId(), copy.getBarCode())) {
            showAlert("Duplicate Loan", "Student already has this book copy loaned out.");
            return null;
        }
        
        return new Loan(student, copy, LocalDate.now(), dueDate, notes);
    }
    
    @FXML
    private void processCheckout(Loan loan) {
        if (loan == null) return;
        try {
            // Mark the book copy as borrowed
            BookCopy copy = loan.getBookCopy();
            copy.setStatus(BookCopy.Status.BORROWED);
            bookCopyDAO.update(copy);

            loanDAO.save(loan);

            // Fulfill any pending book request for this student and book
            Student student = loan.getStudent();
            Book book = copy.getBook();
            List<BookRequest> pendingRequests = bookRequestDAO.findPendingRequestsForBook(book);
            for (BookRequest req : pendingRequests) {
                if (req.getStudent().getBroncoId().equals(student.getBroncoId())) {
                    req.setStatus("fulfilled");
                    bookRequestDAO.update(req);
                }
            }

            showInfo("Success", "Book checked out successfully.");
            showLoanReceipt(loan);
            loadLoans();
            checkoutDialog.close();
        } catch (Exception e) {
            showError("Error processing checkout", e.getMessage());
        }
    }
    
    @FXML
    private void cancelCheckoutDialog() {
        checkoutDialog.close();
    }
    
    @FXML
    private void returnBook() {
        if (selectedLoan == null) {
            showAlert("No Selection", "Please select a loan to return.");
            return;
        }
        if (selectedLoan.isReturned()) {
            showAlert("Already Returned", "This book has already been returned.");
            return;
        }
        showReturnDialog(selectedLoan);
    }
    
    private void showReturnDialog(Loan loan) {
        returnStudentLabel.setText(loan.getStudent().getName());
        returnBookLabel.setText(loan.getBookCopy().getBook().getTitle());
        returnCopyLabel.setText(String.valueOf(loan.getBookCopy().getId()));
        returnDueLabel.setText(loan.getDueDate().format(dateFormatter));
        returnDatePicker.setValue(LocalDate.now());
        returnNotes.clear();
        
        Optional<ButtonType> result = returnDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            processReturn(loan);
        }
    }
    
    @FXML
    private void processReturn(Loan loan) {
        try {
            LocalDate returnDate = returnDatePicker.getValue();
            String notes = returnNotes.getText().trim();
            
            if (returnDate == null) {
                showAlert("Validation Error", "Return date is required.");
                return;
            }
            
            loan.setReturnDate(LocalDate.now());
            loan.setReturnNotes(notes);
            
            // Update book copy condition and status
            BookCopy copy = loan.getBookCopy();
            copy.setCondition("Good"); // Default condition
            copy.setStatus(BookCopy.Status.AVAILABLE);
            bookCopyDAO.update(copy);
            
            loanDAO.update(loan);
            showInfo("Success", "Book returned successfully.");
            loadLoans();
            handleSearch();
            returnDialog.close();
            
        } catch (Exception e) {
            showError("Error processing return", e.getMessage());
        }
    }
    
    @FXML
    private void cancelReturnDialog() {
        returnDialog.close();
    }
    
    @FXML
    private void extendLoan() {
        if (selectedLoan == null) {
            showAlert("No Selection", "Please select a loan to extend.");
            return;
        }
        if (selectedLoan.isReturned()) {
            showAlert("Already Returned", "Cannot extend a returned loan.");
            return;
        }
        
        // Check if there are pending requests for this book
        Book book = selectedLoan.getBookCopy().getBook();
        List<BookRequest> pendingRequests = bookRequestDAO.findPendingRequestsForBook(book);
        if (!pendingRequests.isEmpty()) {
            showAlert("Extension Not Allowed", 
                "This book has " + pendingRequests.size() + " pending request(s) from other students and cannot be extended.\n\n" +
                "Please return the book so it can be loaned to waiting students.");
            return;
        }
        
        showExtendDialog(selectedLoan);
    }
    
    private void showExtendDialog(Loan loan) {
        extendStudentLabel.setText(loan.getStudent().getName());
        extendBookLabel.setText(loan.getBookCopy().getBook().getTitle());
        extendCurrentDueLabel.setText(loan.getDueDate().format(dateFormatter));
        extendNewDueDate.setValue(loan.getDueDate().plusDays(30)); // Default 30 days extension
        extendReason.clear();
        
        Optional<ButtonType> result = extendDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            processExtend(loan);
        }
    }
    
    @FXML
    private void processExtend(Loan loan) {
        try {
            LocalDate newDueDate = extendNewDueDate.getValue();
            String reason = extendReason.getText().trim();
            
            if (newDueDate == null) {
                showAlert("Validation Error", "New due date is required.");
                return;
            }
            
            if (newDueDate.isBefore(loan.getDueDate())) {
                showAlert("Validation Error", "New due date cannot be before current due date.");
                return;
            }
            
            // Check maximum loan duration (180 days)
            long totalDays = java.time.temporal.ChronoUnit.DAYS.between(loan.getCheckoutDate(), newDueDate);
            if (totalDays > 180) {
                showAlert("Loan Duration Exceeded", "Total loan duration cannot exceed 180 days.");
                return;
            }

            // 1. Mark previous loan as extended and returned
            loan.setReturnDate(LocalDate.now());
            loan.setExtensionReason(reason);
            loan.setStatus("Extended");
            loanDAO.update(loan);

            // 2. Create a new loan for the same student and book copy
            Student student = loan.getStudent();
            BookCopy copy = loan.getBookCopy();
            Loan newLoan = new Loan(student, copy, LocalDate.now(), newDueDate, "Extended from loan ID: " + loan.getLoanId());
            loanDAO.save(newLoan);

            showInfo("Success", "Loan extended successfully.");
            loadLoans();
            handleSearch();
            extendDialog.close();

        } catch (Exception e) {
            showError("Error extending loan", e.getMessage());
        }
    }
    
    @FXML
    private void cancelExtendDialog() {
        extendDialog.close();
    }
    
    @FXML
    private void viewLoanDetails() {
        if (selectedLoan == null) {
            showAlert("No Selection", "Please select a loan to view details.");
            return;
        }
        
        StringBuilder details = new StringBuilder();
        details.append("Loan ID: ").append(selectedLoan.getId()).append("\n");
        details.append("Student: ").append(selectedLoan.getStudent().getName()).append(" (").append(selectedLoan.getStudent().getBroncoId()).append(")\n");
        details.append("Book: ").append(selectedLoan.getBookCopy().getBook().getTitle()).append(" by ").append(selectedLoan.getBookCopy().getBook().getAuthor()).append("\n");
        details.append("Copy ID: ").append(selectedLoan.getBookCopy().getId()).append("\n");
        details.append("Checkout Date: ").append(selectedLoan.getCheckoutDate().format(dateFormatter)).append("\n");
        details.append("Due Date: ").append(selectedLoan.getDueDate().format(dateFormatter)).append("\n");
        
        if (selectedLoan.getReturnDate() != null) {
            details.append("Return Date: ").append(selectedLoan.getReturnDate().format(dateFormatter)).append("\n");
        }
        
        details.append("Status: ").append(selectedLoan.isReturned() ? "Returned" : "Active").append("\n");
        details.append("Overdue: ").append(selectedLoan.isOverdue() ? "Yes" : "No").append("\n");
        
        if (selectedLoan.getNotes() != null && !selectedLoan.getNotes().isEmpty()) {
            details.append("Notes: ").append(selectedLoan.getNotes()).append("\n");
        }
        
        if (selectedLoan.getExtensionReason() != null && !selectedLoan.getExtensionReason().isEmpty()) {
            details.append("Extension Reason: ").append(selectedLoan.getExtensionReason()).append("\n");
        }
        
        showInfo("Loan Details", details.toString());
    }
    
    @FXML
    private void showRequestQueueDialog() {
        Loan selectedLoan = loanTable.getSelectionModel().getSelectedItem();
        if (selectedLoan == null) {
            showAlert("No Selection", "Please select a loan to view its request queue.");
            return;
        }
        Book book = selectedLoan.getBookCopy().getBook();
        List<BookRequest> requests = bookRequestDAO.findPendingRequestsForBook(book);

        StringBuilder sb = new StringBuilder();
        sb.append("Request Queue for: ").append(book.getTitle()).append("\n\n");
        if (requests.isEmpty()) {
            sb.append("No pending requests.");
        } else {
            for (BookRequest req : requests) {
                sb.append("Student: ").append(req.getStudent().getName())
                  .append(" (").append(req.getStudent().getBroncoId()).append(")\n")
                  .append("Requested on: ").append(req.getRequestDate()).append("\n")
                  .append("Status: ").append(req.getStatus()).append("\n\n");
            }
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Book Request Queue");
        alert.setHeaderText(null);
        alert.setContentText(sb.toString());
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
    
    private void updateLoanCount() {
        int total = loansList.size();
        int filtered = filteredLoans.size();
        loanCountLabel.setText(String.format("Showing: %d of %d", filtered, total));
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
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Show a loan receipt after successful checkout
    private void showLoanReceipt(Loan loan) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("Loan Receipt\n");
        receipt.append("------------\n");
        receipt.append("Loan Number: ").append(loan.getId()).append("\n");
        receipt.append("Student: ").append(loan.getStudent().getName()).append("\n");
        receipt.append("Borrow Date: ").append(loan.getCheckoutDate()).append("\n");
        receipt.append("Due Date: ").append(loan.getDueDate()).append("\n");
        receipt.append("Book: ").append(loan.getBookCopy().getBook().getTitle()).append("\n");
        receipt.append("Copy Barcode: ").append(loan.getBookCopy().getBarCode()).append("\n");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Loan Receipt");
        alert.setHeaderText("Loan Receipt");
        alert.setContentText(receipt.toString());
        alert.showAndWait();
    }
    
    @FXML
    private void showRequestDialog() {
        try {
            loadStudents();
            loadAllBooks(); // Load all books, not just available ones
            clearRequestDialog();
            requestDialog.setTitle("Request Book");
            requestDialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    processRequest();
                }
            });
        } catch (Exception e) {
            showError("Error opening request dialog", e.getMessage());
        }
    }
    
    private void loadAllBooks() {
        try {
            List<Book> books = bookDAO.findAll();
            booksList = FXCollections.observableArrayList(books);
            requestBook.setItems(booksList);
        } catch (Exception e) {
            showError("Error loading books", e.getMessage());
        }
    }
    
    private void clearRequestDialog() {
        requestStudent.setValue(null);
        requestBook.setValue(null);
        requestNotes.clear();
    }
    
    private void processRequest() {
        try {
            Student student = requestStudent.getValue();
            Book book = requestBook.getValue();
            String notes = requestNotes.getText().trim();
            
            if (student == null) {
                showAlert("Validation Error", "Please select a student.");
                return;
            }
            
            if (book == null) {
                showAlert("Validation Error", "Please select a book.");
                return;
            }
            
            // Check if student already has a pending request for this book
            List<BookRequest> existingRequests = bookRequestDAO.findPendingRequestsForBook(book);
            for (BookRequest req : existingRequests) {
                if (req.getStudent().getBroncoId().equals(student.getBroncoId())) {
                    showAlert("Duplicate Request", "You already have a pending request for this book.");
                    return;
                }
            }
            
            // Create the book request
            BookRequest request = new BookRequest(student, book, LocalDate.now(), "pending");
            bookRequestDAO.save(request);
            
            showInfo("Success", "Book request submitted successfully.");
            clearRequestDialog();
            requestDialog.close();
            
        } catch (Exception e) {
            showError("Error processing request", e.getMessage());
        }
    }
    
    @FXML
    private void cancelRequestDialog() {
        requestDialog.close();
    }
} 