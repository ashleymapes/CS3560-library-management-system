package com.cpp.library.dao;

import com.cpp.library.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookDAO {
    
    // Basic CRUD operations
    void save(Book book);
    void update(Book book);
    void delete(Book book);
    Optional<Book> findById(String isbn);
    List<Book> findAll();
    
    // Business-specific queries
    List<Book> findBooksWithAvailableCopies();
    List<Book> findAvailableBooks(); // Books that have at least one available copy
    List<Book> findBooksByTitleContaining(String title);
    List<Book> findBooksByAuthorContaining(String author);
    List<Book> findBooksByPublisher(String publisher);
    Optional<Book> findByIsbn(String isbn);
    
    // Search and filter
    List<Book> searchBooks(String searchTerm);
    List<Book> findBooksByYear(int year);
    
    // Validation queries
    boolean existsByIsbn(String isbn);
    long countAvailableCopies(String isbn);
    long countTotalCopies(String isbn);
    
    // Advanced queries
    List<Book> findMostBorrowedBooks(int limit);
    List<Book> findBooksWithNoAvailableCopies();
} 