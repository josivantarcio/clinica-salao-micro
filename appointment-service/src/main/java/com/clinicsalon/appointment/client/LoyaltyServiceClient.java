package com.clinicsalon.appointment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "loyalty-service", fallbackFactory = LoyaltyServiceClientFallbackFactory.class)
public interface LoyaltyServiceClient {

    @GetMapping("/api/loyalty/accounts/{clientId}")
    ResponseEntity<Map<String, Object>> getLoyaltyAccount(@PathVariable("clientId") Long clientId);
    
    @PostMapping("/api/loyalty/points/add")
    ResponseEntity<Map<String, Object>> addPoints(@RequestBody Map<String, Object> pointsRequest);
    
    @GetMapping("/api/loyalty/points/balance/{clientId}")
    ResponseEntity<Map<String, Object>> getPointsBalance(@PathVariable("clientId") Long clientId);
}
