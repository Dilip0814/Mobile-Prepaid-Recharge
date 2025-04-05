package com.mobileapp.controller;

import com.mobileapp.exception.MobileAppException;
import com.mobileapp.model.Category;
import com.mobileapp.model.ExpiryAlert;
import com.mobileapp.model.Plan;
import com.mobileapp.model.Statistics;
import com.mobileapp.model.Support;
import com.mobileapp.model.Transaction;
import com.mobileapp.model.User;
import com.mobileapp.service.ExpiryAlertService;
import com.mobileapp.service.PlanService;
import com.mobileapp.service.StatisticsService;
import com.mobileapp.service.SupportService;
import com.mobileapp.service.TransactionService;
import com.mobileapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://127.0.0.1:5500") // Allow requests from the frontend
public class AdminController {
    @Autowired
    private UserService userService;

    @Autowired
    private PlanService planService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ExpiryAlertService expiryAlertService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private SupportService supportService;

    // Admin login with username and password
    @PostMapping("/login")
    public ResponseEntity<String> adminLogin(@RequestParam String username, @RequestParam String password) {
        try {
            User admin = userService.adminLogin(username, password);
            if (admin != null) {
                return ResponseEntity.status(HttpStatus.OK).body("Login Successful");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed. Please try again.");
        }
    }


    
    // Validate email and role
    @PostMapping("/validate-email")
    public ResponseEntity<String> validateEmail(@RequestParam String email) {
        try {
            User user = userService.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not registered.");
            }
            if (!"ADMIN".equals(user.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email does not belong to an admin.");
            }
            return ResponseEntity.status(HttpStatus.OK).body("Email validated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to validate email.");
        }
    }
    
    // Change admin password
    @PutMapping("/reset-password")
    public ResponseEntity<String> changeAdminPassword(
            @RequestParam String email,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Passwords do not match.");
            }
            User admin = userService.changeAdminPassword(email, newPassword, confirmPassword);
            if (admin != null) {
                return ResponseEntity.status(HttpStatus.OK).body("Password changed successfully!");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to reset password.");
        }
    }
   
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsersByRole("USER");
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    // Add a new plan
    @PostMapping("/plans")
    public ResponseEntity<Plan> addPlan(@RequestBody Plan plan) {
        try {
            Plan savedPlan = planService.addPlan(plan);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPlan);
        } catch (RuntimeException e) {
            throw new MobileAppException("Failed to add plan: " + e.getMessage());
        }
    }

    // Update a plan by planId
    @PutMapping("/plans/{planId}")
    public ResponseEntity<Plan> updatePlan(@PathVariable String planId, @RequestBody Plan updatedPlan) {
        try {
            Plan updated = planService.updatePlan(planId, updatedPlan);
            return ResponseEntity.status(HttpStatus.OK).body(updated);
        } catch (RuntimeException e) {
            throw new MobileAppException("Failed to update plan: " + e.getMessage());
        }
    }

    // Change plan status (active/inactive)
    @PutMapping("/plans/{planId}/status")
    public ResponseEntity<Plan> changePlanStatus(@PathVariable String planId, @RequestParam String status) {
        try {
            Plan updatedPlan = planService.changePlanStatus(planId, status);
            return ResponseEntity.status(HttpStatus.OK).body(updatedPlan);
        } catch (RuntimeException e) {
            throw new MobileAppException("Failed to change plan status: " + e.getMessage());
        }
    }

    // Add a new category
    @PostMapping("/categories")
    public ResponseEntity<Category> addCategory(@RequestBody Category category) {
        try {
            Category savedCategory = planService.addCategory(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
        } catch (RuntimeException e) {
            throw new MobileAppException("Failed to add category: " + e.getMessage());
        }
    }

    // Get all categories
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = planService.getAllCategories();
        return ResponseEntity.status(HttpStatus.OK).body(categories);
    }
    
    // View plans by category
    @GetMapping("/plans/category/{categoryId}")
    public ResponseEntity<List<Plan>> getPlansByCategory(@PathVariable String categoryId) {
        List<Plan> plans = planService.getPlansByCategory(categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(plans);
    }

   
    // View all plans
    @GetMapping("/plans")
    public ResponseEntity<List<Plan>> getAllPlans() {
        List<Plan> plans = planService.getAllPlans();
        return ResponseEntity.status(HttpStatus.OK).body(plans);
    }
    
    // View plans by status (active/inactive)
    @GetMapping("/plans/status/{status}")
    public ResponseEntity<List<Plan>> getPlansByStatus(@PathVariable String status) {
        List<Plan> plans = planService.getPlansByStatus(status);
        return ResponseEntity.status(HttpStatus.OK).body(plans);
    }

    // View all transactions
    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        return ResponseEntity.status(HttpStatus.OK).body(transactions);
    }

    // Update user status (activate/deactivate)
    @PutMapping("/users/status")
    public ResponseEntity<User> updateUserStatus(@RequestParam String mobileNo, @RequestParam String status) {
        try {
            User updatedUser = userService.updateUserStatus(mobileNo, status);
            return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
        } catch (RuntimeException e) {
            throw new MobileAppException("Failed to update user status: " + e.getMessage());
        }
    }

    

    // View all expiring plans (daysToExpiry <= 3)
    @GetMapping("/expiry-alerts")
    public ResponseEntity<List<ExpiryAlert>> getAllExpiringPlans() {
        List<ExpiryAlert> expiringPlans = expiryAlertService.getAllExpiringPlans();
        return ResponseEntity.status(HttpStatus.OK).body(expiringPlans);
    }

    // View the latest statistics
    @GetMapping("/statistics")
    public ResponseEntity<Statistics> getStatistics() {
        Statistics statistics = statisticsService.getLatestStatistics();
        return ResponseEntity.status(HttpStatus.OK).body(statistics);
    }

    // Get all support messages
    @GetMapping("/support")
    public ResponseEntity<List<Support>> getAllSupportMessages() {
        List<Support> supportMessages = supportService.getAllSupportMessages();
        return ResponseEntity.status(HttpStatus.OK).body(supportMessages);
    }

    // Delete support message by token ID
    @DeleteMapping("/support/{tokenId}")
    public ResponseEntity<String> deleteSupportMessage(@PathVariable Integer tokenId) {
        try {
            supportService.deleteSupportMessage(tokenId);
            return ResponseEntity.status(HttpStatus.OK).body("Support message deleted successfully!");
        } catch (RuntimeException e) {
            throw new MobileAppException("Failed to delete support message: " + e.getMessage());
        }
    }
    
 
}