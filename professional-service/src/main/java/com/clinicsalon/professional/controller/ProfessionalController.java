package com.clinicsalon.professional.controller;

import com.clinicsalon.professional.dto.ProfessionalRequest;
import com.clinicsalon.professional.dto.ProfessionalResponse;
import com.clinicsalon.professional.service.ProfessionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/professionals")
@RequiredArgsConstructor
@Tag(name = "Professional API", description = "Endpoints para gerenciamento de profissionais")
public class ProfessionalController {

    private final ProfessionalService professionalService;

    @PostMapping
    @Operation(summary = "Criar um novo profissional", description = "Cria um novo profissional com os dados fornecidos")
    public ResponseEntity<ProfessionalResponse> createProfessional(@Valid @RequestBody ProfessionalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(professionalService.createProfessional(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar profissional por ID", description = "Retorna os detalhes de um profissional específico")
    public ResponseEntity<ProfessionalResponse> getProfessionalById(@PathVariable Long id) {
        return ResponseEntity.ok(professionalService.getProfessionalById(id));
    }

    @GetMapping
    @Operation(summary = "Listar todos os profissionais", description = "Retorna uma lista com todos os profissionais cadastrados")
    public ResponseEntity<List<ProfessionalResponse>> getAllProfessionals() {
        return ResponseEntity.ok(professionalService.getAllProfessionals());
    }

    @GetMapping("/active")
    @Operation(summary = "Listar profissionais ativos", description = "Retorna uma lista com todos os profissionais ativos")
    public ResponseEntity<List<ProfessionalResponse>> getActiveProfessionals() {
        return ResponseEntity.ok(professionalService.getActiveProfessionals());
    }

    @GetMapping("/specialization/{specialization}")
    @Operation(summary = "Listar profissionais por especialização", description = "Retorna uma lista de profissionais com a especialização especificada")
    public ResponseEntity<List<ProfessionalResponse>> getProfessionalsBySpecialization(@PathVariable String specialization) {
        return ResponseEntity.ok(professionalService.getProfessionalsBySpecialization(specialization));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar profissional", description = "Atualiza os dados de um profissional existente")
    public ResponseEntity<ProfessionalResponse> updateProfessional(@PathVariable Long id, @Valid @RequestBody ProfessionalRequest request) {
        return ResponseEntity.ok(professionalService.updateProfessional(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desativar profissional", description = "Desativa um profissional existente")
    public ResponseEntity<ProfessionalResponse> deactivateProfessional(@PathVariable Long id) {
        return ResponseEntity.ok(professionalService.deactivateProfessional(id));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Ativar profissional", description = "Ativa um profissional existente")
    public ResponseEntity<ProfessionalResponse> activateProfessional(@PathVariable Long id) {
        return ResponseEntity.ok(professionalService.activateProfessional(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir profissional", description = "Remove permanentemente um profissional do sistema")
    public ResponseEntity<Void> deleteProfessional(@PathVariable Long id) {
        professionalService.deleteProfessional(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/name")
    @Operation(summary = "Buscar nome do profissional", description = "Retorna apenas o nome do profissional pelo ID")
    public ResponseEntity<String> getProfessionalName(@PathVariable Long id) {
        return ResponseEntity.ok(professionalService.getProfessionalName(id));
    }
}
