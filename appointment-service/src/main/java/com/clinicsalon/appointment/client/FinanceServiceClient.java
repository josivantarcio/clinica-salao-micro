package com.clinicsalon.appointment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(name = "finance-service", fallbackFactory = FinanceServiceClientFallbackFactory.class)
public interface FinanceServiceClient {

    @PostMapping("/api/payments/create-link")
    ResponseEntity<Map<String, String>> createPaymentLink(
            @RequestBody Map<String, Object> paymentRequest);
    
    @GetMapping("/api/payments/appointment/{appointmentId}")
    ResponseEntity<Map<String, Object>> getPaymentStatusByAppointmentId(
            @PathVariable("appointmentId") Long appointmentId);
    
    @PostMapping("/api/payments/process-refund/{appointmentId}")
    ResponseEntity<Map<String, Object>> processRefund(
            @PathVariable("appointmentId") Long appointmentId);
}
