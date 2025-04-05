package com.mobileapp.model;


import jakarta.persistence.*;

@Entity
public class Category {
    @Id
    private String categoryId;

    private String categoryName;

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

    // Getters and Setters
    
    
}