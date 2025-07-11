package com.cpp.library.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "book_copies")
public class BookCopy {
    
    public enum Status {
        AVAILABLE("Available"),
        BORROWED("Borrowed");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    @Id
    @Column(name = "bar_code", nullable = false, unique = true)
    private String barCode;
    
    @Column(name = "physical_location", nullable = false)
    private String physicalLocation;
    
    @Column(name = "condition")
    private String condition = "Good";
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.AVAILABLE;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_isbn", nullable = false)
    private Book book;
    
    // Constructors
    public BookCopy() {}
    
    public BookCopy(String barCode, String physicalLocation, Book book) {
        this.barCode = barCode;
        this.physicalLocation = physicalLocation;
        this.book = book;
        this.status = Status.AVAILABLE;
    }
    
    public BookCopy(String barCode, String physicalLocation, Status status, Book book) {
        this.barCode = barCode;
        this.physicalLocation = physicalLocation;
        this.status = status;
        this.book = book;
    }
    
    public BookCopy(String barCode, String physicalLocation, String condition, Book book) {
        this.barCode = barCode;
        this.physicalLocation = physicalLocation;
        this.condition = condition;
        this.book = book;
        this.status = Status.AVAILABLE;
    }
    
    public BookCopy(Book book, String barCode) {
        this.barCode = barCode;
        this.book = book;
        this.physicalLocation = "Main Library";
        this.condition = "Good";
        this.status = Status.AVAILABLE;
    }
    
    // Getters and Setters
    public String getBarCode() {
        return barCode;
    }
    
    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }
    
    public String getPhysicalLocation() {
        return physicalLocation;
    }
    
    public void setPhysicalLocation(String physicalLocation) {
        this.physicalLocation = physicalLocation;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public Book getBook() {
        return book;
    }
    
    public void setBook(Book book) {
        this.book = book;
    }
    
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
    
    // Helper methods
    public boolean isAvailable() {
        return status == Status.AVAILABLE;
    }
    
    public boolean isBorrowed() {
        return status == Status.BORROWED;
    }
    
    public void markAsBorrowed() {
        this.status = Status.BORROWED;
    }
    
    public void markAsAvailable() {
        this.status = Status.AVAILABLE;
    }
    
    // Get book information
    public String getBookTitle() {
        return book != null ? book.getTitle() : "Unknown";
    }
    
    public String getBookIsbn() {
        return book != null ? book.getIsbn() : "Unknown";
    }
    
    public String getBookAuthors() {
        return book != null ? book.getAuthors() : "Unknown";
    }
    
    // Get ID (alias for bar code)
    public String getId() {
        return barCode;
    }
    
    @Override
    public String toString() {
        return "BookCopy{" +
                "barCode='" + barCode + '\'' +
                ", physicalLocation='" + physicalLocation + '\'' +
                ", status=" + status +
                ", bookTitle='" + getBookTitle() + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookCopy bookCopy = (BookCopy) o;
        return Objects.equals(barCode, bookCopy.barCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(barCode);
    }
} 