package com.cpp.library.dao;

import com.cpp.library.model.Student;
import java.util.List;
import java.util.Optional;

public interface StudentDAO {
    
    // Basic CRUD operations
    void save(Student student);
    void update(Student student);
    void delete(Student student);
    Optional<Student> findById(String broncoId);
    List<Student> findAll();
    
    // Business-specific queries
    List<Student> findStudentsWithOverdueLoans();
    List<Student> findStudentsWithActiveLoans();
    Optional<Student> findByBroncoId(String broncoId);
    List<Student> findByNameContaining(String name);
    List<Student> findByDegree(String degree);
    
    // Validation queries
    boolean existsByBroncoId(String broncoId);
    long countActiveLoansByStudent(String broncoId);
    boolean hasOverdueLoans(String broncoId);
    
    // Search and filter
    List<Student> searchStudents(String searchTerm);
} 