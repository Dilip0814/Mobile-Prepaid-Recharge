package com.mobileapp.controller;

import com.mobileapp.exception.MobileAppException;
import com.mobileapp.model.Category;
import com.mobileapp.model.Plan;
import com.mobileapp.model.Support;
import com.mobileapp.model.Transaction;
import com.mobileapp.model.User;
import com.mobileapp.service.CashfreeService;
import com.mobileapp.service.PlanService;
import com.mobileapp.service.SupportService;
import com.mobileapp.service.TransactionService;
import com.mobileapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private PlanService planService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private SupportService supportService;
    
    @Autowired
    private CashfreeService cashfreeService;

    // Login with mobile number only
    @PostMapping("/login-with-mobile")
    public ResponseEntity<String> loginWithMobile(@RequestParam String mobileNo) {
        User user = userService.userLogin(mobileNo);
        if (user != null && "ACTIVE".equals(user.getStatus())) {
            return ResponseEntity.status(HttpStatus.OK).body("Login Successful");
        }
        throw new MobileAppException("Login failed. User is inactive or does not exist.");
    }

    // Generate and send OTP
    @PostMapping("/generate-otp")
    public ResponseEntity<String> generateOTP(@RequestParam String mobileNo) {
        String response = userService.generateAndSendOTP(mobileNo);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Verify OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOTP(@RequestParam String mobileNo, @RequestParam String otp) {
        if (userService.verifyOTP(mobileNo, otp)) {
            return ResponseEntity.status(HttpStatus.OK).body("OTP verified successfully!");
        }
        throw new MobileAppException("Invalid or expired OTP!");
    }

    // View all active plans
    @GetMapping("/plans")
    public ResponseEntity<List<Plan>> getAllPlans() {
        List<Plan> plans = planService.getActivePlans();
        return ResponseEntity.status(HttpStatus.OK).body(plans);
    }

    // Get all categories
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = planService.getAllCategories();
        return ResponseEntity.status(HttpStatus.OK).body(categories);
    }

    // View active plans by category
    @GetMapping("/plans/category/{categoryId}")
    public ResponseEntity<List<Plan>> getPlansByCategory(@PathVariable String categoryId) {
        List<Plan> plans = planService.getActivePlansByCategory(categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(plans);
    }

    // Purchase a plan
    @PostMapping("/purchase")
    public ResponseEntity<String> purchasePlan(@RequestParam String mobileNo, @RequestParam String planId) {
        try {
            Plan plan = planService.getPlanById(planId);
            if (plan == null || !"ACTIVE".equals(plan.getStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Plan is no longer active.");
            }
            Transaction transaction = transactionService.addTransaction(
                mobileNo, 
                planId,
                plan.getPrice(), // Added price
                "PENDING" // Initial payment status
            );
            return ResponseEntity.status(HttpStatus.OK).body("Plan purchased successfully! Transaction ID: " + transaction.getTransactionId());
        } catch (RuntimeException e) {
            throw new MobileAppException("Failed to purchase plan: " + e.getMessage());
        }
    }

    // View user's transaction history
    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getUserTransactions(@RequestParam String mobileNo) {
        List<Transaction> transactions = transactionService.getUserTransactions(mobileNo);
        return ResponseEntity.status(HttpStatus.OK).body(transactions);
    }
    
 // Add this method to UserController.java
 // Add this method to UserController.java
    @GetMapping("/details")
    public ResponseEntity<Map<String, String>> getUserDetails(@RequestParam String mobileNo) {
        try {
            User user = userService.userLogin(mobileNo);
            if (user == null) {
                throw new MobileAppException("User not found");
            }
            
            // Create a map with only the required fields
            Map<String, String> userDetails = Map.of(
                "mobileNo", user.getMobileNo() != null ? user.getMobileNo() : "",
                "username", user.getUsername() != null ? user.getUsername() : "",
                "email", user.getEmail() != null ? user.getEmail() : "",
                "location", user.getLocation() != null ? user.getLocation() : ""
            );
            
            return ResponseEntity.status(HttpStatus.OK).body(userDetails);
        } catch (RuntimeException e) {
            throw new MobileAppException("Failed to fetch user details: " + e.getMessage());
        }
    }

    // Update user details (alternate mobile number, location, email)
    @PutMapping("/update")
    public ResponseEntity<String> updateUserDetails(
            @RequestParam String mobileNo,
            @RequestParam(required = false) String alternateMobileNo,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String email) {
        try {
            User user = userService.updateUserDetails(mobileNo, location, email);
            return ResponseEntity.status(HttpStatus.OK).body("User details updated successfully!");
        } catch (RuntimeException e) {
            throw new MobileAppException("Failed to update user details: " + e.getMessage());
        }
    }

    // Send support message from user
    @PostMapping("/send-support")
    public ResponseEntity<String> sendSupportMessage(@RequestParam String mobileNo, @RequestParam String message) {
        try {
            User user = userService.userLogin(mobileNo);
            if (user == null || !"ACTIVE".equals(user.getStatus())) {
                throw new MobileAppException("The user is not found in the database.");
            }
            Support savedSupport = supportService.sendSupportMessage(mobileNo, message);
            return ResponseEntity.status(HttpStatus.OK).body("Support message sent successfully! Token ID: " + savedSupport.getTokenId());
        } catch (RuntimeException e) {
            throw new MobileAppException("Failed to send support message: " + e.getMessage());
        }
    }
    
    
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, String>> createOrder(@RequestBody Map<String, Object> request) {
        String mobileNo = (String) request.get("mobileNo");
        double amount = ((Number) request.get("amount")).doubleValue();
        Map<String, String> planDetails = (Map<String, String>) request.get("planDetails");
        
        // Extract planId by matching plan details
        String[] planInfoParts = planDetails.get("info").split(" \\| ");
        if (planInfoParts.length != 3) {
            throw new MobileAppException("Invalid plan details format");
        }
        String service = planInfoParts[0];
        String dataLimit = planInfoParts[1];
        int validity = Integer.parseInt(planInfoParts[2].split(" ")[0]); // Extract number from "X Days"
        double price = Double.parseDouble(planDetails.get("price").replace("₹", ""));
        
        Plan plan = planService.getPlanByDetails(service, dataLimit, validity, price);
        if (plan == null) {
            throw new MobileAppException("Plan not found with provided details");
        }

        Map<String, String> orderResponse = cashfreeService.createOrder(mobileNo, amount, plan.getPlanId());
        return ResponseEntity.status(HttpStatus.OK).body(orderResponse);
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<Map<String, String>> verifyPayment(@RequestBody Map<String, Object> request) {
        String orderId = (String) request.get("orderId");
        String mobileNo = (String) request.get("mobileNo");
        Map<String, String> planDetails = (Map<String, String>) request.get("planDetails");
        
        try {
            // Extract plan details
            String[] planInfoParts = planDetails.get("info").split(" \\| ");
            if (planInfoParts.length != 3) {
                throw new MobileAppException("Invalid plan details format");
            }
            String service = planInfoParts[0];
            String dataLimit = planInfoParts[1];
            int validity = Integer.parseInt(planInfoParts[2].split(" ")[0]);
            double price = Double.parseDouble(planDetails.get("price").replace("₹", ""));
            
            // Find the plan
            Plan plan = planService.getPlanByDetails(service, dataLimit, validity, price);
            if (plan == null) {
                throw new MobileAppException("Plan not found with provided details");
            }

            // Verify payment with Cashfree
            if (cashfreeService.verifyPayment(orderId)) {
                // Create transaction record
                Transaction transaction = transactionService.addTransaction(
                    mobileNo, 
                    plan.getPlanId(),
                    plan.getPrice(),
                    "SUCCESS" // Payment status
                );
                
                // Return success response
                Map<String, String> response = Map.of(
                    "message", "Payment verified and transaction recorded",
                    "transactionId", transaction.getTransactionId().toString(),
                    "paymentStatus", transaction.getPaymentStatus(),
                    "planId", plan.getPlanId(),
                    "expiryDate", transaction.getExpiryDate().toString()
                );
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                // Payment verification failed - still record as failed transaction
                Transaction transaction = transactionService.addTransaction(
                    mobileNo, 
                    plan.getPlanId(),
                    plan.getPrice(),
                    "FAILED" // Payment status
                );
                throw new MobileAppException("Payment verification failed for order: " + orderId);
            }
        } catch (Exception e) {
            throw new MobileAppException("Payment verification error: " + e.getMessage());
        }
    }
}