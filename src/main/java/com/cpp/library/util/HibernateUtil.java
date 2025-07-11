package com.cpp.library.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

public class HibernateUtil {
    private static SessionFactory sessionFactory;
    
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();
                
                // Database connection settings
                configuration.setProperty(Environment.DRIVER, ConfigLoader.getProperty("DB_DRIVER", "org.postgresql.Driver"));
                configuration.setProperty(Environment.URL, ConfigLoader.getProperty("DB_URL", "jdbc:postgresql://localhost:5432/librarydb"));
                configuration.setProperty(Environment.USER, ConfigLoader.getProperty("DB_USERNAME", "postgres"));
                configuration.setProperty(Environment.PASS, ConfigLoader.getProperty("DB_PASSWORD", ""));
                
                // Hibernate properties
                configuration.setProperty(Environment.DIALECT, ConfigLoader.getProperty("HIBERNATE_DIALECT", "org.hibernate.dialect.PostgreSQLDialect"));
                configuration.setProperty(Environment.SHOW_SQL, ConfigLoader.getProperty("HIBERNATE_SHOW_SQL", "true"));
                configuration.setProperty(Environment.FORMAT_SQL, ConfigLoader.getProperty("HIBERNATE_FORMAT_SQL", "true"));
                configuration.setProperty(Environment.HBM2DDL_AUTO, ConfigLoader.getProperty("HIBERNATE_HBM2DDL_AUTO", "update"));
                
                // Add entity classes
                configuration.addAnnotatedClass(com.cpp.library.model.Student.class);
                configuration.addAnnotatedClass(com.cpp.library.model.Book.class);
                configuration.addAnnotatedClass(com.cpp.library.model.BookCopy.class);
                configuration.addAnnotatedClass(com.cpp.library.model.Loan.class);
                configuration.addAnnotatedClass(com.cpp.library.model.BookRequest.class);
                
                sessionFactory = configuration.buildSessionFactory();
            } catch (Exception e) {
                System.err.println("Error creating Hibernate SessionFactory: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return sessionFactory;
    }
    
    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
} 