package com.clinicsalon.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "loyalty-service", fallbackFactory = LoyaltyClientFallbackFactory.class)
public interface LoyaltyClient {

    @GetMapping("/api/loyalty/clients/{clientId}/points")
    List<LoyaltyPointsDto> getClientLoyaltyPoints(@PathVariable Long clientId);

    @GetMapping("/api/loyalty/clients/{clientId}/points/by-date")
    List<LoyaltyPointsDto> getClientLoyaltyPointsByDateRange(
            @PathVariable Long clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);

    @GetMapping("/api/loyalty/clients/{clientId}/total-points")
    Integer getTotalClientPoints(@PathVariable Long clientId);
}
