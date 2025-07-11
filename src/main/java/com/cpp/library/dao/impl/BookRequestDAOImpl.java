package com.cpp.library.dao.impl;

import com.cpp.library.dao.BookRequestDAO;
import com.cpp.library.model.Book;
import com.cpp.library.model.BookRequest;
import com.cpp.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

public class BookRequestDAOImpl implements BookRequestDAO {
    @Override
    public void save(BookRequest request) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.save(request);
            tx.commit();
        }
    }

    @Override
    public void update(BookRequest request) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.update(request);
            tx.commit();
        }
    }

    @Override
    public void delete(BookRequest request) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.delete(request);
            tx.commit();
        }
    }

    @Override
    public BookRequest findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(BookRequest.class, id);
        }
    }

    @Override
    public List<BookRequest> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from BookRequest", BookRequest.class).list();
        }
    }

    @Override
    public List<BookRequest> findPendingRequestsForBook(Book book) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BookRequest> query = session.createQuery(
                "from BookRequest where book = :book and status = 'pending'", BookRequest.class);
            query.setParameter("book", book);
            return query.list();
        }
    }
} 