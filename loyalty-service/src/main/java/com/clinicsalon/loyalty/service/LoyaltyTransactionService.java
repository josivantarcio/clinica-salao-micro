package com.clinicsalon.loyalty.service;

import com.clinicsalon.loyalty.dto.LoyaltyTransactionRequest;
import com.clinicsalon.loyalty.dto.LoyaltyTransactionResponse;
import com.clinicsalon.loyalty.exception.ResourceNotFoundException;
import com.clinicsalon.loyalty.model.LoyaltyAccount;
import com.clinicsalon.loyalty.model.LoyaltyTransaction;
import com.clinicsalon.loyalty.repository.LoyaltyAccountRepository;
import com.clinicsalon.loyalty.repository.LoyaltyTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyTransactionService {

    private final LoyaltyTransactionRepository transactionRepository;
    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyAccountService accountService;
    private final ClientLookupService clientLookupService;

    @Transactional
    public LoyaltyTransactionResponse createTransaction(LoyaltyTransactionRequest request) {
        log.info("Creating loyalty transaction for client ID: {} of type: {}", 
                 request.getClientId(), request.getType());
        
        // Buscar a conta de fidelidade
        LoyaltyAccount account = accountRepository.findByClientId(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Loyalty account not found for client ID: " + request.getClientId()));
        
        // Criar a transação
        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .loyaltyAccount(account)
                .type(request.getType())
                .points(request.getPoints())
                .description(request.getDescription())
                .referenceId(request.getReferenceId())
                .transactionDate(LocalDateTime.now())
                .expiryDate(request.getExpiryDate())
                .build();
        
        // Salvar a transação
        LoyaltyTransaction savedTransaction = transactionRepository.save(transaction);
        
        // Atualizar o saldo de pontos da conta
        Integer pointsDelta;
        switch (request.getType()) {
            case EARNED:
                pointsDelta = request.getPoints();
                break;
            case REDEEMED:
            case EXPIRED:
                pointsDelta = -request.getPoints();
                break;
            case ADJUSTED:
                pointsDelta = request.getPoints(); // Pode ser positivo ou negativo
                break;
            default:
                pointsDelta = 0;
        }
        
        accountService.updatePointsBalance(request.getClientId(), pointsDelta);
        
        return buildTransactionResponse(savedTransaction, account.getClientId());
    }
    
    @Transactional(readOnly = true)
    public Page<LoyaltyTransactionResponse> getTransactionsByClientId(Long clientId, Pageable pageable) {
        log.info("Fetching transactions for client ID: {}", clientId);
        
        LoyaltyAccount account = accountRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Loyalty account not found for client ID: " + clientId));
        
        Page<LoyaltyTransaction> transactions = transactionRepository.findByLoyaltyAccount(account, pageable);
        
        return transactions.map(transaction -> buildTransactionResponse(transaction, clientId));
    }
    
    @Transactional(readOnly = true)
    public List<LoyaltyTransactionResponse> getTransactionsByClientIdAndDateRange(
            Long clientId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching transactions for client ID: {} between {} and {}", 
                 clientId, startDate, endDate);
        
        LoyaltyAccount account = accountRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Loyalty account not found for client ID: " + clientId));
        
        List<LoyaltyTransaction> transactions = 
                transactionRepository.findByLoyaltyAccountAndTransactionDateBetween(account, startDate, endDate);
        
        return transactions.stream()
                .map(transaction -> buildTransactionResponse(transaction, clientId))
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void processExpiredPoints() {
        log.info("Processing expired points");
        
        LocalDateTime now = LocalDateTime.now();
        List<LoyaltyTransaction> expiredTransactions = transactionRepository.findByExpiryDateBefore(now);
        
        for (LoyaltyTransaction transaction : expiredTransactions) {
            if (transaction.getType() == LoyaltyTransaction.TransactionType.EARNED) {
                // Criar uma transação de expiração
                LoyaltyTransaction expirationTransaction = LoyaltyTransaction.builder()
                        .loyaltyAccount(transaction.getLoyaltyAccount())
                        .type(LoyaltyTransaction.TransactionType.EXPIRED)
                        .points(transaction.getPoints())
                        .description("Pontos expirados da transação #" + transaction.getId())
                        .transactionDate(now)
                        .build();
                
                transactionRepository.save(expirationTransaction);
                
                // Atualizar o saldo de pontos
                Long clientId = transaction.getLoyaltyAccount().getClientId();
                accountService.updatePointsBalance(clientId, -transaction.getPoints());
            }
        }
    }
    
    private LoyaltyTransactionResponse buildTransactionResponse(LoyaltyTransaction transaction, Long clientId) {
        String clientName = clientLookupService.getClientName(clientId);
        
        return LoyaltyTransactionResponse.builder()
                .id(transaction.getId())
                .accountId(transaction.getLoyaltyAccount().getId())
                .clientId(clientId)
                .clientName(clientName)
                .type(transaction.getType())
                .points(transaction.getPoints())
                .description(transaction.getDescription())
                .referenceId(transaction.getReferenceId())
                .transactionDate(transaction.getTransactionDate())
                .expiryDate(transaction.getExpiryDate())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
