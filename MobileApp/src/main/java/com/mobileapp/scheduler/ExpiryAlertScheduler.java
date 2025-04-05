package com.mobileapp.scheduler;


import com.mobileapp.service.ExpiryAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExpiryAlertScheduler {

    @Autowired
    private ExpiryAlertService expiryAlertService;

    // Run every day at midnight (Indian time zone)
    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Kolkata")
    public void refreshExpiryAlertsDaily() {
        expiryAlertService.checkAndAddExpiringPlans();
    }
}