package com.clinicsalon.finance.controller;

import com.clinicsalon.finance.dto.TransactionRequest;
import com.clinicsalon.finance.dto.TransactionResponse;
import com.clinicsalon.finance.model.TransactionStatus;
import com.clinicsalon.finance.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transactions", description = "API para gerenciamento de transações financeiras")
public class TransactionController {

    private final TransactionService transactionService;
    
    @PostMapping
    @Operation(summary = "Criar uma nova transação financeira")
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        log.info("REST request to create transaction");
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "Listar todas as transações")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        log.info("REST request to get all transactions");
        List<TransactionResponse> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar transação por ID")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable UUID id) {
        log.info("REST request to get transaction by id: {}", id);
        TransactionResponse transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }
    
    @GetMapping("/client/{clientId}")
    @Operation(summary = "Buscar transações por ID do cliente")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByClientId(@PathVariable UUID clientId) {
        log.info("REST request to get transactions by client: {}", clientId);
        List<TransactionResponse> transactions = transactionService.getTransactionsByClientId(clientId);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/appointment/{appointmentId}")
    @Operation(summary = "Buscar transações por ID do agendamento")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAppointmentId(@PathVariable UUID appointmentId) {
        log.info("REST request to get transactions by appointment: {}", appointmentId);
        List<TransactionResponse> transactions = transactionService.getTransactionsByAppointmentId(appointmentId);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Buscar transações por status")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByStatus(@PathVariable TransactionStatus status) {
        log.info("REST request to get transactions by status: {}", status);
        List<TransactionResponse> transactions = transactionService.getTransactionsByStatus(status);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/date-range")
    @Operation(summary = "Buscar transações por período")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("REST request to get transactions between {} and {}", startDate, endDate);
        List<TransactionResponse> transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(transactions);
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status de uma transação")
    public ResponseEntity<TransactionResponse> updateTransactionStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> statusRequest) {
        try {
            TransactionStatus status = TransactionStatus.valueOf(statusRequest.get("status"));
            log.info("REST request to update transaction {} status to {}", id, status);
            TransactionResponse updated = transactionService.updateTransactionStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Invalid transaction status: {}", statusRequest.get("status"));
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir uma transação")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID id) {
        log.info("REST request to delete transaction: {}", id);
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/payment-link")
    @Operation(summary = "Gerar link de pagamento para uma transação")
    public ResponseEntity<Map<String, String>> generatePaymentLink(@PathVariable UUID id) {
        log.info("REST request to generate payment link for transaction: {}", id);
        String paymentLink = transactionService.generatePaymentLink(id);
        return ResponseEntity.ok(Map.of("paymentLink", paymentLink));
    }
    
    @GetMapping("/revenue")
    @Operation(summary = "Calcular receita total em um período")
    public ResponseEntity<Map<String, Double>> calculateRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("REST request to calculate revenue between {} and {}", startDate, endDate);
        Double revenue = transactionService.calculateRevenueBetweenDates(startDate, endDate);
        return ResponseEntity.ok(Map.of("totalRevenue", revenue));
    }
    
    @PostMapping("/{id}/process-payment")
    @Operation(summary = "Processar pagamento de uma transação")
    public ResponseEntity<TransactionResponse> processPayment(@PathVariable UUID id) {
        log.info("REST request to process payment for transaction: {}", id);
        TransactionResponse response = transactionService.processPayment(id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/refund")
    @Operation(summary = "Processar reembolso de uma transação")
    public ResponseEntity<TransactionResponse> processRefund(@PathVariable UUID id) {
        log.info("REST request to process refund for transaction: {}", id);
        TransactionResponse response = transactionService.processRefund(id);
        return ResponseEntity.ok(response);
    }
}
