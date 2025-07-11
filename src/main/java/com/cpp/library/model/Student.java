package com.cpp.library.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "students")
public class Student {
    
    @Id
    @Column(name = "bronco_id", nullable = false, unique = true)
    private String broncoId;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "address", nullable = false)
    private String address;
    
    @Column(name = "degree", nullable = false)
    private String degree;
    
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Loan> loans = new ArrayList<>();
    
    // Constructors
    public Student() {}
    
    public Student(String broncoId, String name, String address, String degree) {
        this.broncoId = broncoId;
        this.name = name;
        this.address = address;
        this.degree = degree;
    }
    
    // Getters and Setters
    public String getBroncoId() {
        return broncoId;
    }
    
    public void setBroncoId(String broncoId) {
        this.broncoId = broncoId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getDegree() {
        return degree;
    }
    
    public void setDegree(String degree) {
        this.degree = degree;
    }
    
    public List<Loan> getLoans() {
        return loans;
    }
    
    public void setLoans(List<Loan> loans) {
        this.loans = loans;
    }
    
    // Helper methods
    public void addLoan(Loan loan) {
        loans.add(loan);
        loan.setStudent(this);
    }
    
    public void removeLoan(Loan loan) {
        loans.remove(loan);
        loan.setStudent(null);
    }
    
    // Check if student has overdue loans
    public boolean hasOverdueLoans() {
        return loans.stream().anyMatch(Loan::isOverdue);
    }
    
    // Get current active loans count
    public long getActiveLoansCount() {
        return loans.stream().filter(loan -> !loan.isReturned()).count();
    }
    
    // Get current active loans
    public List<Loan> getActiveLoans() {
        return loans.stream().filter(loan -> !loan.isReturned()).toList();
    }
    
    // Get overdue loans
    public List<Loan> getOverdueLoans() {
        return loans.stream().filter(Loan::isOverdue).toList();
    }
    
    // Check if student can borrow more books (max 5)
    public boolean canBorrowMoreBooks() {
        return getActiveLoansCount() < 5;
    }
    
    // Get total loans count
    public int getTotalLoansCount() {
        return loans.size();
    }
    
    // Check if student has an active loan for a specific BookCopy
    public boolean hasActiveLoanForCopy(BookCopy copy) {
        return loans.stream()
            .filter(loan -> !loan.isReturned())
            .anyMatch(loan -> loan.getBookCopies().stream().anyMatch(c -> c.equals(copy)));
    }
    
    @Override
    public String toString() {
        return "Student{" +
                "broncoId='" + broncoId + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", degree='" + degree + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(broncoId, student.broncoId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(broncoId);
    }
} 