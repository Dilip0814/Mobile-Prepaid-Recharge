package com.mobileapp.repository;





import com.mobileapp.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    User findByMobileNo(String mobileNo); // For user login
    User findByUsernameAndPasswordAndRole(String username, String password, String role); // For admin login
    
    List<User> findByRole(String role); 
    
    
    // New method to find admin by email
    User findByEmailAndRole(String email, String role);
    User findByEmail(String email);
}