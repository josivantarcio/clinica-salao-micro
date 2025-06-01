package com.clinicsalon.loyalty.service;

import com.clinicsalon.loyalty.dto.LoyaltyAccountRequest;
import com.clinicsalon.loyalty.dto.LoyaltyAccountResponse;
import com.clinicsalon.loyalty.exception.ResourceNotFoundException;
import com.clinicsalon.loyalty.model.LoyaltyAccount;
import com.clinicsalon.loyalty.model.LoyaltyTier;
import com.clinicsalon.loyalty.repository.LoyaltyAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyAccountService {

    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final ClientLookupService clientLookupService;

    @Transactional
    public LoyaltyAccountResponse createLoyaltyAccount(LoyaltyAccountRequest request) {
        log.info("Creating loyalty account for client ID: {}", request.getClientId());
        
        // Verificar se o cliente existe
        String clientName = clientLookupService.getClientName(request.getClientId());
        
        // Verificar se já existe uma conta para este cliente
        if (loyaltyAccountRepository.findByClientId(request.getClientId()).isPresent()) {
            throw new IllegalStateException("Loyalty account already exists for client ID: " + request.getClientId());
        }
        
        // Criar a nova conta de fidelidade
        LoyaltyAccount account = LoyaltyAccount.builder()
                .clientId(request.getClientId())
                .pointsBalance(request.getInitialPoints() != null ? request.getInitialPoints() : 0)
                .lifetimePoints(request.getInitialPoints() != null ? request.getInitialPoints() : 0)
                .tier(LoyaltyTier.BRONZE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        LoyaltyAccount savedAccount = loyaltyAccountRepository.save(account);
        
        // Retornar a resposta com os detalhes do cliente
        return buildLoyaltyAccountResponse(savedAccount, clientName);
    }
    
    @Transactional(readOnly = true)
    public LoyaltyAccountResponse getLoyaltyAccountByClientId(Long clientId) {
        log.info("Fetching loyalty account for client ID: {}", clientId);
        
        LoyaltyAccount account = loyaltyAccountRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Loyalty account not found for client ID: " + clientId));
        
        String clientName = clientLookupService.getClientName(clientId);
        
        return buildLoyaltyAccountResponse(account, clientName);
    }
    
    @Transactional(readOnly = true)
    public List<LoyaltyAccountResponse> getAllLoyaltyAccounts() {
        log.info("Fetching all loyalty accounts");
        
        List<LoyaltyAccount> accounts = loyaltyAccountRepository.findAll();
        
        return accounts.stream()
                .map(account -> {
                    String clientName = clientLookupService.getClientName(account.getClientId());
                    return buildLoyaltyAccountResponse(account, clientName);
                })
                .collect(Collectors.toList());
    }
    
    @Transactional
    public LoyaltyAccountResponse updateTier(Long clientId, LoyaltyTier newTier) {
        log.info("Updating tier for client ID: {} to {}", clientId, newTier);
        
        LoyaltyAccount account = loyaltyAccountRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Loyalty account not found for client ID: " + clientId));
        
        account.setTier(newTier);
        account.setUpdatedAt(LocalDateTime.now());
        
        LoyaltyAccount savedAccount = loyaltyAccountRepository.save(account);
        String clientName = clientLookupService.getClientName(clientId);
        
        return buildLoyaltyAccountResponse(savedAccount, clientName);
    }
    
    @Transactional
    public LoyaltyAccountResponse updatePointsBalance(Long clientId, Integer pointsDelta) {
        log.info("Updating points balance for client ID: {} by {}", clientId, pointsDelta);
        
        LoyaltyAccount account = loyaltyAccountRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Loyalty account not found for client ID: " + clientId));
        
        // Atualizar o saldo de pontos
        Integer newPointsBalance = account.getPointsBalance() + pointsDelta;
        if (newPointsBalance < 0) {
            newPointsBalance = 0;
        }
        account.setPointsBalance(newPointsBalance);
        
        // Se estamos adicionando pontos, atualizar o total de pontos da vida
        if (pointsDelta > 0) {
            account.setLifetimePoints(account.getLifetimePoints() + pointsDelta);
            
            // Verificar e atualizar o tier com base nos pontos acumulados
            updateTierBasedOnLifetimePoints(account);
        }
        
        account.setUpdatedAt(LocalDateTime.now());
        
        LoyaltyAccount savedAccount = loyaltyAccountRepository.save(account);
        String clientName = clientLookupService.getClientName(clientId);
        
        return buildLoyaltyAccountResponse(savedAccount, clientName);
    }
    
    private void updateTierBasedOnLifetimePoints(LoyaltyAccount account) {
        if (account.getLifetimePoints() >= 10000) {
            account.setTier(LoyaltyTier.PLATINUM);
        } else if (account.getLifetimePoints() >= 5000) {
            account.setTier(LoyaltyTier.GOLD);
        } else if (account.getLifetimePoints() >= 2000) {
            account.setTier(LoyaltyTier.SILVER);
        }
    }
    
    private LoyaltyAccountResponse buildLoyaltyAccountResponse(LoyaltyAccount account, String clientName) {
        return LoyaltyAccountResponse.builder()
                .id(account.getId())
                .clientId(account.getClientId())
                .clientName(clientName)
                .pointsBalance(account.getPointsBalance())
                .lifetimePoints(account.getLifetimePoints())
                .tier(account.getTier())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
