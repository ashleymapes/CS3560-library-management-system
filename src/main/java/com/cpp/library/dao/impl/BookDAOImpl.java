package com.cpp.library.dao.impl;

import com.cpp.library.dao.BookDAO;
import com.cpp.library.model.Book;
import com.cpp.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.cpp.library.model.BookCopy;

public class BookDAOImpl implements BookDAO {
    
    @Override
    public void save(Book book) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            // If the book has no copies, generate them based on the intended initial count
            if (book.getCopies() == null || book.getCopies().isEmpty()) {
                int initialCopies = book.getInitialCopies();
                for (int i = 0; i < initialCopies; i++) {
                    String barCode = book.getIsbn() + "-C" + (i + 1) + "-" + System.currentTimeMillis();
                    BookCopy copy = new BookCopy();
                    copy.setBarCode(barCode);
                    copy.setBook(book);
                    copy.setPhysicalLocation("Main Library");
                    copy.setCondition("Good");
                    copy.setStatus(BookCopy.Status.AVAILABLE);
                    book.getCopies().add(copy);
                }
                book.setInitialCopies(0);
            }
            session.saveOrUpdate(book);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error saving book", e);
        }
    }
    
    @Override
    public void update(Book book) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(book);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error updating book", e);
        }
    }
    
    @Override
    public void delete(Book book) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(book);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting book", e);
        }
    }
    
    @Override
    public Optional<Book> findById(String isbn) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Book book = session.get(Book.class, isbn);
            return Optional.ofNullable(book);
        } catch (Exception e) {
            throw new RuntimeException("Error finding book by ID", e);
        }
    }
    
    @Override
    public List<Book> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery("FROM Book b ORDER BY b.title", Book.class);
            List<Book> books = query.list();
            // Force initialization of copies collection for each book
            for (Book book : books) {
                book.getCopies().size();
            }
            return books;
        } catch (Exception e) {
            throw new RuntimeException("Error finding all books", e);
        }
    }
    
    @Override
    public List<Book> findBooksWithAvailableCopies() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                "SELECT DISTINCT b FROM Book b " +
                "JOIN b.copies c " +
                "WHERE c.status = 'AVAILABLE' " +
                "ORDER BY b.title", Book.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding books with available copies", e);
        }
    }
    
    @Override
    public List<Book> findAvailableBooks() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                "SELECT DISTINCT b FROM Book b " +
                "JOIN b.copies c " +
                "WHERE c.status = 'AVAILABLE' " +
                "ORDER BY b.title", Book.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding available books", e);
        }
    }
    
    @Override
    public List<Book> findBooksByTitleContaining(String title) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                "FROM Book b WHERE LOWER(b.title) LIKE LOWER(:title) ORDER BY b.title", 
                Book.class);
            query.setParameter("title", "%" + title + "%");
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding books by title", e);
        }
    }
    
    @Override
    public List<Book> findBooksByAuthorContaining(String author) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                "FROM Book b WHERE LOWER(b.authors) LIKE LOWER(:author) ORDER BY b.title", 
                Book.class);
            query.setParameter("author", "%" + author + "%");
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding books by author", e);
        }
    }
    
    @Override
    public List<Book> findBooksByPublisher(String publisher) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                "FROM Book b WHERE b.publisher = :publisher ORDER BY b.title", 
                Book.class);
            query.setParameter("publisher", publisher);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding books by publisher", e);
        }
    }
    
    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return findById(isbn);
    }
    
    @Override
    public List<Book> searchBooks(String searchTerm) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                "FROM Book b WHERE LOWER(b.title) LIKE LOWER(:searchTerm) " +
                "OR LOWER(b.authors) LIKE LOWER(:searchTerm) " +
                "OR LOWER(b.isbn) LIKE LOWER(:searchTerm) " +
                "OR LOWER(b.publisher) LIKE LOWER(:searchTerm) " +
                "ORDER BY b.title", 
                Book.class);
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error searching books", e);
        }
    }
    
    @Override
    public List<Book> findBooksByYear(int year) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);
            
            Query<Book> query = session.createQuery(
                "FROM Book b WHERE b.publicationDate BETWEEN :startDate AND :endDate " +
                "ORDER BY b.title", 
                Book.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding books by year", e);
        }
    }
    
    @Override
    public boolean existsByIsbn(String isbn) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(b) FROM Book b WHERE b.isbn = :isbn", 
                Long.class);
            query.setParameter("isbn", isbn);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Error checking if book exists", e);
        }
    }
    
    @Override
    public long countAvailableCopies(String isbn) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(c) FROM BookCopy c WHERE c.book.isbn = :isbn AND c.status = 'AVAILABLE'", 
                Long.class);
            query.setParameter("isbn", isbn);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error counting available copies", e);
        }
    }
    
    @Override
    public long countTotalCopies(String isbn) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(c) FROM BookCopy c WHERE c.book.isbn = :isbn", 
                Long.class);
            query.setParameter("isbn", isbn);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error counting total copies", e);
        }
    }
    
    @Override
    public List<Book> findMostBorrowedBooks(int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                "SELECT b FROM Book b " +
                "JOIN b.copies c " +
                "JOIN c.loans l " +
                "GROUP BY b " +
                "ORDER BY COUNT(l) DESC", 
                Book.class);
            query.setMaxResults(limit);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding most borrowed books", e);
        }
    }
    
    @Override
    public List<Book> findBooksWithNoAvailableCopies() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                "SELECT b FROM Book b " +
                "WHERE NOT EXISTS (SELECT c FROM BookCopy c WHERE c.book = b AND c.status = 'AVAILABLE') " +
                "ORDER BY b.title", 
                Book.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding books with no available copies", e);
        }
    }
} 