package com.cpp.library.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "loans")
public class Loan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_id")
    private Long loanId;
    
    @Column(name = "borrowing_date", nullable = false)
    private LocalDate borrowingDate;
    
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    
    @Column(name = "return_date")
    private LocalDate returnDate;
    
    @Column(name = "notes")
    private String notes;
    
    @Column(name = "return_notes")
    private String returnNotes;
    
    @Column(name = "extension_reason")
    private String extensionReason;
    
    @Column(name = "status")
    private String status;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_bronco_id", nullable = false)
    private Student student;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "loan_book_copies",
        joinColumns = @JoinColumn(name = "loan_id"),
        inverseJoinColumns = @JoinColumn(name = "book_copy_bar_code")
    )
    private List<BookCopy> bookCopies = new ArrayList<>();
    
    // Constructors
    public Loan() {}
    
    public Loan(Student student, LocalDate borrowingDate, LocalDate dueDate) {
        this.student = student;
        this.borrowingDate = borrowingDate;
        this.dueDate = dueDate;
    }
    
    public Loan(Student student, BookCopy bookCopy, LocalDate borrowingDate, LocalDate dueDate, String notes) {
        this.student = student;
        this.borrowingDate = borrowingDate;
        this.dueDate = dueDate;
        this.notes = notes;
        this.bookCopies.add(bookCopy);
        bookCopy.markAsBorrowed();
    }
    
    // Getters and Setters
    public Long getLoanId() {
        return loanId;
    }
    
    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }
    
    public LocalDate getBorrowingDate() {
        return borrowingDate;
    }
    
    public void setBorrowingDate(LocalDate borrowingDate) {
        this.borrowingDate = borrowingDate;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public LocalDate getReturnDate() {
        return returnDate;
    }
    
    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getReturnNotes() {
        return returnNotes;
    }
    
    public void setReturnNotes(String returnNotes) {
        this.returnNotes = returnNotes;
    }
    
    public String getExtensionReason() {
        return extensionReason;
    }
    
    public void setExtensionReason(String extensionReason) {
        this.extensionReason = extensionReason;
    }
    
    public String getStatus() {
        if (status != null && !status.isEmpty()) {
            return status;
        }
        if (isReturned()) {
            return "Returned";
        } else if (isOverdue()) {
            return "Overdue";
        } else {
            return "Active";
        }
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Student getStudent() {
        return student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
    }
    
    public List<BookCopy> getBookCopies() {
        return bookCopies;
    }
    
    public void setBookCopies(List<BookCopy> bookCopies) {
        this.bookCopies = bookCopies;
    }
    
    // Helper methods
    public void addBookCopy(BookCopy bookCopy) {
        bookCopies.add(bookCopy);
        bookCopy.markAsBorrowed();
    }
    
    public void removeBookCopy(BookCopy bookCopy) {
        bookCopies.remove(bookCopy);
        bookCopy.markAsAvailable();
    }
    
    public void returnAllBooks() {
        for (BookCopy bookCopy : bookCopies) {
            bookCopy.markAsAvailable();
        }
        this.returnDate = LocalDate.now();
    }
    
    // Check if loan is overdue
    public boolean isOverdue() {
        return !isReturned() && LocalDate.now().isAfter(dueDate);
    }
    
    // Check if loan is returned
    public boolean isReturned() {
        return returnDate != null;
    }
    
    // Get days overdue
    public long getDaysOverdue() {
        if (isReturned() || !isOverdue()) {
            return 0;
        }
        return LocalDate.now().toEpochDay() - dueDate.toEpochDay();
    }
    
    // Get loan duration in days
    public long getLoanDurationDays() {
        LocalDate endDate = isReturned() ? returnDate : LocalDate.now();
        return endDate.toEpochDay() - borrowingDate.toEpochDay();
    }
    
    // Get student name
    public String getStudentName() {
        return student != null ? student.getName() : "Unknown";
    }
    
    public String getStudentBroncoId() {
        return student != null ? student.getBroncoId() : "Unknown";
    }
    
    // Get book titles
    public List<String> getBookTitles() {
        return bookCopies.stream()
                .map(BookCopy::getBookTitle)
                .toList();
    }
    
    // Get first book copy (for compatibility with controllers)
    public BookCopy getBookCopy() {
        return bookCopies.isEmpty() ? null : bookCopies.get(0);
    }
    
    // Get checkout date (alias for borrowing date)
    public LocalDate getCheckoutDate() {
        return borrowingDate;
    }
    
    // Get ID (alias for loan ID)
    public Long getId() {
        return loanId;
    }
    
    // Get total number of books in loan
    public int getBookCount() {
        return bookCopies.size();
    }
    
    // Get book titles as string (for display)
    public String getBookTitlesString() {
        return String.join(", ", getBookTitles());
    }
    
    // Get fine amount (if overdue)
    public double getFineAmount() {
        if (!isOverdue()) {
            return 0.0;
        }
        // $1 per day overdue
        return getDaysOverdue() * 1.0;
    }
    
    @Override
    public String toString() {
        return "Loan{" +
                "loanId=" + loanId +
                ", studentName='" + getStudentName() + '\'' +
                ", borrowingDate=" + borrowingDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", bookCount=" + getBookCount() +
                ", isOverdue=" + isOverdue() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loan loan = (Loan) o;
        return Objects.equals(loanId, loan.loanId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(loanId);
    }
} 