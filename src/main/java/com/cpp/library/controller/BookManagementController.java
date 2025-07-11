package com.cpp.library.controller;

import com.cpp.library.dao.BookDAO;
import com.cpp.library.dao.BookCopyDAO;
import com.cpp.library.dao.impl.BookDAOImpl;
import com.cpp.library.dao.impl.BookCopyDAOImpl;
import com.cpp.library.model.Book;
import com.cpp.library.model.BookCopy;
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
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class BookManagementController {
    
    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, String> isbnColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> subjectColumn;
    @FXML private TableColumn<Book, String> totalCopiesColumn;
    @FXML private TableColumn<Book, String> availableCopiesColumn;
    @FXML private TableColumn<Book, String> loanedCopiesColumn;
    @FXML private TableColumn<Book, Void> actionsColumn;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> subjectFilter;
    @FXML private ComboBox<String> availabilityFilter;
    @FXML private Label bookCountLabel;
    
    private Dialog<Book> bookDialog;
    @FXML private TextField dialogIsbn;
    @FXML private TextField dialogTitle;
    @FXML private TextField dialogAuthor;
    @FXML private ComboBox<String> dialogSubject;
    @FXML private TextArea dialogDescription;
    @FXML private Spinner<Integer> dialogCopies;
    
    private Dialog<Void> copyDialog;
    @FXML private Text copyDialogTitle;
    @FXML private Label totalCopiesLabel;
    @FXML private Label availableCopiesLabel;
    @FXML private Label loanedCopiesLabel;
    @FXML private TableView<BookCopy> copyTable;
    @FXML private TableColumn<BookCopy, String> copyIdColumn;
    @FXML private TableColumn<BookCopy, String> copyStatusColumn;
    @FXML private TableColumn<BookCopy, Void> copyActionsColumn;
    
    private final BookDAO bookDAO = new BookDAOImpl();
    private final BookCopyDAO bookCopyDAO = new BookCopyDAOImpl();
    private ObservableList<Book> booksList;
    private FilteredList<Book> filteredBooks;
    private ObservableList<BookCopy> copiesList;
    private Book selectedBook;
    private boolean isEditMode = false;
    
    @FXML
    public void initialize() {
        try {
            System.out.println("BookManagementController: initialize start");
            setupTableColumns();
            setupFilters();
            loadBooks();
            setupDialog();
            setupCopyDialog();
            System.out.println("BookManagementController: initialize end");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Initialization Error", e.getMessage());
        }
    }
    
    private void setupTableColumns() {
        // Set up cell value factories
        isbnColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getIsbn()));
        titleColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getTitle()));
        authorColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getAuthor()));
        subjectColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getSubject()));
        totalCopiesColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().getTotalCopies())));
        availableCopiesColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().getAvailableCopiesCount())));
        loanedCopiesColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().getLoanedCopies())));
        
        // Set up actions column
        actionsColumn.setCellFactory(createActionsCellFactory());
        
        // Set up table selection
        bookTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> selectedBook = newValue);
    }
    
    private Callback<TableColumn<Book, Void>, TableCell<Book, Void>> createActionsCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Book, Void> call(TableColumn<Book, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    private final Button copiesBtn = new Button("Copies");
                    private final HBox buttonBox = new HBox(5, editBtn, deleteBtn, copiesBtn);
                    
                    {
                        editBtn.setOnAction(event -> {
                            Book book = getTableView().getItems().get(getIndex());
                            showEditBookDialog(book);
                        });
                        
                        deleteBtn.setOnAction(event -> {
                            Book book = getTableView().getItems().get(getIndex());
                            deleteBook(book);
                        });
                        
                        copiesBtn.setOnAction(event -> {
                            Book book = getTableView().getItems().get(getIndex());
                            manageCopies(book);
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
        // Set up availability filter options
        availabilityFilter.getItems().addAll("All", "Available", "Partially Available", "Unavailable");
        availabilityFilter.setValue("All");
        
        // Set up subject filter (will be populated when books are loaded)
        subjectFilter.getItems().add("All");
        subjectFilter.setValue("All");
    }
    
    private void setupDialog() {
        // Set up subject options in dialog
        dialogSubject.getItems().addAll(
            "Computer Science", "Engineering", "Mathematics", "Physics", "Chemistry",
            "Biology", "Business", "Economics", "History", "Literature", "Philosophy",
            "Psychology", "Sociology", "Art", "Music", "Other"
        );

        // Initialize dialogCopies Spinner value factory
        dialogCopies.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));

        // Build the dialog UI in code
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("ISBN:"), 0, 0);
        grid.add(dialogIsbn, 1, 0);
        grid.add(new Label("Title:"), 0, 1);
        grid.add(dialogTitle, 1, 1);
        grid.add(new Label("Author:"), 0, 2);
        grid.add(dialogAuthor, 1, 2);
        grid.add(new Label("Subject:"), 0, 3);
        grid.add(dialogSubject, 1, 3);
        grid.add(new Label("Description:"), 0, 4);
        grid.add(dialogDescription, 1, 4);
        grid.add(new Label("Initial Copies:"), 0, 5);
        grid.add(dialogCopies, 1, 5);

        bookDialog = new Dialog<>();
        bookDialog.setTitle("Add New Book");
        bookDialog.getDialogPane().setContent(grid);
        bookDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        // Set up dialog result converter
        bookDialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return createBookFromDialog();
            }
            return null;
        });
    }
    
    private void setupCopyDialog() {
        // Set up copy table columns
        copyIdColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        copyStatusColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().isAvailable() ? "Available" : "Loaned"));

        // Set preferred width for the table and dialog
        copyTable.setPrefWidth(500); // or any width you prefer
        VBox vbox = new VBox(10);
        vbox.setPrefWidth(520); // slightly wider than the table
        vbox.getChildren().addAll(
            copyDialogTitle,
            totalCopiesLabel,
            availableCopiesLabel,
            loanedCopiesLabel,
            copyTable
        );

        copyDialog = new Dialog<>();
        copyDialog.setTitle("Manage Book Copies");
        copyDialog.getDialogPane().setContent(vbox);
        copyDialog.getDialogPane().setPrefWidth(540); // for the dialog pane itself
        copyDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Set up copy actions column
        copyActionsColumn.setCellFactory(createCopyActionsCellFactory());
    }
    
    private Callback<TableColumn<BookCopy, Void>, TableCell<BookCopy, Void>> createCopyActionsCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<BookCopy, Void> call(TableColumn<BookCopy, Void> param) {
                return new TableCell<>() {
                    private final Button removeBtn = new Button("Remove");
                    private final HBox buttonBox = new HBox(5, removeBtn);
                    
                    {
                        removeBtn.setOnAction(event -> {
                            BookCopy copy = getTableView().getItems().get(getIndex());
                            removeCopy(copy);
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
    
    @FXML
    private void loadBooks() {
        try {
            List<Book> books = bookDAO.findAll();
            booksList = FXCollections.observableArrayList(books);
            filteredBooks = new FilteredList<>(booksList, p -> true);
            bookTable.setItems(filteredBooks);
            
            updateBookCount();
            populateSubjectFilter();
            
        } catch (Exception e) {
            showError("Error loading books", e.getMessage());
        }
    }
    
    private void populateSubjectFilter() {
        subjectFilter.getItems().clear();
        subjectFilter.getItems().add("All");
        
        booksList.stream()
            .map(Book::getSubject)
            .distinct()
            .sorted()
            .forEach(subject -> subjectFilter.getItems().add(subject));
        
        subjectFilter.setValue("All");
    }
    
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        String subjectFilterValue = subjectFilter.getValue();
        String availabilityFilterValue = availabilityFilter.getValue();
        
        filteredBooks.setPredicate(book -> {
            // Search filter
            boolean matchesSearch = searchText.isEmpty() ||
                book.getTitle().toLowerCase().contains(searchText) ||
                book.getAuthor().toLowerCase().contains(searchText) ||
                book.getIsbn().toLowerCase().contains(searchText) ||
                book.getSubject().toLowerCase().contains(searchText);
            
            // Subject filter
            boolean matchesSubject = "All".equals(subjectFilterValue) ||
                book.getSubject().equals(subjectFilterValue);
            
            // Availability filter
            boolean matchesAvailability = "All".equals(availabilityFilterValue) ||
                ("Available".equals(availabilityFilterValue) && book.getAvailableCopiesCount() > 0) ||
                ("Partially Available".equals(availabilityFilterValue) && 
                 book.getAvailableCopiesCount() > 0 && book.getAvailableCopiesCount() < book.getTotalCopiesCount()) ||
                ("Unavailable".equals(availabilityFilterValue) && book.getAvailableCopiesCount() == 0);
            
            return matchesSearch && matchesSubject && matchesAvailability;
        });
        
        updateBookCount();
    }
    
    @FXML
    private void clearFilters() {
        searchField.clear();
        subjectFilter.setValue("All");
        availabilityFilter.setValue("All");
        handleSearch();
    }
    
    @FXML
    private void refreshBooks() {
        loadBooks();
    }
    
    @FXML
    private void showAddBookDialog() {
        isEditMode = false;
        clearDialog();
        bookDialog.setTitle("Add New Book");
        bookDialog.showAndWait().ifPresent(this::saveBook);
    }
    
    @FXML
    private void showEditBookDialog() {
        if (selectedBook == null) {
            showAlert("No Selection", "Please select a book to edit.");
            return;
        }
        showEditBookDialog(selectedBook);
    }
    
    private void showEditBookDialog(Book book) {
        isEditMode = true;
        populateDialog(book);
        bookDialog.setTitle("Edit Book");
        bookDialog.showAndWait().ifPresent(this::saveBook);
    }
    
    private void clearDialog() {
        dialogIsbn.clear();
        dialogTitle.clear();
        dialogAuthor.clear();
        dialogSubject.setValue(null);
        dialogDescription.clear();
        dialogCopies.getValueFactory().setValue(1);
        dialogIsbn.setDisable(false);
        dialogCopies.setDisable(false);
    }
    
    private void populateDialog(Book book) {
        dialogIsbn.setText(book.getIsbn());
        dialogTitle.setText(book.getTitle());
        dialogAuthor.setText(book.getAuthor());
        dialogSubject.setValue(book.getSubject());
        dialogDescription.setText(book.getDescription());
        dialogCopies.getValueFactory().setValue(book.getTotalCopies());
        dialogIsbn.setDisable(true); // Can't change ISBN in edit mode
        dialogCopies.setDisable(true); // Can't change copies in edit mode
    }
    
    private Book createBookFromDialog() {
        String isbn = dialogIsbn.getText().trim();
        String title = dialogTitle.getText().trim();
        String author = dialogAuthor.getText().trim();
        String subject = dialogSubject.getValue();
        String description = dialogDescription.getText().trim();
        int copies = dialogCopies.getValue();
        
        if (isbn.isEmpty() || title.isEmpty() || author.isEmpty() || subject == null) {
            showAlert("Validation Error", "ISBN, Title, Author, and Subject are required.");
            return null;
        }
        
        if (isEditMode) {
            selectedBook.setTitle(title);
            selectedBook.setAuthor(author);
            selectedBook.setSubject(subject);
            selectedBook.setDescription(description);
            return selectedBook;
        } else {
            Book book = new Book(isbn, title, author, subject, description);
            book.setInitialCopies(copies);
            return book;
        }
    }
    
    @FXML
    private void saveBook(Book book) {
        if (book == null) return;
        
        try {
            if (isEditMode) {
                bookDAO.update(book);
                showInfo("Success", "Book updated successfully.");
            } else {
                if (bookDAO.existsByIsbn(book.getIsbn())) {
                    showAlert("Error", "A book with this ISBN already exists.");
                    return;
                }
                bookDAO.save(book);
                showInfo("Success", "Book added successfully.");
            }
            
            loadBooks();
            bookDialog.close();
            
        } catch (Exception e) {
            showError("Error saving book", e.getMessage());
        }
    }
    
    @FXML
    private void cancelBookDialog() {
        bookDialog.close();
    }
    
    @FXML
    private void deleteBook() {
        if (selectedBook == null) {
            showAlert("No Selection", "Please select a book to delete.");
            return;
        }
        deleteBook(selectedBook);
    }
    
    private void deleteBook(Book book) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Book");
        alert.setContentText("Are you sure you want to delete book '" + book.getTitle() + "'?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Check if book has loaned copies
                if (book.getLoanedCopies() > 0) {
                    showAlert("Cannot Delete", 
                        "Cannot delete book with loaned copies. Please wait for all copies to be returned.");
                    return;
                }
                
                bookDAO.delete(book);
                showInfo("Success", "Book deleted successfully.");
                loadBooks();
                
            } catch (Exception e) {
                showError("Error deleting book", e.getMessage());
            }
        }
    }
    
    @FXML
    private void manageCopies() {
        if (selectedBook == null) {
            showAlert("No Selection", "Please select a book to manage copies.");
            return;
        }
        manageCopies(selectedBook);
    }
    
    private void manageCopies(Book book) {
        try {
            copyDialogTitle.setText(book.getTitle());
            loadCopies(book);
            updateCopyStats(book);
            copyDialog.showAndWait();
        } catch (Exception e) {
            showError("Error loading copies", e.getMessage());
        }
    }
    
    private void loadCopies(Book book) {
        try {
            List<BookCopy> copies = bookCopyDAO.findByBook(book);
            copiesList = FXCollections.observableArrayList(copies);
            copyTable.setItems(copiesList);
        } catch (Exception e) {
            showError("Error loading copies", e.getMessage());
        }
    }
    
    private void updateCopyStats(Book book) {
        totalCopiesLabel.setText(String.valueOf(book.getTotalCopiesCount()));
        availableCopiesLabel.setText(String.valueOf(book.getAvailableCopiesCount()));
        loanedCopiesLabel.setText(String.valueOf(book.getLoanedCopies()));
    }
    
    @FXML
    private void addCopy() {
        if (selectedBook == null) return;
        
        try {
            // Generate a unique bar code
            String barCode = "BC" + System.currentTimeMillis();
            BookCopy newCopy = new BookCopy(selectedBook, barCode);
            bookCopyDAO.save(newCopy);
            loadCopies(selectedBook);
            loadBooks(); // Refresh book stats
            updateCopyStats(selectedBook);
            showInfo("Success", "Copy added successfully.");
        } catch (Exception e) {
            showError("Error adding copy", e.getMessage());
        }
    }
    
    @FXML
    private void removeCopy() {
        BookCopy selectedCopy = copyTable.getSelectionModel().getSelectedItem();
        if (selectedCopy == null) {
            showAlert("No Selection", "Please select a copy to remove.");
            return;
        }
        removeCopy(selectedCopy);
    }
    
    private void removeCopy(BookCopy copy) {
        if (!copy.isAvailable()) {
            showAlert("Cannot Remove", "Cannot remove a copy that is currently loaned.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Remove");
        alert.setHeaderText("Remove Copy");
        alert.setContentText("Are you sure you want to remove this copy?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                bookCopyDAO.delete(copy);
                loadCopies(selectedBook);
                loadBooks(); // Refresh book stats
                updateCopyStats(selectedBook);
                showInfo("Success", "Copy removed successfully.");
            } catch (Exception e) {
                showError("Error removing copy", e.getMessage());
            }
        }
    }
    
    @FXML
    private void closeCopyDialog() {
        copyDialog.close();
    }
    
    @FXML
    private void viewBookLoans() {
        if (selectedBook == null) {
            showAlert("No Selection", "Please select a book to view loans.");
            return;
        }
        
        try {
            // Open loan management screen filtered for this book
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoanManagement.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the book filter
            // This will be implemented when we create the LoanManagementController
            
            Stage stage = new Stage();
            stage.setTitle("Loans for " + selectedBook.getTitle());
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();
            
        } catch (IOException e) {
            showError("Error opening loan management", e.getMessage());
        }
    }
    
    private void updateBookCount() {
        int total = booksList.size();
        int filtered = filteredBooks.size();
        bookCountLabel.setText(String.format("Showing: %d of %d", filtered, total));
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
} 