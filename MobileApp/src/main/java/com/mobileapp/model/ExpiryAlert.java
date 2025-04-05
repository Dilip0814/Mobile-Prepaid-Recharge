package com.mobileapp.model;


import jakarta.persistence.*;

@Entity
public class ExpiryAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer alertId;

   
    private String username; // Username of the user
    private String mobileNo; // Mobile number of the user
    private String planId; // ID of the plan
    private Integer daysToExpiry; // Days remaining until the plan expires
	public Integer getAlertId() {
		return alertId;
	}
	public void setAlertId(Integer alertId) {
		this.alertId = alertId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getMobileNo() {
		return mobileNo;
	}
	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}
	public String getPlanId() {
		return planId;
	}
	public void setPlanId(String planId) {
		this.planId = planId;
	}
	public Integer getDaysToExpiry() {
		return daysToExpiry;
	}
	public void setDaysToExpiry(Integer daysToExpiry) {
		this.daysToExpiry = daysToExpiry;
	}

    // Getters and Setters
    
    
}