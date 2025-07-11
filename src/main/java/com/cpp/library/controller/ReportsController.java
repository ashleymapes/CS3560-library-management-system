package com.cpp.library.controller;

import com.cpp.library.dao.LoanDAO;
import com.cpp.library.dao.StudentDAO;
import com.cpp.library.dao.BookDAO;
import com.cpp.library.dao.BookCopyDAO;
import com.cpp.library.dao.impl.LoanDAOImpl;
import com.cpp.library.dao.impl.StudentDAOImpl;
import com.cpp.library.dao.impl.BookDAOImpl;
import com.cpp.library.dao.impl.BookCopyDAOImpl;
import com.cpp.library.model.Loan;
import com.cpp.library.model.Student;
import com.cpp.library.model.Book;
import com.cpp.library.model.BookCopy;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportsController {
    
    // Statistics Labels
    @FXML private Label totalStudentsLabel;
    @FXML private Label totalBooksLabel;
    @FXML private Label totalCopiesLabel;
    @FXML private Label activeLoansLabel;
    @FXML private Label overdueLoansLabel;
    @FXML private Label availableCopiesLabel;
    @FXML private Label totalLoansLabel;
    @FXML private Label returnRateLabel;
    
    // Overdue Loans Table
    @FXML private TableView<Loan> overdueTable;
    @FXML private TableColumn<Loan, String> overdueStudentColumn;
    @FXML private TableColumn<Loan, String> overdueBookColumn;
    @FXML private TableColumn<Loan, String> overdueCopyColumn;
    @FXML private TableColumn<Loan, String> overdueDueDateColumn;
    @FXML private TableColumn<Loan, String> overdueDaysLateColumn;
    @FXML private TableColumn<Loan, String> overdueContactColumn;
    
    // Popular Books Table
    @FXML private TableView<Book> popularBooksTable;
    @FXML private TableColumn<Book, String> popularRankColumn;
    @FXML private TableColumn<Book, String> popularTitleColumn;
    @FXML private TableColumn<Book, String> popularAuthorColumn;
    @FXML private TableColumn<Book, String> popularSubjectColumn;
    @FXML private TableColumn<Book, String> popularLoansColumn;
    @FXML private TableColumn<Book, String> popularCopiesColumn;
    @FXML private TableColumn<Book, String> popularAvailabilityColumn;
    @FXML private ComboBox<String> popularPeriodFilter;
    
    // Student Activity Table
    @FXML private TableView<Student> studentActivityTable;
    @FXML private TableColumn<Student, String> activityStudentColumn;
    @FXML private TableColumn<Student, String> activityBroncoIdColumn;
    @FXML private TableColumn<Student, String> activityDegreeColumn;
    @FXML private TableColumn<Student, String> activityTotalLoansColumn;
    @FXML private TableColumn<Student, String> activityActiveLoansColumn;
    @FXML private TableColumn<Student, String> activityOverdueColumn;
    @FXML private TableColumn<Student, String> activityLastActivityColumn;
    @FXML private ComboBox<String> activityPeriodFilter;
    
    // Subject Analysis Table
    @FXML private TableView<Map.Entry<String, Object>> subjectAnalysisTable;
    @FXML private TableColumn<Map.Entry<String, Object>, String> subjectNameColumn;
    @FXML private TableColumn<Map.Entry<String, Object>, String> subjectBooksColumn;
    @FXML private TableColumn<Map.Entry<String, Object>, String> subjectCopiesColumn;
    @FXML private TableColumn<Map.Entry<String, Object>, String> subjectLoansColumn;
    @FXML private TableColumn<Map.Entry<String, Object>, String> subjectAvailabilityColumn;
    @FXML private TableColumn<Map.Entry<String, Object>, String> subjectPopularityColumn;
    
    // Custom Reports
    @FXML private ComboBox<String> customReportType;
    @FXML private DatePicker customStartDate;
    @FXML private DatePicker customEndDate;
    @FXML private CheckBox customIncludeOverdue;
    @FXML private CheckBox customIncludeReturned;
    @FXML private CheckBox customIncludeActive;
    @FXML private TextArea customReportOutput;
    
    private final LoanDAO loanDAO = new LoanDAOImpl();
    private final StudentDAO studentDAO = new StudentDAOImpl();
    private final BookDAO bookDAO = new BookDAOImpl();
    private final BookCopyDAO bookCopyDAO = new BookCopyDAOImpl();
    
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    
    @FXML
    public void initialize() {
        try {
            System.out.println("ReportsController: initialize start");
            setupTables();
            setupFilters();
            loadStatistics();
            loadOverdueLoans();
            loadPopularBooks();
            loadStudentActivity();
            loadSubjectAnalysis();
            System.out.println("ReportsController: initialize end");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Initialization Error", e.getMessage());
        }
    }
    
    private void setupTables() {
        // Overdue loans table
        overdueStudentColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getStudent().getName()));
        overdueBookColumn.setCellValueFactory(data -> {
            BookCopy copy = data.getValue().getBookCopy();
            return new SimpleStringProperty(copy != null ? copy.getBookTitle() : "Unknown");
        });
        overdueCopyColumn.setCellValueFactory(data -> {
            BookCopy copy = data.getValue().getBookCopy();
            return new SimpleStringProperty(copy != null ? copy.getId() : "Unknown");
        });
        overdueDueDateColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDueDate().format(dateFormatter)));
        overdueDaysLateColumn.setCellValueFactory(data -> {
            long daysLate = java.time.temporal.ChronoUnit.DAYS.between(
                data.getValue().getDueDate(), LocalDate.now());
            return new SimpleStringProperty(String.valueOf(daysLate));
        });
        overdueContactColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getStudent().getAddress()));
        
        // Popular books table
        popularTitleColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getTitle()));
        popularAuthorColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getAuthor()));
        popularSubjectColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getSubject()));
        popularCopiesColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().getTotalCopiesCount())));
        popularAvailabilityColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().getAvailableCopiesCount())));
        
        // Student activity table
        activityStudentColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getName()));
        activityBroncoIdColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getBroncoId()));
        activityDegreeColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDegree()));
        activityActiveLoansColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().getActiveLoansCount())));
        activityOverdueColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().hasOverdueLoans() ? "Yes" : "No"));
    }
    
    private void setupFilters() {
        // Popular books period filter
        popularPeriodFilter.getItems().addAll("All Time", "Last 30 Days", "Last 90 Days", "Last 6 Months", "Last Year");
        popularPeriodFilter.setValue("All Time");
        
        // Student activity period filter
        activityPeriodFilter.getItems().addAll("All Time", "Last 30 Days", "Last 90 Days", "Last 6 Months", "Last Year");
        activityPeriodFilter.setValue("All Time");
        
        // Custom report types
        customReportType.getItems().addAll(
            "Loan Summary", "Student Activity", "Book Circulation", "Overdue Analysis", "Return Analysis"
        );
        
        // Set default dates
        customStartDate.setValue(LocalDate.now().minusDays(30));
        customEndDate.setValue(LocalDate.now());
    }
    
    @FXML
    private void refreshAllReports() {
        loadStatistics();
        loadOverdueLoans();
        loadPopularBooks();
        loadStudentActivity();
        loadSubjectAnalysis();
        showInfo("Success", "All reports refreshed successfully.");
    }
    
    private void loadStatistics() {
        try {
            List<Student> students = studentDAO.findAll();
            List<Book> books = bookDAO.findAll();
            List<BookCopy> copies = bookCopyDAO.findAll();
            List<Loan> loans = loanDAO.findAll();
            
            int totalStudents = students.size();
            int totalBooks = books.size();
            int totalCopies = copies.size();
            int activeLoans = (int) loans.stream().filter(loan -> !loan.isReturned()).count();
            int overdueLoans = (int) loans.stream().filter(Loan::isOverdue).count();
            int availableCopies = (int) copies.stream().filter(BookCopy::isAvailable).count();
            int totalLoans = loans.size();
            int returnedLoans = (int) loans.stream().filter(Loan::isReturned).count();
            double returnRate = totalLoans > 0 ? (double) returnedLoans / totalLoans * 100 : 0;
            
            totalStudentsLabel.setText(String.valueOf(totalStudents));
            totalBooksLabel.setText(String.valueOf(totalBooks));
            totalCopiesLabel.setText(String.valueOf(totalCopies));
            activeLoansLabel.setText(String.valueOf(activeLoans));
            overdueLoansLabel.setText(String.valueOf(overdueLoans));
            availableCopiesLabel.setText(String.valueOf(availableCopies));
            totalLoansLabel.setText(String.valueOf(totalLoans));
            returnRateLabel.setText(String.format("%.1f%%", returnRate));
            
        } catch (Exception e) {
            showError("Error loading statistics", e.getMessage());
        }
    }
    
    @FXML
    private void loadOverdueLoans() {
        try {
            List<Loan> overdueLoans = loanDAO.findOverdueLoans();
            ObservableList<Loan> overdueList = FXCollections.observableArrayList(overdueLoans);
            overdueTable.setItems(overdueList);
        } catch (Exception e) {
            showError("Error loading overdue loans", e.getMessage());
        }
    }
    
    @FXML
    private void loadPopularBooks() {
        try {
            String period = popularPeriodFilter.getValue();
            LocalDate startDate = getStartDateForPeriod(period);
            
            List<Book> books = bookDAO.findAll();
            List<Loan> loans = startDate != null ? 
                loanDAO.findLoansAfterDate(startDate) : loanDAO.findAll();
            
            // Calculate loan counts for each book
            Map<Book, Long> bookLoanCounts = loans.stream()
                .filter(loan -> loan.getBookCopy() != null)
                .collect(Collectors.groupingBy(
                    loan -> loan.getBookCopy().getBook(),
                    Collectors.counting()
                ));
            
            // Sort books by loan count
            List<Book> popularBooks = books.stream()
                .sorted((b1, b2) -> {
                    long count1 = bookLoanCounts.getOrDefault(b1, 0L);
                    long count2 = bookLoanCounts.getOrDefault(b2, 0L);
                    return Long.compare(count2, count1); // Descending order
                })
                .collect(Collectors.toList());
            
            ObservableList<Book> popularList = FXCollections.observableArrayList(popularBooks);
            popularBooksTable.setItems(popularList);
            
            // Set loan counts in the table
            popularLoansColumn.setCellValueFactory(data -> {
                Book book = data.getValue();
                long count = bookLoanCounts.getOrDefault(book, 0L);
                return new SimpleStringProperty(String.valueOf(count));
            });
            
            // Set rank
            popularRankColumn.setCellValueFactory(data -> {
                int rank = popularBooks.indexOf(data.getValue()) + 1;
                return new SimpleStringProperty(String.valueOf(rank));
            });
            
        } catch (Exception e) {
            showError("Error loading popular books", e.getMessage());
        }
    }
    
    @FXML
    private void loadStudentActivity() {
        try {
            String period = activityPeriodFilter.getValue();
            LocalDate startDate = getStartDateForPeriod(period);
            
            List<Student> students = studentDAO.findAll();
            List<Loan> loans = startDate != null ? 
                loanDAO.findLoansAfterDate(startDate) : loanDAO.findAll();
            
            // Calculate total loans for each student
            Map<Student, Long> studentLoanCounts = loans.stream()
                .collect(Collectors.groupingBy(Loan::getStudent, Collectors.counting()));
            
            // Sort students by loan count
            List<Student> activeStudents = students.stream()
                .sorted((s1, s2) -> {
                    long count1 = studentLoanCounts.getOrDefault(s1, 0L);
                    long count2 = studentLoanCounts.getOrDefault(s2, 0L);
                    return Long.compare(count2, count1); // Descending order
                })
                .collect(Collectors.toList());
            
            ObservableList<Student> activityList = FXCollections.observableArrayList(activeStudents);
            studentActivityTable.setItems(activityList);
            
            // Set total loans count
            activityTotalLoansColumn.setCellValueFactory(data -> {
                Student student = data.getValue();
                long count = studentLoanCounts.getOrDefault(student, 0L);
                return new SimpleStringProperty(String.valueOf(count));
            });
            
            // Set last activity
            activityLastActivityColumn.setCellValueFactory(data -> {
                Student student = data.getValue();
                LocalDate lastActivity = loans.stream()
                    .filter(loan -> loan.getStudent().equals(student))
                    .map(Loan::getCheckoutDate)
                    .max(LocalDate::compareTo)
                    .orElse(null);
                return new SimpleStringProperty(lastActivity != null ? 
                    lastActivity.format(dateFormatter) : "Never");
            });
            
        } catch (Exception e) {
            showError("Error loading student activity", e.getMessage());
        }
    }
    
    @FXML
    private void loadSubjectAnalysis() {
        try {
            List<Book> books = bookDAO.findAll();
            List<Loan> loans = loanDAO.findAll();
            
            // Group books by subject
            Map<String, List<Book>> booksBySubject = books.stream()
                .collect(Collectors.groupingBy(Book::getSubject));
            
            // Calculate statistics for each subject
            List<Map.Entry<String, Object>> subjectStats = booksBySubject.entrySet().stream()
                .map(entry -> {
                    String subject = entry.getKey();
                    List<Book> subjectBooks = entry.getValue();
                    
                    int bookCount = subjectBooks.size();
                    int copyCount = subjectBooks.stream().mapToInt(Book::getTotalCopiesCount).sum();
                    int availableCount = subjectBooks.stream().mapToInt(Book::getAvailableCopiesCount).sum();
                    
                    // Calculate loan count for this subject
                    long loanCount = loans.stream()
                        .filter(loan -> loan.getBookCopy() != null && 
                                loan.getBookCopy().getBook() != null &&
                                subject.equals(loan.getBookCopy().getBook().getSubject()))
                        .count();
                    
                    // Calculate popularity (loans per copy)
                    double popularityScore = copyCount > 0 ? (double) loanCount / copyCount : 0;
                    
                    // Create a simple object to hold the data
                    Object stats = new Object() {
                        public final int books = bookCount;
                        public final int copies = copyCount;
                        public final long loans = loanCount;
                        public final int available = availableCount;
                        public final double popularity = popularityScore;
                    };
                    
                    return Map.entry(subject, stats);
                })
                .sorted((e1, e2) -> {
                    Object stats1 = e1.getValue();
                    Object stats2 = e2.getValue();
                    // Sort by popularity
                    return Double.compare(getPopularity(stats2), getPopularity(stats1));
                })
                .collect(Collectors.toList());
            
            ObservableList<Map.Entry<String, Object>> subjectList = FXCollections.observableArrayList(subjectStats);
            subjectAnalysisTable.setItems(subjectList);
            
            // Set up cell value factories
            subjectNameColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getKey()));
            subjectBooksColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(String.valueOf(getBooks(data.getValue().getValue()))));
            subjectCopiesColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(String.valueOf(getCopies(data.getValue().getValue()))));
            subjectLoansColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(String.valueOf(getLoans(data.getValue().getValue()))));
            subjectAvailabilityColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(String.valueOf(getAvailable(data.getValue().getValue()))));
            subjectPopularityColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(String.format("%.2f", getPopularity(data.getValue().getValue()))));
            
        } catch (Exception e) {
            showError("Error loading subject analysis", e.getMessage());
        }
    }
    
    // Helper methods for subject analysis
    private int getBooks(Object stats) {
        try {
            return (int) stats.getClass().getField("books").get(stats);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private int getCopies(Object stats) {
        try {
            return (int) stats.getClass().getField("copies").get(stats);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private long getLoans(Object stats) {
        try {
            return (long) stats.getClass().getField("loans").get(stats);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private int getAvailable(Object stats) {
        try {
            return (int) stats.getClass().getField("available").get(stats);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private double getPopularity(Object stats) {
        try {
            return (double) stats.getClass().getField("popularity").get(stats);
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    private LocalDate getStartDateForPeriod(String period) {
        if (period == null) return null;
        
        switch (period) {
            case "Last 30 Days":
                return LocalDate.now().minusDays(30);
            case "Last 90 Days":
                return LocalDate.now().minusDays(90);
            case "Last 6 Months":
                return LocalDate.now().minusMonths(6);
            case "Last Year":
                return LocalDate.now().minusYears(1);
            default:
                return null; // All Time
        }
    }
    
    @FXML
    private void generateCustomReport() {
        try {
            String reportType = customReportType.getValue();
            LocalDate startDate = customStartDate.getValue();
            LocalDate endDate = customEndDate.getValue();
            
            if (reportType == null) {
                showAlert("Validation Error", "Please select a report type.");
                return;
            }
            
            if (startDate == null || endDate == null) {
                showAlert("Validation Error", "Please select start and end dates.");
                return;
            }
            
            if (startDate.isAfter(endDate)) {
                showAlert("Validation Error", "Start date cannot be after end date.");
                return;
            }
            
            StringBuilder report = new StringBuilder();
            report.append("Custom Report: ").append(reportType).append("\n");
            report.append("Date Range: ").append(startDate.format(dateFormatter))
                  .append(" to ").append(endDate.format(dateFormatter)).append("\n");
            report.append("Generated: ").append(LocalDate.now().format(dateFormatter)).append("\n\n");
            
            switch (reportType) {
                case "Loan Summary":
                    generateLoanSummaryReport(report, startDate, endDate);
                    break;
                case "Student Activity":
                    generateStudentActivityReport(report, startDate, endDate);
                    break;
                case "Book Circulation":
                    generateBookCirculationReport(report, startDate, endDate);
                    break;
                case "Overdue Analysis":
                    generateOverdueAnalysisReport(report, startDate, endDate);
                    break;
                case "Return Analysis":
                    generateReturnAnalysisReport(report, startDate, endDate);
                    break;
            }
            
            customReportOutput.setText(report.toString());
            
        } catch (Exception e) {
            showError("Error generating custom report", e.getMessage());
        }
    }
    
    private void generateLoanSummaryReport(StringBuilder report, LocalDate startDate, LocalDate endDate) {
        try {
            List<Loan> loans = loanDAO.findLoansBetweenDates(startDate, endDate);
            
            report.append("LOAN SUMMARY REPORT\n");
            report.append("===================\n\n");
            
            report.append("Total Loans: ").append(loans.size()).append("\n");
            report.append("Active Loans: ").append(loans.stream().filter(loan -> !loan.isReturned()).count()).append("\n");
            report.append("Returned Loans: ").append(loans.stream().filter(Loan::isReturned).count()).append("\n");
            report.append("Overdue Loans: ").append(loans.stream().filter(Loan::isOverdue).count()).append("\n\n");
            
            // Top borrowers
            Map<Student, Long> borrowerCounts = loans.stream()
                .collect(Collectors.groupingBy(Loan::getStudent, Collectors.counting()));
            
            report.append("TOP BORROWERS:\n");
            borrowerCounts.entrySet().stream()
                .sorted(Map.Entry.<Student, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    report.append("- ").append(entry.getKey().getName())
                          .append(" (").append(entry.getKey().getBroncoId()).append("): ")
                          .append(entry.getValue()).append(" loans\n");
                });
            
        } catch (Exception e) {
            report.append("Error generating loan summary: ").append(e.getMessage()).append("\n");
        }
    }
    
    private void generateStudentActivityReport(StringBuilder report, LocalDate startDate, LocalDate endDate) {
        try {
            List<Student> students = studentDAO.findAll();
            List<Loan> loans = loanDAO.findLoansBetweenDates(startDate, endDate);
            
            report.append("STUDENT ACTIVITY REPORT\n");
            report.append("=======================\n\n");
            
            report.append("Active Students: ").append(students.size()).append("\n");
            report.append("Students with Loans: ").append(
                loans.stream().map(Loan::getStudent).distinct().count()).append("\n\n");
            
            // Student activity breakdown
            Map<Student, Long> studentActivity = loans.stream()
                .collect(Collectors.groupingBy(Loan::getStudent, Collectors.counting()));
            
            report.append("STUDENT ACTIVITY BREAKDOWN:\n");
            studentActivity.entrySet().stream()
                .sorted(Map.Entry.<Student, Long>comparingByValue().reversed())
                .forEach(entry -> {
                    Student student = entry.getKey();
                    long loanCount = entry.getValue();
                    report.append("- ").append(student.getName())
                          .append(" (").append(student.getBroncoId()).append("): ")
                          .append(loanCount).append(" loans\n");
                });
            
        } catch (Exception e) {
            report.append("Error generating student activity report: ").append(e.getMessage()).append("\n");
        }
    }
    
    private void generateBookCirculationReport(StringBuilder report, LocalDate startDate, LocalDate endDate) {
        try {
            List<Book> books = bookDAO.findAll();
            List<Loan> loans = loanDAO.findLoansBetweenDates(startDate, endDate);
            
            report.append("BOOK CIRCULATION REPORT\n");
            report.append("=======================\n\n");
            
            report.append("Total Books: ").append(books.size()).append("\n");
            report.append("Books with Loans: ").append(
                loans.stream().map(loan -> loan.getBookCopy().getBook()).distinct().count()).append("\n\n");
            
            // Most circulated books
            Map<Book, Long> bookCirculation = loans.stream()
                .collect(Collectors.groupingBy(
                    loan -> loan.getBookCopy().getBook(), Collectors.counting()));
            
            report.append("MOST CIRCULATED BOOKS:\n");
            bookCirculation.entrySet().stream()
                .sorted(Map.Entry.<Book, Long>comparingByValue().reversed())
                .limit(15)
                .forEach(entry -> {
                    Book book = entry.getKey();
                    long circulation = entry.getValue();
                    report.append("- ").append(book.getTitle())
                          .append(" by ").append(book.getAuthor())
                          .append(" (").append(book.getSubject()).append("): ")
                          .append(circulation).append(" loans\n");
                });
            
        } catch (Exception e) {
            report.append("Error generating book circulation report: ").append(e.getMessage()).append("\n");
        }
    }
    
    private void generateOverdueAnalysisReport(StringBuilder report, LocalDate startDate, LocalDate endDate) {
        try {
            List<Loan> overdueLoans = loanDAO.findOverdueLoans();
            
            report.append("OVERDUE ANALYSIS REPORT\n");
            report.append("=======================\n\n");
            
            report.append("Total Overdue Loans: ").append(overdueLoans.size()).append("\n\n");
            
            // Overdue by days
            Map<Long, Long> overdueByDays = overdueLoans.stream()
                .collect(Collectors.groupingBy(
                    loan -> java.time.temporal.ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now()),
                    Collectors.counting()));
            
            report.append("OVERDUE BY DAYS:\n");
            overdueByDays.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    report.append("- ").append(entry.getKey()).append(" days: ")
                          .append(entry.getValue()).append(" loans\n");
                });
            
        } catch (Exception e) {
            report.append("Error generating overdue analysis report: ").append(e.getMessage()).append("\n");
        }
    }
    
    private void generateReturnAnalysisReport(StringBuilder report, LocalDate startDate, LocalDate endDate) {
        try {
            List<Loan> returnedLoans = loanDAO.findReturnedLoansBetweenDates(startDate, endDate);
            
            report.append("RETURN ANALYSIS REPORT\n");
            report.append("======================\n\n");
            
            report.append("Total Returns: ").append(returnedLoans.size()).append("\n\n");
            
            // Return timing analysis
            long onTime = returnedLoans.stream()
                .filter(loan -> loan.getReturnDate().isBefore(loan.getDueDate()) || 
                               loan.getReturnDate().isEqual(loan.getDueDate()))
                .count();
            long late = returnedLoans.size() - onTime;
            
            report.append("Return Timing:\n");
            report.append("- On Time: ").append(onTime).append(" (").append(
                returnedLoans.size() > 0 ? (onTime * 100 / returnedLoans.size()) : 0).append("%)\n");
            report.append("- Late: ").append(late).append(" (").append(
                returnedLoans.size() > 0 ? (late * 100 / returnedLoans.size()) : 0).append("%)\n");
            
        } catch (Exception e) {
            report.append("Error generating return analysis report: ").append(e.getMessage()).append("\n");
        }
    }
    
    // Export methods
    @FXML
    private void exportOverdueReport() {
        exportTableToCSV(overdueTable, "overdue_loans_report");
    }
    
    @FXML
    private void exportPopularBooksReport() {
        exportTableToCSV(popularBooksTable, "popular_books_report");
    }
    
    @FXML
    private void exportStudentActivityReport() {
        exportTableToCSV(studentActivityTable, "student_activity_report");
    }
    
    @FXML
    private void exportSubjectAnalysisReport() {
        exportTableToCSV(subjectAnalysisTable, "subject_analysis_report");
    }
    
    @FXML
    private void exportCustomReport() {
        String content = customReportOutput.getText();
        if (content.isEmpty()) {
            showAlert("No Report", "Please generate a report first.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Custom Report");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("custom_report.txt");
        
        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
                showInfo("Success", "Report exported successfully to " + file.getName());
            } catch (IOException e) {
                showError("Export Error", "Failed to export report: " + e.getMessage());
            }
        }
    }
    
    private <T> void exportTableToCSV(TableView<T> table, String filename) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Report");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName(filename + ".csv");
        
        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write headers
                StringBuilder headers = new StringBuilder();
                for (TableColumn<T, ?> column : table.getColumns()) {
                    headers.append(column.getText()).append(",");
                }
                writer.write(headers.toString().replaceAll(",$", "\n"));
                
                // Write data
                for (T item : table.getItems()) {
                    StringBuilder row = new StringBuilder();
                    for (TableColumn<T, ?> column : table.getColumns()) {
                        Object cellValue = column.getCellData(item);
                        row.append(cellValue != null ? cellValue.toString() : "").append(",");
                    }
                    writer.write(row.toString().replaceAll(",$", "\n"));
                }
                
                showInfo("Success", "Report exported successfully to " + file.getName());
            } catch (IOException e) {
                showError("Export Error", "Failed to export report: " + e.getMessage());
            }
        }
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