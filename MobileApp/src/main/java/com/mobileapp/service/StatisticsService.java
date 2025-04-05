package com.mobileapp.service;

import com.mobileapp.model.Plan;
import com.mobileapp.model.Statistics;
import com.mobileapp.model.Transaction;
import com.mobileapp.model.User;
import com.mobileapp.repository.StatisticsRepository;
import com.mobileapp.repository.TransactionRepository;
import com.mobileapp.repository.UserRepository;
import com.mobileapp.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private StatisticsRepository statisticsRepository;

    @Autowired
    private PlanRepository planRepository;

    // Calculate and save statistics
    public void calculateAndSaveStatistics() {
        // Fetch all users
        List<User> users = userRepository.findAll();

        // Calculate total users with role = USER
        int totalUsers = (int) users.stream()
                .filter(user -> "USER".equals(user.getRole()))
                .count();

        // Calculate active and inactive users with role = USER
        int activeUsers = (int) users.stream()
                .filter(user -> "USER".equals(user.getRole()) && "ACTIVE".equals(user.getStatus()))
                .count();
        int inactiveUsers = totalUsers - activeUsers;

        // Calculate total revenue
        double totalRevenue = transactionRepository.findAll().stream()
                .mapToDouble(transaction -> {
                    // Fetch the plan details using the planId from the transaction
                    Plan plan = planRepository.findById(transaction.getPlanId()).orElse(null);
                    return plan != null ? plan.getPrice() : 0.0;
                })
                .sum();

        // Find the major location among users with role = USER
        Map<String, Long> locationCounts = users.stream()
                .filter(user -> "USER".equals(user.getRole()))
                .collect(Collectors.groupingBy(User::getLocation, Collectors.counting()));
        Map.Entry<String, Long> majorLocationEntry = locationCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
        String majorLocation = majorLocationEntry != null ? majorLocationEntry.getKey() : "N/A";
        int majorLocationCount = majorLocationEntry != null ? majorLocationEntry.getValue().intValue() : 0;

        // Find the popular plan
        Map<String, Long> planCounts = transactionRepository.findAll().stream()
                .collect(Collectors.groupingBy(Transaction::getPlanId, Collectors.counting()));
        Map.Entry<String, Long> popularPlanEntry = planCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
        String popularPlanId = popularPlanEntry != null ? popularPlanEntry.getKey() : "N/A";
        String popularPlanCategory = popularPlanEntry != null ? 
                planRepository.findById(popularPlanId).orElse(new Plan()).getCategory() : "N/A"; // Directly use getCategory()
        int popularPlanCount = popularPlanEntry != null ? popularPlanEntry.getValue().intValue() : 0;

        // Save statistics to the database
        Statistics statistics = new Statistics();
        statistics.setTotalUsers(totalUsers);
        statistics.setActiveUsers(activeUsers);
        statistics.setInactiveUsers(inactiveUsers);
        statistics.setTotalRevenue(totalRevenue);
        statistics.setMajorLocation(majorLocation);
        statistics.setMajorLocationCount(majorLocationCount);
        statistics.setPopularPlanId(popularPlanId);
        statistics.setPopularPlanCategory(popularPlanCategory);
        statistics.setPopularPlanCount(popularPlanCount);

        statisticsRepository.deleteAll(); // Clear previous statistics
        statisticsRepository.save(statistics);
    }

    // Get the latest statistics
    public Statistics getLatestStatistics() {
        return statisticsRepository.findAll().stream()
                .findFirst()
                .orElse(null);
    }
}