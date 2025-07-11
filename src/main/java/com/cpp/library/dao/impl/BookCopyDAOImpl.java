package com.cpp.library.dao.impl;

import com.cpp.library.dao.BookCopyDAO;
import com.cpp.library.model.BookCopy;
import com.cpp.library.model.Book;
import com.cpp.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class BookCopyDAOImpl implements BookCopyDAO {
    
    @Override
    public void save(BookCopy bookCopy) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(bookCopy);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error saving book copy", e);
        }
    }
    
    @Override
    public void update(BookCopy bookCopy) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(bookCopy);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error updating book copy", e);
        }
    }
    
    @Override
    public void delete(BookCopy bookCopy) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(bookCopy);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting book copy", e);
        }
    }
    
    @Override
    public Optional<BookCopy> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            BookCopy bookCopy = session.get(BookCopy.class, id);
            return Optional.ofNullable(bookCopy);
        } catch (Exception e) {
            throw new RuntimeException("Error finding book copy by ID", e);
        }
    }
    
    @Override
    public List<BookCopy> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BookCopy> query = session.createQuery("FROM BookCopy", BookCopy.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all book copies", e);
        }
    }
    
    @Override
    public List<BookCopy> findByBook(Book book) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BookCopy> query = session.createQuery(
                "FROM BookCopy bc WHERE bc.book = :book", BookCopy.class);
            query.setParameter("book", book);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding book copies by book", e);
        }
    }
    
    @Override
    public List<BookCopy> findAvailableByBook(Book book) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BookCopy> query = session.createQuery(
                "SELECT c FROM BookCopy c JOIN FETCH c.book WHERE c.book = :book AND c.status = 'AVAILABLE'", BookCopy.class);
            query.setParameter("book", book);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding available book copies by book", e);
        }
    }
    
    @Override
    public List<BookCopy> findLoanedByBook(Book book) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BookCopy> query = session.createQuery(
                "FROM BookCopy bc WHERE bc.book = :book AND bc.available = false", BookCopy.class);
            query.setParameter("book", book);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding loaned book copies by book", e);
        }
    }
    
    @Override
    public List<BookCopy> findAvailableCopies() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BookCopy> query = session.createQuery(
                "FROM BookCopy bc WHERE bc.available = true", BookCopy.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding available book copies", e);
        }
    }
    
    @Override
    public List<BookCopy> findLoanedCopies() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BookCopy> query = session.createQuery(
                "FROM BookCopy bc WHERE bc.available = false", BookCopy.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding loaned book copies", e);
        }
    }
    
    @Override
    public List<BookCopy> findByCondition(String condition) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BookCopy> query = session.createQuery(
                "FROM BookCopy bc WHERE bc.condition = :condition", BookCopy.class);
            query.setParameter("condition", condition);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding book copies by condition", e);
        }
    }
    
    @Override
    public List<BookCopy> findOverdueCopies() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BookCopy> query = session.createQuery(
                "SELECT DISTINCT bc FROM BookCopy bc " +
                "JOIN bc.loans l " +
                "WHERE l.returnDate IS NULL AND l.dueDate < CURRENT_DATE", BookCopy.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding overdue book copies", e);
        }
    }
    
    @Override
    public boolean existsById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(bc) FROM BookCopy bc WHERE bc.id = :id", Long.class);
            query.setParameter("id", id);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Error checking if book copy exists", e);
        }
    }
    
    @Override
    public long countAvailableCopies(Book book) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(bc) FROM BookCopy bc WHERE bc.book = :book AND bc.available = true", Long.class);
            query.setParameter("book", book);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error counting available copies", e);
        }
    }
    
    @Override
    public long countLoanedCopies(Book book) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(bc) FROM BookCopy bc WHERE bc.book = :book AND bc.available = false", Long.class);
            query.setParameter("book", book);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error counting loaned copies", e);
        }
    }
    
    @Override
    public long countTotalCopies(Book book) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(bc) FROM BookCopy bc WHERE bc.book = :book", Long.class);
            query.setParameter("book", book);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error counting total copies", e);
        }
    }
    
    @Override
    public List<BookCopy> findCopiesNeedingMaintenance() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BookCopy> query = session.createQuery(
                "FROM BookCopy bc WHERE bc.condition IN ('Poor', 'Damaged')", BookCopy.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding copies needing maintenance", e);
        }
    }
    
    @Override
    public List<BookCopy> findCopiesByCondition(String condition) {
        return findByCondition(condition);
    }
} 