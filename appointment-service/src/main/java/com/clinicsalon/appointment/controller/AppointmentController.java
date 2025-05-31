package com.clinicsalon.appointment.controller;

import com.clinicsalon.appointment.dto.AppointmentRequest;
import com.clinicsalon.appointment.dto.AppointmentResponse;
import com.clinicsalon.appointment.model.AppointmentStatus;
import com.clinicsalon.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Agendamentos", description = "API para gerenciamento de agendamentos")
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Operation(summary = "Listar todos os agendamentos", description = "Retorna uma lista paginada de todos os agendamentos")
    @GetMapping
    public ResponseEntity<Page<AppointmentResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(appointmentService.findAll(pageable));
    }

    @Operation(summary = "Buscar agendamentos por cliente", description = "Retorna uma lista paginada de agendamentos de um cliente específico")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<Page<AppointmentResponse>> findByClientId(
            @Parameter(description = "ID do cliente") @PathVariable Long clientId,
            Pageable pageable) {
        return ResponseEntity.ok(appointmentService.findByClientId(clientId, pageable));
    }

    @Operation(summary = "Buscar agendamentos por profissional", description = "Retorna uma lista paginada de agendamentos de um profissional específico")
    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<Page<AppointmentResponse>> findByProfessionalId(
            @Parameter(description = "ID do profissional") @PathVariable Long professionalId,
            Pageable pageable) {
        return ResponseEntity.ok(appointmentService.findByProfessionalId(professionalId, pageable));
    }

    @Operation(summary = "Buscar agendamentos por status", description = "Retorna uma lista paginada de agendamentos com um status específico")
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<AppointmentResponse>> findByStatus(
            @Parameter(description = "Status do agendamento") @PathVariable AppointmentStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(appointmentService.findByStatus(status, pageable));
    }

    @Operation(summary = "Buscar agendamento por ID", description = "Retorna um agendamento específico pelo seu ID")
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> findById(
            @Parameter(description = "ID do agendamento") @PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.findById(id));
    }

    @Operation(summary = "Buscar agendamentos do profissional por dia", description = "Retorna a lista de agendamentos de um profissional em um dia específico")
    @GetMapping("/professional/{professionalId}/day")
    public ResponseEntity<List<AppointmentResponse>> findProfessionalAppointmentsForDay(
            @Parameter(description = "ID do profissional") @PathVariable Long professionalId,
            @Parameter(description = "Data (yyyy-MM-dd'T'HH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime day) {
        return ResponseEntity.ok(appointmentService.findProfessionalAppointmentsForDay(professionalId, day));
    }

    @Operation(summary = "Criar novo agendamento", description = "Cria um novo agendamento e retorna seus dados")
    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(request));
    }

    @Operation(summary = "Atualizar agendamento", description = "Atualiza os dados de um agendamento existente")
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> update(
            @Parameter(description = "ID do agendamento") @PathVariable Long id,
            @Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.update(id, request));
    }

    @Operation(summary = "Atualizar status do agendamento", description = "Atualiza apenas o status de um agendamento existente")
    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @Parameter(description = "ID do agendamento") @PathVariable Long id,
            @Parameter(description = "Novo status") @RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, status));
    }
}
