package com.clinicsalon.finance.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class AppointmentClientFallback implements AppointmentClient {

    @Override
    public Map<String, Object> getAppointmentById(UUID id) {
        log.warn("Fallback executed for getAppointmentById with id: {}", id);
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("id", id);
        fallbackResponse.put("status", "UNKNOWN");
        fallbackResponse.put("date", "N/A");
        fallbackResponse.put("time", "N/A");
        fallbackResponse.put("clientId", null);
        fallbackResponse.put("professionalId", null);
        fallbackResponse.put("serviceId", null);
        fallbackResponse.put("fallback", true);
        return fallbackResponse;
    }

    @Override
    public Map<String, Object> getServiceDetailsForAppointment(UUID id) {
        log.warn("Fallback executed for getServiceDetailsForAppointment with id: {}", id);
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("appointmentId", id);
        fallbackResponse.put("serviceName", "Serviço indisponível");
        fallbackResponse.put("price", 0.0);
        fallbackResponse.put("duration", 0);
        fallbackResponse.put("fallback", true);
        return fallbackResponse;
    }
}
