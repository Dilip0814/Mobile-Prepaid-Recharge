package com.mobileapp.repository;


import com.mobileapp.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, String> {
    List<Plan> findByCategory_CategoryId(String categoryId);
    
    // Fetch active plans
    List<Plan> findByStatus(String status);

    // Fetch active plans by category
    List<Plan> findByCategory_CategoryIdAndStatus(String categoryId, String status);
    
    // Fetch a plan by its ID
    Optional<Plan> findById(String planId);
    
    
}