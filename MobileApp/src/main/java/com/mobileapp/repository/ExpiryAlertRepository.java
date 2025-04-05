package com.mobileapp.repository;

import com.mobileapp.model.ExpiryAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpiryAlertRepository extends JpaRepository<ExpiryAlert, Integer> {
    List<ExpiryAlert> findAllByOrderByDaysToExpiryAsc();
    Optional<ExpiryAlert> findById(Integer alertId); // Added to check existing alerts
}