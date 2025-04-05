package com.mobileapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CashfreeService {

	 @Value("${cashfree.app-id}")
	    private String appId;

	    @Value("${cashfree.secret-key}")
	    private String secretKey;

	    @Value("${cashfree.api-url}")
	    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String BASE_URL = "https://sandbox.cashfree.com/pg";

    public Map<String, String> createOrder(String mobileNo, double amount, String planId) {
        String url = BASE_URL + "/orders";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-version", "2022-09-01");
        headers.set("x-client-id", appId);
        headers.set("x-client-secret", secretKey);
        headers.set("Content-Type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("order_amount", amount);
        requestBody.put("order_currency", "INR");
        requestBody.put("order_id", "order_" + System.currentTimeMillis() + "_" + mobileNo);
        requestBody.put("customer_details", Map.of(
            "customer_id", mobileNo,
            "customer_phone", mobileNo
        ));
        requestBody.put("order_meta", Map.of(
            "return_url", "http://127.0.0.1:5500/transaction.html?order_id={order_id}"
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(response.getBody(), Map.class);
            Map<String, String> result = new HashMap<>();
            result.put("orderId", (String) responseMap.get("order_id"));
            result.put("paymentSessionId", (String) responseMap.get("payment_session_id"));
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Cashfree response: " + e.getMessage());
        }
    }

    public boolean verifyPayment(String orderId) {
        String url = BASE_URL + "/orders/" + orderId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-version", "2022-09-01");
        headers.set("x-client-id", appId);
        headers.set("x-client-secret", secretKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(response.getBody(), Map.class);
            String orderStatus = (String) responseMap.get("order_status");
            return "PAID".equals(orderStatus); // Only consider "PAID" as success
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify payment: " + e.getMessage());
        }
    }
}