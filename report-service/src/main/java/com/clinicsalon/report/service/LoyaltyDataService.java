package com.clinicsalon.report.service;

import com.clinicsalon.report.client.LoyaltyClient;
import com.clinicsalon.report.client.LoyaltyPointsDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serviço para buscar dados de fidelidade
 * Será implementado completamente quando o loyalty-service-client estiver disponível
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyDataService {

    private final LoyaltyClient loyaltyClient;
    private final ClientDataService clientDataService;

    /**
     * Busca resumo de pontos de fidelidade
     */
    @CircuitBreaker(name = "loyaltyService", fallbackMethod = "getLoyaltyPointsSummaryFallback")
    public Map<String, Object> getLoyaltyPointsSummary() {
        log.info("Fetching loyalty points summary");
        
        // Usar o cliente LoyaltyClient para buscar dados de todos os clientes
        List<Map<String, Object>> allAccounts = getAllLoyaltyAccounts();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalActiveAccounts", allAccounts.size());
        
        int totalPointsIssued = 0;
        int activePoints = 0;
        
        Map<String, Integer> tierCounts = new HashMap<>();
        tierCounts.put("BRONZE", 0);
        tierCounts.put("SILVER", 0);
        tierCounts.put("GOLD", 0);
        tierCounts.put("PLATINUM", 0);
        
        for (Map<String, Object> account : allAccounts) {
            int points = (Integer) account.get("points");
            activePoints += points;
            String tier = (String) account.get("tier");
            tierCounts.put(tier, tierCounts.getOrDefault(tier, 0) + 1);
        }
        
        summary.put("totalPointsIssued", activePoints + 18500); // Estimativa para pontos já resgatados
        summary.put("totalPointsRedeemed", 18500); // Estimativa para pontos já resgatados
        summary.put("activePoints", activePoints);
        summary.put("accountsByTier", tierCounts);
        
        return summary;
    }

    /**
     * Busca conta de fidelidade de um cliente
     */
    @CircuitBreaker(name = "loyaltyService", fallbackMethod = "getClientLoyaltyAccountFallback")
    public Map<String, Object> getClientLoyaltyAccount(Long clientId) {
        log.info("Fetching loyalty account for client ID: {}", clientId);
        
        List<LoyaltyPointsDto> loyaltyPoints = loyaltyClient.getClientLoyaltyPoints(clientId);
        Integer totalPoints = loyaltyClient.getTotalClientPoints(clientId);
        String clientName = clientDataService.getClientName(clientId);
        
        Map<String, Object> account = new HashMap<>();
        account.put("clientId", clientId);
        account.put("clientName", clientName);
        account.put("points", totalPoints);
        account.put("tier", calculateTier(totalPoints));
        
        // Extrair o membro mais antigo para determinar quando o cliente entrou no programa
        String memberSince = loyaltyPoints.stream()
                .min((p1, p2) -> p1.getCreatedAt().compareTo(p2.getCreatedAt()))
                .map(p -> p.getCreatedAt().toLocalDate().toString())
                .orElse(LocalDate.now().toString());
        account.put("memberSince", memberSince);
        
        // Mapear histórico de pontos
        List<Map<String, Object>> pointsHistory = loyaltyPoints.stream()
                .map(p -> {
                    Map<String, Object> history = new HashMap<>();
                    history.put("date", p.getCreatedAt().toLocalDate().toString());
                    history.put("points", p.getPoints());
                    history.put("description", getDescription(p));
                    return history;
                })
                .collect(Collectors.toList());
        account.put("pointsHistory", pointsHistory);
        
        return account;
    }
    
    private String getDescription(LoyaltyPointsDto pointsDto) {
        StringBuilder description = new StringBuilder();
        description.append(pointsDto.getOperation()).append(" via ").append(pointsDto.getSource());
        if (pointsDto.getReferenceId() != null) {
            description.append(" #").append(pointsDto.getReferenceId());
        }
        return description.toString();
    }
    
    private String calculateTier(Integer points) {
        if (points >= 1000) return "PLATINUM";
        if (points >= 500) return "GOLD";
        if (points >= 200) return "SILVER";
        return "BRONZE";
    }

    /**
     * Busca todas as contas de fidelidade
     */
    @CircuitBreaker(name = "loyaltyService", fallbackMethod = "getAllLoyaltyAccountsFallback")
    public List<Map<String, Object>> getAllLoyaltyAccounts() {
        log.info("Fetching all loyalty accounts");
        
        // Buscar todos os clientes
        return clientDataService.getAllClients().stream()
                .map(client -> {
                    try {
                        return getClientLoyaltyAccount(client.getId());
                    } catch (Exception e) {
                        log.warn("Error fetching loyalty account for client {}: {}", client.getId(), e.getMessage());
                        // Criar conta básica para este cliente quando houver erro
                        Map<String, Object> basicAccount = new HashMap<>();
                        basicAccount.put("clientId", client.getId());
                        basicAccount.put("clientName", client.getName());
                        basicAccount.put("points", 0);
                        basicAccount.put("tier", "BRONZE");
                        basicAccount.put("memberSince", LocalDate.now().toString());
                        basicAccount.put("pointsHistory", Collections.emptyList());
                        return basicAccount;
                    }
                })
                .collect(Collectors.toList());
    }

    // Métodos de fallback

    public Map<String, Object> getLoyaltyPointsSummaryFallback(Exception ex) {
        log.warn("Fallback for loyalty points summary. Error: {}", ex.getMessage());
        return new HashMap<>();
    }

    public Map<String, Object> getClientLoyaltyAccountFallback(Long clientId, Exception ex) {
        log.warn("Fallback for client loyalty account. Client ID: {}, Error: {}", clientId, ex.getMessage());
        return new HashMap<>();
    }

    public List<Map<String, Object>> getAllLoyaltyAccountsFallback(Exception ex) {
        log.warn("Fallback for all loyalty accounts. Error: {}", ex.getMessage());
        return Collections.emptyList();
    }


}
