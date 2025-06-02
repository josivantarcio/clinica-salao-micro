package com.clinicsalon.appointment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "appointment-service", 
             fallbackFactory = AppointmentClientFallbackFactory.class,
             path = "/api/v1/appointments")
public interface AppointmentClient {

    @GetMapping("/{id}")
    ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable String id);
    
    @GetMapping
    ResponseEntity<PagedAppointmentsResponse> getAppointments(
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) String professionalId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);
    
    @GetMapping("/client/{clientId}")
    ResponseEntity<List<AppointmentResponse>> getAppointmentsByClientId(@PathVariable String clientId);
    
    @GetMapping("/professional/{professionalId}")
    ResponseEntity<List<AppointmentResponse>> getAppointmentsByProfessionalId(@PathVariable String professionalId);
    
    @GetMapping("/date/{date}")
    ResponseEntity<List<AppointmentResponse>> getAppointmentsByDate(@PathVariable LocalDate date);
    
    @PostMapping
    ResponseEntity<AppointmentResponse> createAppointment(@RequestBody AppointmentRequest request);
}
