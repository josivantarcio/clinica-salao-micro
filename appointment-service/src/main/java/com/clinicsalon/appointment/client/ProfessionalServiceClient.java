package com.clinicsalon.appointment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "professional-service")
public interface ProfessionalServiceClient {

    @GetMapping("/api/professionals/{id}/name")
    String getProfessionalName(@PathVariable("id") Long id);
}
