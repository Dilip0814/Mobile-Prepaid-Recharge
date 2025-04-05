package com.mobileapp.model;

import jakarta.persistence.*;

@Entity
public class Plan {
    @Id
    private String planId;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private String dataLimit;
    private long validity;
    private String service;
    private Double price;
    private String status;

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    // Return only the categoryId instead of the entire Category object
    public String getCategory() {
        return this.category.getCategoryId();
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDataLimit() {
        return dataLimit;
    }

    public void setDataLimit(String dataLimit) {
        this.dataLimit = dataLimit;
    }

    public long getValidity() {
        return validity;
    }

    public void setValidity(long validity) {
        this.validity = validity;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}