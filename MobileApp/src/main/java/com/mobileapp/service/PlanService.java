package com.mobileapp.service;

import com.mobileapp.model.Category;
import com.mobileapp.model.Plan;
import com.mobileapp.repository.CategoryRepository;
import com.mobileapp.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlanService {
    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Get all plans
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    // Get plans by category
    public List<Plan> getPlansByCategory(String categoryId) {
        return planRepository.findByCategory_CategoryId(categoryId);
    }

    // Add a new plan
    public Plan addPlan(Plan plan) {
        // Fetch the category based on the categoryId
        Category category = categoryRepository.findById(plan.getCategory())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Set the category object in the plan
        plan.setCategory(category);
        return planRepository.save(plan);
    }

    // Update a plan by planId
    public Plan updatePlan(String planId, Plan updatedPlan) {
        Optional<Plan> existingPlanOptional = planRepository.findById(planId);
        if (existingPlanOptional.isPresent()) {
            Plan existingPlan = existingPlanOptional.get();

            // Fetch the category based on the categoryId
            Category category = categoryRepository.findById(updatedPlan.getCategory())
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            // Update the fields of the existing plan
            existingPlan.setCategory(category);
            existingPlan.setDataLimit(updatedPlan.getDataLimit());
            existingPlan.setValidity(updatedPlan.getValidity());
            existingPlan.setService(updatedPlan.getService());
            existingPlan.setPrice(updatedPlan.getPrice());
            existingPlan.setStatus(updatedPlan.getStatus());

            return planRepository.save(existingPlan);
        } else {
            throw new RuntimeException("Plan not found with ID: " + planId);
        }
    }
    // Change plan status (active/inactive)
    public Plan changePlanStatus(String planId, String status) {
        Optional<Plan> existingPlanOptional = planRepository.findById(planId);
        if (existingPlanOptional.isPresent()) {
            Plan existingPlan = existingPlanOptional.get();
            existingPlan.setStatus(status); // Update the status
            return planRepository.save(existingPlan);
        } else {
            throw new RuntimeException("Plan not found with ID: " + planId);
        }
    }

    // Add a new category
    public Category addCategory(Category category) {
        return categoryRepository.save(category);
    }

    // Get all categories
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    // Get active plans for users
    public List<Plan> getActivePlans() {
        return planRepository.findByStatus("ACTIVE");
    }

    // Get active plans by category for users
    public List<Plan> getActivePlansByCategory(String categoryId) {
        return planRepository.findByCategory_CategoryIdAndStatus(categoryId, "ACTIVE");
    }
    
    public Plan getPlanById(String planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found with ID: " + planId));
    }
    
 // Get plans by status (active/inactive)
    public List<Plan> getPlansByStatus(String status) {
        return planRepository.findByStatus(status);
    }
    
    public Plan getPlanByDetails(String service, String dataLimit, int validity, double price) {
        return planRepository.findAll().stream()
            .filter(plan -> plan.getService().equals(service) &&
                           plan.getDataLimit().equals(dataLimit) &&
                           plan.getValidity() == validity &&
                           plan.getPrice().equals(price))
            .findFirst()
            .orElse(null);
    }
}