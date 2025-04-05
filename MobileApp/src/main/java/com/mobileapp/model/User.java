package com.mobileapp.model;


import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class User {
    @Id
    private String mobileNo;
    private String username;
    private String email;
    private String location;
 
    private String password; // New field
    private String status;
    private String role;
    private String otp; // For OTP-based login
    
    
    public String getOtp() {
		return otp;
	}
	public void setOtp(String otp) {
		this.otp = otp;
	}
	public LocalDateTime getOtpExpiryTime() {
		return otpExpiryTime;
	}
	public void setOtpExpiryTime(LocalDateTime otpExpiryTime) {
		this.otpExpiryTime = otpExpiryTime;
	}
	private LocalDateTime otpExpiryTime; // OTP expiry time
    
	public String getMobileNo() {
		return mobileNo;
	}
	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}

    // Getters and Setters
    
}