package com.clinicsalon.finance.client;

import com.clinicsalon.finance.client.dto.TransactionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Interface para comunicação com o serviço financeiro
 */
@FeignClient(name = "finance-service", path = "/api/v1/transactions")
public interface FinanceClient {
    
    /**
     * Busca uma transação por ID
     */
    @GetMapping("/{id}")
    ResponseEntity<TransactionDTO> getTransactionById(@PathVariable("id") UUID id);
    
    /**
     * Busca todas as transações de um cliente
     */
    @GetMapping("/client/{clientId}")
    ResponseEntity<List<TransactionDTO>> getTransactionsByClientId(@PathVariable("clientId") UUID clientId);
    
    /**
     * Busca todas as transações de um agendamento
     */
    @GetMapping("/appointment/{appointmentId}")
    ResponseEntity<List<TransactionDTO>> getTransactionsByAppointmentId(@PathVariable("appointmentId") UUID appointmentId);
    
    /**
     * Gera link de pagamento para uma transação
     */
    @PostMapping("/{id}/payment-link")
    ResponseEntity<String> generatePaymentLink(@PathVariable("id") UUID id);
    
    /**
     * Processa um pagamento para uma transação
     */
    @PostMapping("/{id}/process-payment")
    ResponseEntity<TransactionDTO> processPayment(@PathVariable("id") UUID id);
    
    /**
     * Processa um reembolso para uma transação
     */
    @PostMapping("/{id}/process-refund")
    ResponseEntity<TransactionDTO> processRefund(@PathVariable("id") UUID id);
    
    /**
     * Calcula a receita em um período
     */
    @GetMapping("/revenue")
    ResponseEntity<Double> calculateRevenue(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate);
}
