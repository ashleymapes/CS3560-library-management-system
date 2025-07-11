package com.cpp.library.dao;

import com.cpp.library.model.BookCopy;
import com.cpp.library.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookCopyDAO {
    
    // Basic CRUD operations
    void save(BookCopy bookCopy);
    void update(BookCopy bookCopy);
    void delete(BookCopy bookCopy);
    Optional<BookCopy> findById(Long id);
    List<BookCopy> findAll();
    
    // Business-specific queries
    List<BookCopy> findByBook(Book book);
    List<BookCopy> findAvailableByBook(Book book);
    List<BookCopy> findLoanedByBook(Book book);
    List<BookCopy> findAvailableCopies();
    List<BookCopy> findLoanedCopies();
    
    // Search and filter
    List<BookCopy> findByCondition(String condition);
    List<BookCopy> findOverdueCopies();
    
    // Validation queries
    boolean existsById(Long id);
    long countAvailableCopies(Book book);
    long countLoanedCopies(Book book);
    long countTotalCopies(Book book);
    
    // Advanced queries
    List<BookCopy> findCopiesNeedingMaintenance();
    List<BookCopy> findCopiesByCondition(String condition);
} 