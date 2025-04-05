package com.mobileapp.service;

import com.mobileapp.model.User;
import com.mobileapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TwilioService twilioService;

    // Fetch all users with role = USER
    public List<User> getAllUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    // User login with mobile number
    public User userLogin(String mobileNo) {
        return userRepository.findByMobileNo(mobileNo);
    }

    // Update user details (alternate mobile number, location, email)
    public User updateUserDetails(String mobileNo, String location, String email) {
        User user = userRepository.findByMobileNo(mobileNo);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (location != null) {
            user.setLocation(location);
        }
        if (email != null) {
            user.setEmail(email);
        }
        return userRepository.save(user);
    }

    // Admin login with username and password
    public User adminLogin(String username, String password) {
        return userRepository.findByUsernameAndPasswordAndRole(username, password, "ADMIN");
    }

    // Update user status
    public User updateUserStatus(String mobileNo, String status) {
        User user = userRepository.findByMobileNo(mobileNo);
        if (user != null) {
            user.setStatus(status);
            userRepository.save(user);
        }
        return user;
    }

    // Find user by email
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Change admin password
    public User changeAdminPassword(String email, String newPassword, String confirmPassword) {
        User admin = userRepository.findByEmailAndRole(email, "ADMIN");
        if (admin == null) {
            throw new RuntimeException("Admin not found with the provided email.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("New password and confirm password do not match.");
        }
        admin.setPassword(newPassword);
        return userRepository.save(admin);
    }

    // Generate a 6-digit OTP
    public String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // Generate and send OTP
    public String generateAndSendOTP(String mobileNo) {
        if (mobileNo == null || mobileNo.isEmpty()) {
            throw new IllegalArgumentException("Mobile number is required!");
        }

        // Check if the mobile number exists in the User table as-is
        User user = userRepository.findByMobileNo(mobileNo);
        if (user == null) {
            // If not found, try with country code (e.g., +91)
            String mobileWithCountryCode = "+91" + mobileNo.trim(); // Adjust country code as needed
            user = userRepository.findByMobileNo(mobileWithCountryCode);
            if (user == null) {
                throw new RuntimeException("User not found for mobile number: " + mobileNo);
            }
            mobileNo = mobileWithCountryCode; // Use the full number for SMS
        } else {
            // If found as-is but doesn’t have country code, add it for Twilio
            if (!mobileNo.startsWith("+")) {
                mobileNo = "+91" + mobileNo.trim(); // Adjust country code as needed
            }
        }

        // Generate OTP and save it
        String otp = generateOTP();
        user.setOtp(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        // Send OTP via Twilio
        twilioService.sendSMS(mobileNo, "Your OTP is: " + otp);
        return "OTP sent successfully!";
    }

    // Verify OTP
    public boolean verifyOTP(String mobileNo, String otp) {
        if (mobileNo == null || otp == null) {
            return false; // Invalid input
        }

        // Try the mobile number as provided first
        User user = userRepository.findByMobileNo(mobileNo);
        if (user == null) {
            // If not found, try with country code
            String mobileWithCountryCode = "+91" + mobileNo.trim(); // Match generateAndSendOTP logic
            user = userRepository.findByMobileNo(mobileWithCountryCode);
            if (user == null) {
                System.out.println("User not found for mobile number: " + mobileNo); // Debugging
                return false;
            }
            mobileNo = mobileWithCountryCode; // Use the matched format
        }

        // Debugging output
        System.out.println("Verifying OTP for mobileNo: " + mobileNo);
        System.out.println("Stored OTP: " + user.getOtp());
        System.out.println("Provided OTP: " + otp);
        System.out.println("Expiry Time: " + user.getOtpExpiryTime());
        System.out.println("Current Time: " + LocalDateTime.now());

        if (user.getOtp() != null && user.getOtp().equals(otp)) {
            if (LocalDateTime.now().isBefore(user.getOtpExpiryTime())) {
                clearOTP(user);
                System.out.println("OTP verified successfully!");
                return true;
            } else {
                System.out.println("OTP expired!");
            }
        } else {
            System.out.println("OTP mismatch or null!");
        }
        return false;
    }

    // Clear OTP after successful verification
    private void clearOTP(User user) {
        user.setOtp(null);
        user.setOtpExpiryTime(null);
        userRepository.save(user);
    }
}