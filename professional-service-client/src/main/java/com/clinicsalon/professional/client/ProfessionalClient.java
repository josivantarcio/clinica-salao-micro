package com.clinicsalon.professional.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Cliente Feign para comunicação com o microserviço professional-service.
 * Permite acessar dados de profissionais com tolerância a falhas.
 */
@FeignClient(name = "professional-service", fallbackFactory = ProfessionalClientFallbackFactory.class)
public interface ProfessionalClient {

    @GetMapping("/api/v1/professionals/{id}")
    ResponseEntity<ProfessionalResponse> findById(@PathVariable("id") Long id);
    
    @GetMapping("/api/v1/professionals/{id}/name")
    ResponseEntity<String> findNameById(@PathVariable("id") Long id);
    
    @GetMapping("/api/v1/professionals/active")
    ResponseEntity<List<ProfessionalResponse>> findAllActive();
    
    @GetMapping("/api/v1/professionals/specialization/{specialization}")
    ResponseEntity<List<ProfessionalResponse>> findBySpecialization(@PathVariable("specialization") String specialization);
}
