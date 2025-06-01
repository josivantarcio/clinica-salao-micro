package com.clinicsalon.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "appointment-service", path = "/appointment-service/api/v1/appointments", fallback = AppointmentClientFallback.class)
public interface AppointmentClient {

    @GetMapping("/{id}")
    Map<String, Object> getAppointmentById(@PathVariable("id") UUID id);
    
    @GetMapping("/{id}/service-details")
    Map<String, Object> getServiceDetailsForAppointment(@PathVariable("id") UUID id);
}
