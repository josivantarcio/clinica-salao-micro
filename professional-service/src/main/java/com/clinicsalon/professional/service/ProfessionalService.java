package com.clinicsalon.professional.service;

import com.clinicsalon.professional.dto.ProfessionalRequest;
import com.clinicsalon.professional.dto.ProfessionalResponse;
import com.clinicsalon.professional.exception.ResourceAlreadyExistsException;
import com.clinicsalon.professional.exception.ResourceNotFoundException;
import com.clinicsalon.professional.mapper.ProfessionalMapper;
import com.clinicsalon.professional.model.Professional;
import com.clinicsalon.professional.repository.ProfessionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.clinicsalon.monitoring.aspect.MonitorPerformance;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfessionalService {

    private final ProfessionalRepository professionalRepository;
    private final ProfessionalMapper professionalMapper;

    @Transactional
    @MonitorPerformance(description = "Criar profissional", thresholdMillis = 500, alertOnError = true)
    public ProfessionalResponse createProfessional(ProfessionalRequest request) {
        log.info("Creating professional with name: {}", request.getName());
        
        // Check if professional with same email or CPF already exists
        if (professionalRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Professional with email " + request.getEmail() + " already exists");
        }
        
        if (professionalRepository.existsByCpf(request.getCpf())) {
            throw new ResourceAlreadyExistsException("Professional with CPF " + request.getCpf() + " already exists");
        }
        
        Professional professional = professionalMapper.toEntity(request);
        professional.setCreatedAt(LocalDateTime.now());
        professional.setUpdatedAt(LocalDateTime.now());
        
        Professional savedProfessional = professionalRepository.save(professional);
        log.info("Professional created with ID: {}", savedProfessional.getId());
        
        return professionalMapper.toResponse(savedProfessional);
    }
    
    @MonitorPerformance(description = "Buscar profissional por ID", thresholdMillis = 200)
    public ProfessionalResponse getProfessionalById(Long id) {
        log.info("Fetching professional with ID: {}", id);
        
        Professional professional = findProfessionalById(id);
        return professionalMapper.toResponse(professional);
    }
    
    @MonitorPerformance(description = "Listar todos os profissionais", thresholdMillis = 300)
    public List<ProfessionalResponse> getAllProfessionals() {
        log.info("Fetching all professionals");
        
        return professionalRepository.findAll().stream()
                .map(professionalMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @MonitorPerformance(description = "Listar profissionais ativos", thresholdMillis = 300)
    public List<ProfessionalResponse> getActiveProfessionals() {
        log.info("Fetching active professionals");
        
        return professionalRepository.findByIsActiveTrue().stream()
                .map(professionalMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @MonitorPerformance(description = "Buscar profissionais por especialização", thresholdMillis = 300)
    public List<ProfessionalResponse> getProfessionalsBySpecialization(String specialization) {
        log.info("Fetching professionals with specialization: {}", specialization);
        
        return professionalRepository.findBySpecializationIgnoreCase(specialization).stream()
                .map(professionalMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    @MonitorPerformance(description = "Atualizar profissional", thresholdMillis = 500, alertOnError = true)
    public ProfessionalResponse updateProfessional(Long id, ProfessionalRequest request) {
        log.info("Updating professional with ID: {}", id);
        
        Professional professional = findProfessionalById(id);
        
        // Check if email is being changed and if it already exists
        if (!professional.getEmail().equals(request.getEmail()) && 
                professionalRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Professional with email " + request.getEmail() + " already exists");
        }
        
        // Check if CPF is being changed and if it already exists
        if (!professional.getCpf().equals(request.getCpf()) && 
                professionalRepository.existsByCpf(request.getCpf())) {
            throw new ResourceAlreadyExistsException("Professional with CPF " + request.getCpf() + " already exists");
        }
        
        professionalMapper.updateEntityFromRequest(request, professional);
        professional.setUpdatedAt(LocalDateTime.now());
        
        Professional updatedProfessional = professionalRepository.save(professional);
        log.info("Professional updated with ID: {}", updatedProfessional.getId());
        
        return professionalMapper.toResponse(updatedProfessional);
    }
    
    @Transactional
    @MonitorPerformance(description = "Desativar profissional", thresholdMillis = 300, alertOnError = true)
    public ProfessionalResponse deactivateProfessional(Long id) {
        log.info("Deactivating professional with ID: {}", id);
        
        Professional professional = findProfessionalById(id);
        professional.setIsActive(false);
        professional.setUpdatedAt(LocalDateTime.now());
        
        Professional deactivatedProfessional = professionalRepository.save(professional);
        log.info("Professional deactivated with ID: {}", deactivatedProfessional.getId());
        
        return professionalMapper.toResponse(deactivatedProfessional);
    }
    
    @Transactional
    @MonitorPerformance(description = "Ativar profissional", thresholdMillis = 300, alertOnError = true)
    public ProfessionalResponse activateProfessional(Long id) {
        log.info("Activating professional with ID: {}", id);
        
        Professional professional = findProfessionalById(id);
        professional.setIsActive(true);
        professional.setUpdatedAt(LocalDateTime.now());
        
        Professional activatedProfessional = professionalRepository.save(professional);
        log.info("Professional activated with ID: {}", activatedProfessional.getId());
        
        return professionalMapper.toResponse(activatedProfessional);
    }
    
    @Transactional
    @MonitorPerformance(description = "Excluir profissional", thresholdMillis = 500, alertOnError = true)
    public void deleteProfessional(Long id) {
        log.info("Deleting professional with ID: {}", id);
        
        Professional professional = findProfessionalById(id);
        professionalRepository.delete(professional);
        
        log.info("Professional deleted with ID: {}", id);
    }
    
    public String getProfessionalName(Long id) {
        log.info("Fetching professional name with ID: {}", id);
        
        return findProfessionalById(id).getName();
    }
    
    private Professional findProfessionalById(Long id) {
        return professionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found with ID: " + id));
    }
}
