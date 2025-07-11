package com.cpp.library.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jakarta.persistence.Transient;

@Entity
@Table(name = "books")
public class Book {
    
    @Id
    @Column(name = "isbn", nullable = false, unique = true)
    private String isbn;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "authors", nullable = false)
    private String authors;
    
    @Column(name = "subject")
    private String subject;
    
    @Column(name = "pages")
    private Integer pages;
    
    @Column(name = "publisher")
    private String publisher;
    
    @Column(name = "publication_date")
    private LocalDate publicationDate;
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookCopy> copies = new ArrayList<>();
    
    @Transient
    private int initialCopies = 0;

    public int getInitialCopies() {
        return initialCopies;
    }
    public void setInitialCopies(int initialCopies) {
        this.initialCopies = initialCopies;
    }
    
    // Constructors
    public Book() {}
    
    public Book(String isbn, String title, String authors) {
        this.isbn = isbn;
        this.title = title;
        this.authors = authors;
    }
    
    public Book(String isbn, String title, String authors, String subject, String description, int initialCopies) {
        this.isbn = isbn;
        this.title = title;
        this.authors = authors;
        this.subject = subject;
        this.description = description;
        // Removed BookCopy creation from here. Copies will be created in the DAO.
    }
    
    public Book(String isbn, String title, String author, String subject, String description) {
        this.isbn = isbn;
        this.title = title;
        this.authors = author;
        this.subject = subject;
        this.description = description;
    }
    
    public Book(String isbn, String title, String description, String authors, 
                Integer pages, String publisher, LocalDate publicationDate) {
        this.isbn = isbn;
        this.title = title;
        this.description = description;
        this.authors = authors;
        this.pages = pages;
        this.publisher = publisher;
        this.publicationDate = publicationDate;
    }
    
    // Getters and Setters
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getAuthors() {
        return authors;
    }
    
    public void setAuthors(String authors) {
        this.authors = authors;
    }
    
    // Alias for getAuthors() to match controller expectations
    public String getAuthor() {
        return authors;
    }
    
    public void setAuthor(String author) {
        this.authors = author;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public Integer getPages() {
        return pages;
    }
    
    public void setPages(Integer pages) {
        this.pages = pages;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public LocalDate getPublicationDate() {
        return publicationDate;
    }
    
    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }
    
    public List<BookCopy> getCopies() {
        return copies;
    }
    
    public void setCopies(List<BookCopy> copies) {
        this.copies = copies;
    }
    
    // Helper methods
    public void addCopy(BookCopy copy) {
        copies.add(copy);
        copy.setBook(this);
    }
    
    public void removeCopy(BookCopy copy) {
        copies.remove(copy);
        copy.setBook(null);
    }
    
    // Get available copies
    public List<BookCopy> getAvailableCopies() {
        return copies.stream()
                .filter(BookCopy::isAvailable)
                .toList();
    }
    
    // Get borrowed copies
    public List<BookCopy> getBorrowedCopies() {
        return copies.stream()
                .filter(BookCopy::isBorrowed)
                .toList();
    }
    
    // Check if book has available copies
    public boolean hasAvailableCopies() {
        return copies.stream().anyMatch(BookCopy::isAvailable);
    }
    
    // Get total copies count
    public int getTotalCopiesCount() {
        return copies.size();
    }
    
    // Alias for getTotalCopiesCount() to match controller expectations
    public int getTotalCopies() {
        return getTotalCopiesCount();
    }
    
    // Get available copies count
    public int getAvailableCopiesCount() {
        return (int) copies.stream().filter(BookCopy::isAvailable).count();
    }
    
    // Get loaned copies count
    public int getLoanedCopies() {
        return (int) copies.stream().filter(copy -> !copy.isAvailable()).count();
    }
    
    // Alias for getAvailableCopiesCount() to match controller expectations
    // public int getAvailableCopies() {
    //     return getAvailableCopiesCount();
    // }
    
    @Override
    public String toString() {
        return "Book{" +
                "isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", authors='" + authors + '\'' +
                ", subject='" + subject + '\'' +
                ", publisher='" + publisher + '\'' +
                ", publicationDate=" + publicationDate +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(isbn, book.isbn);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }
} 