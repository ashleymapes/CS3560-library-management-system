package com.cpp.library.dao.impl;

import com.cpp.library.dao.StudentDAO;
import com.cpp.library.model.Student;
import com.cpp.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class StudentDAOImpl implements StudentDAO {
    
    @Override
    public void save(Student student) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(student);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error saving student", e);
        }
    }
    
    @Override
    public void update(Student student) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(student);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error updating student", e);
        }
    }
    
    @Override
    public void delete(Student student) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(student);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting student", e);
        }
    }
    
    @Override
    public Optional<Student> findById(String broncoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Student student = session.get(Student.class, broncoId);
            return Optional.ofNullable(student);
        } catch (Exception e) {
            throw new RuntimeException("Error finding student by ID", e);
        }
    }
    
    @Override
    public List<Student> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Student> query = session.createQuery("FROM Student s ORDER BY s.name", Student.class);
            List<Student> students = query.list();
            // Force initialization of loans collection for each student
            for (Student student : students) {
                student.getLoans().size();
            }
            return students;
        } catch (Exception e) {
            throw new RuntimeException("Error finding all students", e);
        }
    }
    
    @Override
    public List<Student> findStudentsWithOverdueLoans() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Student> query = session.createQuery(
                "SELECT DISTINCT s FROM Student s " +
                "JOIN s.loans l " +
                "WHERE l.returnDate IS NULL AND l.dueDate < CURRENT_DATE " +
                "ORDER BY s.name", Student.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding students with overdue loans", e);
        }
    }
    
    @Override
    public List<Student> findStudentsWithActiveLoans() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Student> query = session.createQuery(
                "SELECT DISTINCT s FROM Student s " +
                "JOIN s.loans l " +
                "WHERE l.returnDate IS NULL " +
                "ORDER BY s.name", Student.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding students with active loans", e);
        }
    }
    
    @Override
    public Optional<Student> findByBroncoId(String broncoId) {
        return findById(broncoId);
    }
    
    @Override
    public List<Student> findByNameContaining(String name) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Student> query = session.createQuery(
                "FROM Student s WHERE LOWER(s.name) LIKE LOWER(:name) ORDER BY s.name", 
                Student.class);
            query.setParameter("name", "%" + name + "%");
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding students by name", e);
        }
    }
    
    @Override
    public List<Student> findByDegree(String degree) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Student> query = session.createQuery(
                "FROM Student s WHERE s.degree = :degree ORDER BY s.name", 
                Student.class);
            query.setParameter("degree", degree);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding students by degree", e);
        }
    }
    
    @Override
    public boolean existsByBroncoId(String broncoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(s) FROM Student s WHERE s.broncoId = :broncoId", 
                Long.class);
            query.setParameter("broncoId", broncoId);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Error checking if student exists", e);
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
            throw new RuntimeException("Error counting active loans", e);
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
    public List<Student> searchStudents(String searchTerm) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Student> query = session.createQuery(
                "FROM Student s WHERE LOWER(s.name) LIKE LOWER(:searchTerm) " +
                "OR LOWER(s.broncoId) LIKE LOWER(:searchTerm) " +
                "OR LOWER(s.degree) LIKE LOWER(:searchTerm) " +
                "ORDER BY s.name", 
                Student.class);
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error searching students", e);
        }
    }
} 