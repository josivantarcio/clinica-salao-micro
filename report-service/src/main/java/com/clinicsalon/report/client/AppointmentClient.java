package com.clinicsalon.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "appointment-service", fallbackFactory = AppointmentClientFallbackFactory.class)
public interface AppointmentClient {

    @GetMapping("/api/appointments/client/{clientId}")
    List<AppointmentDto> getAppointmentsByClientId(
            @PathVariable Long clientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);

    @GetMapping("/api/appointments/professional/{professionalId}")
    List<AppointmentDto> getAppointmentsByProfessionalId(
            @PathVariable Long professionalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);
}
