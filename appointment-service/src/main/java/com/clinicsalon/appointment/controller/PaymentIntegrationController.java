package com.clinicsalon.appointment.controller;

import com.clinicsalon.appointment.service.AppointmentPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Integração com Pagamentos", description = "API para integração com o serviço financeiro")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentIntegrationController {

    private final AppointmentPaymentService paymentService;

    @Operation(summary = "Gerar link de pagamento", description = "Gera um link de pagamento para um agendamento específico")
    @PostMapping("/generate-link/{appointmentId}")
    public ResponseEntity<Map<String, String>> generatePaymentLink(
            @Parameter(description = "ID do agendamento") @PathVariable Long appointmentId) {
        log.info("Solicitação para gerar link de pagamento para agendamento ID: {}", appointmentId);
        Map<String, String> paymentLink = paymentService.createPaymentLink(appointmentId);
        return ResponseEntity.ok(paymentLink);
    }

    @Operation(summary = "Verificar status de pagamento", description = "Verifica o status de pagamento de um agendamento específico")
    @GetMapping("/status/{appointmentId}")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(
            @Parameter(description = "ID do agendamento") @PathVariable Long appointmentId) {
        log.info("Verificando status de pagamento para agendamento ID: {}", appointmentId);
        Map<String, Object> paymentStatus = paymentService.getPaymentStatus(appointmentId);
        return ResponseEntity.ok(paymentStatus);
    }

    @Operation(summary = "Processar reembolso", description = "Processa um reembolso para um agendamento cancelado ou no-show")
    @PostMapping("/refund/{appointmentId}")
    public ResponseEntity<Map<String, Object>> processRefund(
            @Parameter(description = "ID do agendamento") @PathVariable Long appointmentId) {
        log.info("Solicitação de reembolso para agendamento ID: {}", appointmentId);
        Map<String, Object> refundResult = paymentService.processRefund(appointmentId);
        return ResponseEntity.ok(refundResult);
    }
}
