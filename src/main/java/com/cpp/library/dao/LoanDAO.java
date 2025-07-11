package com.cpp.library.dao;

import com.cpp.library.model.Loan;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanDAO {
    
    // Basic CRUD operations
    void save(Loan loan);
    void update(Loan loan);
    void delete(Loan loan);
    Optional<Loan> findById(Long loanId);
    List<Loan> findAll();
    
    // Business-specific queries
    List<Loan> findOverdueLoans();
    List<Loan> findActiveLoans();
    List<Loan> findLoansByStudent(String broncoId);
    List<Loan> findLoansByDateRange(LocalDate startDate, LocalDate endDate);
    List<Loan> findLoansByBookCopy(String barCode);
    
    // Date-based queries
    List<Loan> findLoansAfterDate(LocalDate date);
    List<Loan> findLoansBetweenDates(LocalDate startDate, LocalDate endDate);
    List<Loan> findReturnedLoansBetweenDates(LocalDate startDate, LocalDate endDate);
    
    // Return operations
    void returnLoan(Long loanId);
    void returnLoan(Loan loan);
    
    // Validation queries
    long countActiveLoansByStudent(String broncoId);
    boolean hasOverdueLoans(String broncoId);
    List<Loan> findOverdueLoansByStudent(String broncoId);
    boolean hasActiveLoanForCopy(String broncoId, String bookCopyBarCode);
    
    // Reporting queries
    List<Loan> findLoansByPeriod(LocalDate startDate, LocalDate endDate);
    long countLoansByPeriod(LocalDate startDate, LocalDate endDate);
    List<Loan> findLoansByBook(String isbn);
} 