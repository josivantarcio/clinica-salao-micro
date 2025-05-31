package com.clinicsalon.appointment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "client-service")
public interface ClientServiceClient {

    @GetMapping("/api/clients/{id}/name")
    String getClientName(@PathVariable("id") Long id);
}
