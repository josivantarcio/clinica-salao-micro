package com.clinicsalon.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "professional-service", fallbackFactory = ProfessionalClientFallbackFactory.class)
public interface ProfessionalClient {

    @GetMapping("/api/v1/professionals/{professionalId}")
    Optional<ProfessionalDto> getProfessionalById(@PathVariable Long professionalId);

    @GetMapping("/api/v1/professionals")
    List<ProfessionalDto> getAllProfessionals();
    
    @GetMapping("/api/v1/professionals/{professionalId}/name")
    String getProfessionalName(@PathVariable Long professionalId);
    
    @GetMapping("/api/v1/professionals/specialization/{specialization}")
    List<ProfessionalDto> getProfessionalsBySpecialization(@PathVariable String specialization);
    
    @GetMapping("/api/v1/professionals/active")
    List<ProfessionalDto> getActiveProfessionals();
}
