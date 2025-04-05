package com.mobileapp.service;


import com.mobileapp.model.ExpiryAlert;
import com.mobileapp.model.Transaction;
import com.mobileapp.repository.ExpiryAlertRepository;
import com.mobileapp.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpiryAlertService {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ExpiryAlertRepository expiryAlertRepository;

    // Check for expiring plans and add them to the expiry_alerts table
    public void checkAndAddExpiringPlans() {
        // Fetch all transactions where daysToExpiry <= 3
        List<Transaction> expiringTransactions = transactionRepository.findByDaysToExpiryLessThanEqual(3);

        // Clear the expiry_alerts table before adding new records
        expiryAlertRepository.deleteAll();

        // Add expiring transactions to the expiry_alerts table
        for (Transaction transaction : expiringTransactions) {
            ExpiryAlert expiryAlert = new ExpiryAlert();
            expiryAlert.setUsername(transaction.getUsername());
            expiryAlert.setMobileNo(transaction.getMobileNo());
            expiryAlert.setPlanId(transaction.getPlanId());
            expiryAlert.setDaysToExpiry(transaction.getDaysToExpiry());

            expiryAlertRepository.save(expiryAlert);
        }
    }

    // Get all expiring plans
    public List<ExpiryAlert> getAllExpiringPlans() {
        return expiryAlertRepository.findAllByOrderByDaysToExpiryAsc();
    }
}