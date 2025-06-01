package com.clinicsalon.report.service;

import com.clinicsalon.report.client.ProfessionalClient;
import com.clinicsalon.report.client.ProfessionalDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfessionalDataService {

    private final ProfessionalClient professionalClient;

    /**
     * Busca informações de um profissional pelo ID
     */
    @CircuitBreaker(name = "professionalService", fallbackMethod = "getProfessionalByIdFallback")
    public ProfessionalDto getProfessionalById(Long professionalId) {
        log.info("Fetching professional data for ID: {}", professionalId);
        Optional<ProfessionalDto> professionalOptional = professionalClient.getProfessionalById(professionalId);
        return professionalOptional.orElseThrow(() -> new RuntimeException("Profissional não encontrado: " + professionalId));
    }

    /**
     * Busca todos os profissionais
     */
    @CircuitBreaker(name = "professionalService", fallbackMethod = "getAllProfessionalsFallback")
    public List<ProfessionalDto> getAllProfessionals() {
        log.info("Fetching all professionals data");
        return professionalClient.getAllProfessionals();
    }
    
    /**
     * Busca o nome de um profissional pelo ID
     */
    @CircuitBreaker(name = "professionalService", fallbackMethod = "getProfessionalNameFallback")
    public String getProfessionalName(Long professionalId) {
        log.info("Fetching professional name for ID: {}", professionalId);
        return professionalClient.getProfessionalName(professionalId);
    }
    
    /**
     * Busca profissionais por especialização
     */
    @CircuitBreaker(name = "professionalService", fallbackMethod = "getProfessionalsBySpecializationFallback")
    public List<ProfessionalDto> getProfessionalsBySpecialization(String specialization) {
        log.info("Fetching professionals with specialization: {}", specialization);
        return professionalClient.getProfessionalsBySpecialization(specialization);
    }
    
    /**
     * Busca todos os profissionais ativos
     */
    @CircuitBreaker(name = "professionalService", fallbackMethod = "getActiveProfessionalsFallback")
    public List<ProfessionalDto> getActiveProfessionals() {
        log.info("Fetching all active professionals");
        return professionalClient.getActiveProfessionals();
    }

    /**
     * Método de fallback para getProfessionalById
     */
    public ProfessionalDto getProfessionalByIdFallback(Long professionalId, Exception ex) {
        log.warn("Fallback for professional lookup. Professional ID: {}, Error: {}", professionalId, ex.getMessage());
        return ProfessionalDto.builder()
                .id(professionalId)
                .name("Profissional Indisponível #" + professionalId)
                .email("indisponivel@exemplo.com")
                .active(true)
                .build();
    }

    /**
     * Método de fallback para getAllProfessionals
     */
    public List<ProfessionalDto> getAllProfessionalsFallback(Exception ex) {
        log.warn("Fallback for all professionals lookup. Error: {}", ex.getMessage());
        return Collections.emptyList();
    }
    
    /**
     * Método de fallback para getProfessionalName
     */
    public String getProfessionalNameFallback(Long professionalId, Exception ex) {
        log.warn("Fallback for professional name lookup. Professional ID: {}, Error: {}", professionalId, ex.getMessage());
        return "Profissional #" + professionalId;
    }
    
    /**
     * Método de fallback para getProfessionalsBySpecialization
     */
    public List<ProfessionalDto> getProfessionalsBySpecializationFallback(String specialization, Exception ex) {
        log.warn("Fallback for professionals by specialization. Specialization: {}, Error: {}", specialization, ex.getMessage());
        return Collections.emptyList();
    }
    
    /**
     * Método de fallback para getActiveProfessionals
     */
    public List<ProfessionalDto> getActiveProfessionalsFallback(Exception ex) {
        log.warn("Fallback for active professionals lookup. Error: {}", ex.getMessage());
        return Collections.emptyList();
    }
}
