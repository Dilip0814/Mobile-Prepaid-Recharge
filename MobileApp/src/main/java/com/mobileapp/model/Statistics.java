package com.mobileapp.model;


import jakarta.persistence.*;

@Entity
public class Statistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer totalUsers;
    private Integer activeUsers;
    private Integer inactiveUsers;
    private Double totalRevenue;
    private String majorLocation;
    private Integer majorLocationCount;
    private String popularPlanId;
    private String popularPlanCategory;
    private Integer popularPlanCount;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getTotalUsers() {
		return totalUsers;
	}
	public void setTotalUsers(Integer totalUsers) {
		this.totalUsers = totalUsers;
	}
	public Integer getActiveUsers() {
		return activeUsers;
	}
	public void setActiveUsers(Integer activeUsers) {
		this.activeUsers = activeUsers;
	}
	public Integer getInactiveUsers() {
		return inactiveUsers;
	}
	public void setInactiveUsers(Integer inactiveUsers) {
		this.inactiveUsers = inactiveUsers;
	}
	public Double getTotalRevenue() {
		return totalRevenue;
	}
	public void setTotalRevenue(Double totalRevenue) {
		this.totalRevenue = totalRevenue;
	}
	public String getMajorLocation() {
		return majorLocation;
	}
	public void setMajorLocation(String majorLocation) {
		this.majorLocation = majorLocation;
	}
	public Integer getMajorLocationCount() {
		return majorLocationCount;
	}
	public void setMajorLocationCount(Integer majorLocationCount) {
		this.majorLocationCount = majorLocationCount;
	}
	public String getPopularPlanId() {
		return popularPlanId;
	}
	public void setPopularPlanId(String popularPlanId) {
		this.popularPlanId = popularPlanId;
	}
	public String getPopularPlanCategory() {
		return popularPlanCategory;
	}
	public void setPopularPlanCategory(String popularPlanCategory) {
		this.popularPlanCategory = popularPlanCategory;
	}
	public Integer getPopularPlanCount() {
		return popularPlanCount;
	}
	public void setPopularPlanCount(Integer popularPlanCount) {
		this.popularPlanCount = popularPlanCount;
	}

    // Getters and Setters
    
    
}