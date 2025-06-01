package com.clinicsalon.loyalty.controller;

import com.clinicsalon.loyalty.dto.LoyaltyAccountRequest;
import com.clinicsalon.loyalty.dto.LoyaltyAccountResponse;
import com.clinicsalon.loyalty.model.LoyaltyTier;
import com.clinicsalon.loyalty.service.LoyaltyAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loyalty/accounts")
@RequiredArgsConstructor
@Tag(name = "Loyalty Account API", description = "API para gerenciamento de contas de fidelidade")
public class LoyaltyAccountController {

    private final LoyaltyAccountService loyaltyAccountService;

    @PostMapping
    @Operation(summary = "Criar uma nova conta de fidelidade")
    public ResponseEntity<LoyaltyAccountResponse> createLoyaltyAccount(
            @Valid @RequestBody LoyaltyAccountRequest request) {
        return new ResponseEntity<>(loyaltyAccountService.createLoyaltyAccount(request), HttpStatus.CREATED);
    }

    @GetMapping("/{clientId}")
    @Operation(summary = "Buscar conta de fidelidade pelo ID do cliente")
    public ResponseEntity<LoyaltyAccountResponse> getLoyaltyAccountByClientId(
            @PathVariable Long clientId) {
        return ResponseEntity.ok(loyaltyAccountService.getLoyaltyAccountByClientId(clientId));
    }

    @GetMapping
    @Operation(summary = "Listar todas as contas de fidelidade")
    public ResponseEntity<List<LoyaltyAccountResponse>> getAllLoyaltyAccounts() {
        return ResponseEntity.ok(loyaltyAccountService.getAllLoyaltyAccounts());
    }

    @PatchMapping("/{clientId}/tier")
    @Operation(summary = "Atualizar o nível de fidelidade de um cliente")
    public ResponseEntity<LoyaltyAccountResponse> updateTier(
            @PathVariable Long clientId,
            @RequestParam LoyaltyTier tier) {
        return ResponseEntity.ok(loyaltyAccountService.updateTier(clientId, tier));
    }

    @PatchMapping("/{clientId}/points")
    @Operation(summary = "Atualizar o saldo de pontos de um cliente")
    public ResponseEntity<LoyaltyAccountResponse> updatePointsBalance(
            @PathVariable Long clientId,
            @RequestParam Integer pointsDelta) {
        return ResponseEntity.ok(loyaltyAccountService.updatePointsBalance(clientId, pointsDelta));
    }
}
