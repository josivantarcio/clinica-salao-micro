package com.clinicsalon.appointment.controller;

import com.clinicsalon.appointment.dto.ServiceRequest;
import com.clinicsalon.appointment.dto.ServiceResponse;
import com.clinicsalon.appointment.service.ServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Serviços", description = "API para gerenciamento de serviços oferecidos")
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @Operation(summary = "Listar todos os serviços ativos", description = "Retorna uma lista paginada de serviços ativos")
    @GetMapping
    public ResponseEntity<Page<ServiceResponse>> findAllActive(Pageable pageable) {
        return ResponseEntity.ok(serviceService.findAllActive(pageable));
    }

    @Operation(summary = "Listar todos os serviços ativos sem paginação", description = "Retorna uma lista completa de serviços ativos")
    @GetMapping("/all")
    public ResponseEntity<List<ServiceResponse>> findAllActiveNoPagination() {
        return ResponseEntity.ok(serviceService.findAllActive());
    }

    @Operation(summary = "Buscar serviços por nome", description = "Retorna uma lista paginada de serviços ativos que contêm o nome especificado")
    @GetMapping("/search")
    public ResponseEntity<Page<ServiceResponse>> findByName(
            @Parameter(description = "Nome para filtrar os serviços") @RequestParam String name,
            Pageable pageable) {
        return ResponseEntity.ok(serviceService.findByNameContaining(name, pageable));
    }

    @Operation(summary = "Buscar serviço por ID", description = "Retorna um serviço específico pelo seu ID")
    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> findById(
            @Parameter(description = "ID do serviço") @PathVariable Long id) {
        return ResponseEntity.ok(serviceService.findById(id));
    }

    @Operation(summary = "Criar novo serviço", description = "Cria um novo serviço e retorna seus dados")
    @PostMapping
    public ResponseEntity<ServiceResponse> create(@Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceService.create(request));
    }

    @Operation(summary = "Atualizar serviço", description = "Atualiza os dados de um serviço existente")
    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> update(
            @Parameter(description = "ID do serviço") @PathVariable Long id,
            @Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.ok(serviceService.update(id, request));
    }

    @Operation(summary = "Desativar serviço", description = "Marca um serviço como inativo (soft delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do serviço") @PathVariable Long id) {
        serviceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Ativar serviço", description = "Reativa um serviço previamente desativado")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ServiceResponse> activate(
            @Parameter(description = "ID do serviço") @PathVariable Long id) {
        return ResponseEntity.ok(serviceService.activate(id));
    }
}
