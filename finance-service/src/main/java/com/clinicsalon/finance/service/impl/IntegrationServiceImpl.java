package com.clinicsalon.finance.service.impl;

import com.clinicsalon.finance.client.AppointmentClient;
import com.clinicsalon.finance.client.ClientClient;
import com.clinicsalon.finance.service.IntegrationService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntegrationServiceImpl implements IntegrationService {

    private final ClientClient clientClient;
    private final AppointmentClient appointmentClient;
    
    private static final String INTEGRATION_CB = "integrationService";

    @Override
    @CircuitBreaker(name = INTEGRATION_CB, fallbackMethod = "getClientInfoFallback")
    public Map<String, Object> getClientInfo(UUID clientId) {
        log.info("Fetching client info for client ID: {}", clientId);
        return clientClient.getClientById(clientId);
    }

    public Map<String, Object> getClientInfoFallback(UUID clientId, Exception ex) {
        log.warn("Circuit breaker fallback for getClientInfo. Client ID: {}, Error: {}", clientId, ex.getMessage());
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("id", clientId);
        fallbackResponse.put("name", "Cliente indisponível");
        fallbackResponse.put("email", "indisponivel@exemplo.com");
        fallbackResponse.put("phone", "N/A");
        fallbackResponse.put("fallback", true);
        fallbackResponse.put("circuitBreakerFallback", true);
        return fallbackResponse;
    }

    @Override
    @CircuitBreaker(name = INTEGRATION_CB, fallbackMethod = "getAppointmentInfoFallback")
    public Map<String, Object> getAppointmentInfo(UUID appointmentId) {
        log.info("Fetching appointment info for appointment ID: {}", appointmentId);
        return appointmentClient.getAppointmentById(appointmentId);
    }
    
    public Map<String, Object> getAppointmentInfoFallback(UUID appointmentId, Exception ex) {
        log.warn("Circuit breaker fallback for getAppointmentInfo. Appointment ID: {}, Error: {}", appointmentId, ex.getMessage());
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("id", appointmentId);
        fallbackResponse.put("status", "UNKNOWN");
        fallbackResponse.put("date", "N/A");
        fallbackResponse.put("time", "N/A");
        fallbackResponse.put("clientId", null);
        fallbackResponse.put("professionalId", null);
        fallbackResponse.put("serviceId", null);
        fallbackResponse.put("fallback", true);
        fallbackResponse.put("circuitBreakerFallback", true);
        return fallbackResponse;
    }

    @Override
    @CircuitBreaker(name = INTEGRATION_CB, fallbackMethod = "getServiceDetailsFallback")
    public Map<String, Object> getServiceDetailsForAppointment(UUID appointmentId) {
        log.info("Fetching service details for appointment ID: {}", appointmentId);
        return appointmentClient.getServiceDetailsForAppointment(appointmentId);
    }
    
    public Map<String, Object> getServiceDetailsFallback(UUID appointmentId, Exception ex) {
        log.warn("Circuit breaker fallback for getServiceDetailsForAppointment. Appointment ID: {}, Error: {}", appointmentId, ex.getMessage());
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("appointmentId", appointmentId);
        fallbackResponse.put("serviceName", "Serviço indisponível");
        fallbackResponse.put("price", 0.0);
        fallbackResponse.put("duration", 0);
        fallbackResponse.put("fallback", true);
        fallbackResponse.put("circuitBreakerFallback", true);
        return fallbackResponse;
    }
}
