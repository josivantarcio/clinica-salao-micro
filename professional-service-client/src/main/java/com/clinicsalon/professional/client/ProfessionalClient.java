package com.clinicsalon.professional.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "professional-service")
public interface ProfessionalClient {

    @GetMapping("/api/v1/professionals/{id}")
    ResponseEntity<ProfessionalResponse> findById(@PathVariable("id") Long id);
    
    @GetMapping("/api/v1/professionals/{id}/name")
    ResponseEntity<String> findNameById(@PathVariable("id") Long id);
}
