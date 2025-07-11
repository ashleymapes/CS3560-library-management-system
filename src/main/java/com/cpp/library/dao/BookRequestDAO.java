package com.cpp.library.dao;

import com.cpp.library.model.Book;
import com.cpp.library.model.BookRequest;
import java.util.List;

public interface BookRequestDAO {
    void save(BookRequest request);
    void update(BookRequest request);
    void delete(BookRequest request);
    BookRequest findById(Long id);
    List<BookRequest> findAll();
    List<BookRequest> findPendingRequestsForBook(Book book);
} 