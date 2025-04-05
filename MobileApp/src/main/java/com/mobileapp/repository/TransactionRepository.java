package com.mobileapp.repository;


import com.mobileapp.model.Transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findByMobileNoOrderByTransactionDateDesc(String mobileNo);
    List<Transaction> findAllByOrderByTransactionDateDesc();
    List<Transaction> findByDaysToExpiryLessThanEqual(int daysToExpiry);
    Transaction findByPlanId(String planId);
    List<Transaction> findByPaymentStatus(String paymentStatus); // New method
    
    
}