package com.cpp.library.dao.impl;

import com.cpp.library.dao.LoanDAO;
import com.cpp.library.model.Loan;
import com.cpp.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class LoanDAOImpl implements LoanDAO {
    
    @Override
    public void save(Loan loan) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(loan);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error saving loan", e);
        }
    }
    
    @Override
    public void update(Loan loan) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(loan);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error updating loan", e);
        }
    }
    
    @Override
    public void delete(Loan loan) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(loan);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting loan", e);
        }
    }
    
    @Override
    public Optional<Loan> findById(Long loanId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Loan loan = session.get(Loan.class, loanId);
            return Optional.ofNullable(loan);
        } catch (Exception e) {
            throw new RuntimeException("Error finding loan by ID", e);
        }
    }
    
    @Override
    public List<Loan> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Loan> query = session.createQuery(
                "SELECT DISTINCT l FROM Loan l " +
                "JOIN FETCH l.student " +
                "LEFT JOIN FETCH l.bookCopies bc " +
                "LEFT JOIN FETCH bc.book " +
                "ORDER BY l.borrowingDate DESC", Loan.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all loans", e);
        }
    }
    
    @Override
    public List<Loan> findOverdueLoans() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Loan> query = session.createQuery(
                "FROM Loan l WHERE l.returnDate IS NULL AND l.dueDate < CURRENT_DATE " +
                "ORDER BY l.dueDate", Loan.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding overdue loans", e);
        }
    }
    
    @Override
    public List<Loan> findActiveLoans() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Loan> query = session.createQuery(
                "FROM Loan l WHERE l.returnDate IS NULL ORDER BY l.dueDate", Loan.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding active loans", e);
        }
    }
    
    @Override
    public List<Loan> findLoansByStudent(String broncoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Loan> query = session.createQuery(
                "FROM Loan l WHERE l.student.broncoId = :broncoId ORDER BY l.borrowingDate DESC", 
                Loan.class);
            query.setParameter("broncoId", broncoId);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding loans by student", e);
        }
    }
    
    @Override
    public List<Loan> findLoansByDateRange(LocalDate startDate, LocalDate endDate) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Loan> query = session.createQuery(
                "SELECT DISTINCT l FROM Loan l " +
                "LEFT JOIN FETCH l.bookCopies bc " +
                "LEFT JOIN FETCH bc.book " +
                "WHERE l.borrowingDate BETWEEN :startDate AND :endDate " +
                "ORDER BY l.borrowingDate DESC", Loan.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding loans by date range", e);
        }
    }
    
    @Override
    public List<Loan> findLoansByBookCopy(String barCode) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Loan> query = session.createQuery(
                "SELECT l FROM Loan l JOIN l.bookCopies bc WHERE bc.barCode = :barCode " +
                "ORDER BY l.borrowingDate DESC", Loan.class);
            query.setParameter("barCode", barCode);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding loans by book copy", e);
        }
    }
    
    @Override
    public void returnLoan(Long loanId) {
        Optional<Loan> loanOpt = findById(loanId);
        if (loanOpt.isPresent()) {
            Loan loan = loanOpt.get();
            returnLoan(loan);
        }
    }
    
    @Override
    public void returnLoan(Loan loan) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Mark all book copies as available
            for (var bookCopy : loan.getBookCopies()) {
                bookCopy.markAsAvailable();
                session.merge(bookCopy);
            }
            
            // Set return date
            loan.setReturnDate(LocalDate.now());
            session.merge(loan);
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error returning loan", e);
        }
    }
    
    @Override
    public long countActiveLoansByStudent(String broncoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(l) FROM Loan l WHERE l.student.broncoId = :broncoId AND l.returnDate IS NULL", 
                Long.class);
            query.setParameter("broncoId", broncoId);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error counting active loans by student", e);
        }
    }
    
    @Override
    public boolean hasOverdueLoans(String broncoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(l) FROM Loan l WHERE l.student.broncoId = :broncoId " +
                "AND l.returnDate IS NULL AND l.dueDate < CURRENT_DATE", 
                Long.class);
            query.setParameter("broncoId", broncoId);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Error checking overdue loans", e);
        }
    }
    
    @Override
    public List<Loan> findOverdueLoansByStudent(String broncoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Loan> query = session.createQuery(
                "FROM Loan l WHERE l.student.broncoId = :broncoId " +
                "AND l.returnDate IS NULL AND l.dueDate < CURRENT_DATE " +
                "ORDER BY l.dueDate", Loan.class);
            query.setParameter("broncoId", broncoId);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding overdue loans by student", e);
        }
    }
    
    @Override
    public List<Loan> findLoansByPeriod(LocalDate startDate, LocalDate endDate) {
        return findLoansByDateRange(startDate, endDate);
    }
    
    @Override
    public long countLoansByPeriod(LocalDate startDate, LocalDate endDate) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(l) FROM Loan l WHERE l.borrowingDate BETWEEN :startDate AND :endDate", 
                Long.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error counting loans by period", e);
        }
    }
    
    @Override
    public List<Loan> findLoansByBook(String isbn) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Loan> query = session.createQuery(
                "SELECT DISTINCT l FROM Loan l JOIN l.bookCopies bc WHERE bc.book.isbn = :isbn " +
                "ORDER BY l.borrowingDate DESC", Loan.class);
            query.setParameter("isbn", isbn);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding loans by book", e);
        }
    }
    
    @Override
    public List<Loan> findLoansAfterDate(LocalDate date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Loan> query = session.createQuery(
                "FROM Loan l WHERE l.borrowingDate > :date ORDER BY l.borrowingDate DESC", 
                Loan.class);
            query.setParameter("date", date);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding loans after date", e);
        }
    }
    
    @Override
    public List<Loan> findLoansBetweenDates(LocalDate startDate, LocalDate endDate) {
        return findLoansByDateRange(startDate, endDate);
    }
    
    @Override
    public List<Loan> findReturnedLoansBetweenDates(LocalDate startDate, LocalDate endDate) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Loan> query = session.createQuery(
                "FROM Loan l WHERE l.returnDate BETWEEN :startDate AND :endDate " +
                "ORDER BY l.returnDate DESC", Loan.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding returned loans between dates", e);
        }
    }

    @Override
    public boolean hasActiveLoanForCopy(String broncoId, String bookCopyBarCode) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(l) FROM Loan l JOIN l.bookCopies bc " +
                "WHERE l.student.broncoId = :broncoId " +
                "AND bc.barCode = :barCode " +
                "AND l.returnDate IS NULL", Long.class);
            query.setParameter("broncoId", broncoId);
            query.setParameter("barCode", bookCopyBarCode);
            return query.uniqueResult() > 0;
        }
    }
} 