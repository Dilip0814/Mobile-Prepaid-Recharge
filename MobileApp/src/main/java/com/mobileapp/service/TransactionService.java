package com.mobileapp.service;


import com.mobileapp.model.Plan;
import com.mobileapp.model.Transaction;
import com.mobileapp.model.User;
import com.mobileapp.repository.PlanRepository;
import com.mobileapp.repository.TransactionRepository;
import com.mobileapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlanRepository planRepository;

    // Add a new transaction
    public Transaction addTransaction(String mobileNo, String planId, Double price, String paymentStatus) {
        // Validate user exists
        User user = userRepository.findByMobileNo(mobileNo);
        if (user == null) {
            throw new RuntimeException("User not found with mobile number: " + mobileNo);
        }

        // Validate plan exists
        Plan plan = planRepository.findById(planId)
            .orElseThrow(() -> new RuntimeException("Plan not found with ID: " + planId));

        // Calculate dates
        Date transactionDate = new Date();
        LocalDate expiryLocalDate = LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(plan.getValidity());
        Date expiryDate = Date.from(expiryLocalDate.atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant());
        long daysToExpiry = ChronoUnit.DAYS.between(LocalDate.now(ZoneId.of("Asia/Kolkata")), expiryLocalDate);

        // Create and save transaction
        Transaction transaction = new Transaction();
        transaction.setUsername(user.getUsername());
        transaction.setMobileNo(user.getMobileNo());
        transaction.setPlanId(plan.getPlanId());
        transaction.setPlanType(plan.getCategory()); // Using plan's category as type
        transaction.setValidityDays(plan.getValidity());
        transaction.setTransactionDate(transactionDate);
        transaction.setExpiryDate(expiryDate);
        transaction.setDaysToExpiry((int) daysToExpiry);
        transaction.setPrice(price);
        transaction.setPaymentStatus(paymentStatus);

        return transactionRepository.save(transaction);
    }

    
    public Transaction updatePaymentStatus(Integer transactionId, String paymentStatus) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));
        
        transaction.setPaymentStatus(paymentStatus);
        return transactionRepository.save(transaction);
    }

    // Get all transactions
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByTransactionDateDesc();
    }

    // Get transactions where daysToExpiry is 3 or less
    public List<Transaction> getExpiringTransactions() {
        return transactionRepository.findByDaysToExpiryLessThanEqual(3);
    }
    
 // In TransactionService.java
    public List<Transaction> getUserTransactions(String mobileNo) {
        List<Transaction> transactions = transactionRepository.findByMobileNoOrderByTransactionDateDesc(mobileNo);
        
        // Enhance transactions with plan details
        return transactions.stream()
            .map(transaction -> {
                Plan plan = planRepository.findById(transaction.getPlanId()).orElse(null);
                if (plan != null) {
                    transaction.setPlanDetails(
                        plan.getService() + " | " + 
                        plan.getDataLimit() + " | " + 
                        plan.getValidity() + " Days"
                    );
                }
                return transaction;
            })
            .collect(Collectors.toList());
    }
}