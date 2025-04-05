package com.mobileapp.service;


import com.mobileapp.model.Support;
import com.mobileapp.model.User;
import com.mobileapp.repository.SupportRepository;
import com.mobileapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupportService {
    @Autowired
    private SupportRepository supportRepository;

    @Autowired
    private UserRepository userRepository;

    // Send support message from user
    public Support sendSupportMessage(String mobileNo, String message) {
        // Check if the user is logged in (exists in the database)
        User user = userRepository.findByMobileNo(mobileNo);
        if (user == null) {
            throw new RuntimeException("User not found. Please log in first.");
        }

        // Create and save the support message
        Support support = new Support();
        support.setUsername(user.getUsername());
        support.setMobileNo(user.getMobileNo());
        support.setMessage(message);

        return supportRepository.save(support);
    }

    // Get all support messages for admin
    public List<Support> getAllSupportMessages() {
        return supportRepository.findAllByOrderByTokenIdDesc();
    }

    // Delete support message by admin
    public void deleteSupportMessage(Integer tokenId) {
        supportRepository.deleteById(tokenId);
    }
}