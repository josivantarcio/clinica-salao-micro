package com.clinicsalon.appointment.controller;

import com.clinicsalon.appointment.service.AppointmentPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/appointments/payments")
@RequiredArgsConstructor
@Tag(name = "Pagamentos de Agendamentos", description = "API para gerenciamento de pagamentos relacionados a agendamentos")
public class AppointmentPaymentController {

    private final AppointmentPaymentService paymentService;

    @Operation(summary = "Gerar link de pagamento", description = "Gera um link de pagamento para um agendamento específico")
    @PostMapping("/{appointmentId}/payment-link")
    public ResponseEntity<Map<String, String>> createPaymentLink(
            @Parameter(description = "ID do agendamento") @PathVariable Long appointmentId) {
        return ResponseEntity.ok(paymentService.createPaymentLink(appointmentId));
    }

    @Operation(summary = "Verificar status de pagamento", description = "Verifica o status de pagamento de um agendamento")
    @GetMapping("/{appointmentId}/status")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(
            @Parameter(description = "ID do agendamento") @PathVariable Long appointmentId) {
        return ResponseEntity.ok(paymentService.getPaymentStatus(appointmentId));
    }

    @Operation(summary = "Processar reembolso", description = "Processa o reembolso de um agendamento cancelado")
    @PostMapping("/{appointmentId}/refund")
    public ResponseEntity<Map<String, Object>> processRefund(
            @Parameter(description = "ID do agendamento") @PathVariable Long appointmentId) {
        return ResponseEntity.ok(paymentService.processRefund(appointmentId));
    }
}
