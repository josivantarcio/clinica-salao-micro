package com.clinicsalon.loyalty.controller;

import com.clinicsalon.loyalty.dto.LoyaltyTransactionRequest;
import com.clinicsalon.loyalty.dto.LoyaltyTransactionResponse;
import com.clinicsalon.loyalty.service.LoyaltyTransactionService;
import io.swagger.v3.oas.annotations.Operation;
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

@RestController
@RequestMapping("/api/v1/loyalty/transactions")
@RequiredArgsConstructor
@Tag(name = "Loyalty Transaction API", description = "API para gerenciamento de transações de pontos de fidelidade")
public class LoyaltyTransactionController {

    private final LoyaltyTransactionService transactionService;

    @PostMapping
    @Operation(summary = "Criar uma nova transação de fidelidade")
    public ResponseEntity<LoyaltyTransactionResponse> createTransaction(
            @Valid @RequestBody LoyaltyTransactionRequest request) {
        return new ResponseEntity<>(transactionService.createTransaction(request), HttpStatus.CREATED);
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Buscar transações de fidelidade por ID do cliente")
    public ResponseEntity<Page<LoyaltyTransactionResponse>> getTransactionsByClientId(
            @PathVariable Long clientId,
            Pageable pageable) {
        return ResponseEntity.ok(transactionService.getTransactionsByClientId(clientId, pageable));
    }

    @GetMapping("/client/{clientId}/date-range")
    @Operation(summary = "Buscar transações de fidelidade por ID do cliente e intervalo de datas")
    public ResponseEntity<List<LoyaltyTransactionResponse>> getTransactionsByClientIdAndDateRange(
            @PathVariable Long clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(transactionService.getTransactionsByClientIdAndDateRange(clientId, startDate, endDate));
    }

    @PostMapping("/process-expired")
    @Operation(summary = "Processar pontos expirados")
    public ResponseEntity<Void> processExpiredPoints() {
        transactionService.processExpiredPoints();
        return ResponseEntity.ok().build();
    }
}
