package com.clinicsalon.report.service;

import com.clinicsalon.report.client.LoyaltyClient;
import com.clinicsalon.report.client.LoyaltyPointsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoyaltyDataServiceTest {

    @Mock
    private LoyaltyClient loyaltyClient;
    
    @Mock
    private ClientDataService clientDataService;

    @InjectMocks
    private LoyaltyDataService loyaltyDataService;

    private final Long clientId = 1L;
    private List<LoyaltyPointsDto> loyaltyPointsList;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.now().minusDays(30);
        endDate = LocalDate.now();
        
        loyaltyPointsList = Arrays.asList(
            LoyaltyPointsDto.builder()
                .id(1L)
                .clientId(clientId)
                .points(100)
                .description("Serviço de corte de cabelo")
                .createdAt(LocalDateTime.now().minusDays(20))
                .build(),
            LoyaltyPointsDto.builder()
                .id(2L)
                .clientId(clientId)
                .points(50)
                .description("Serviço de manicure")
                .createdAt(LocalDateTime.now().minusDays(10))
                .build()
        );
    }

    @Test
    void getLoyaltyPoints_Success() {
        // Arrange
        when(loyaltyClient.getLoyaltyPointsByClient(clientId)).thenReturn(loyaltyPointsList);
        when(clientDataService.getClientName(clientId)).thenReturn("João Silva");

        // Act
        List<Map<String, Object>> result = loyaltyDataService.getLoyaltyPoints(clientId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(100, result.get(0).get("points"));
        assertEquals("Serviço de corte de cabelo", result.get(0).get("description"));
        assertEquals("João Silva", result.get(0).get("clientName"));
        
        verify(loyaltyClient, times(1)).getLoyaltyPointsByClient(clientId);
        verify(clientDataService, times(1)).getClientName(clientId);
    }

    @Test
    void getLoyaltyPointsByDateRange_Success() {
        // Arrange
        when(loyaltyClient.getLoyaltyPointsByDateRange(clientId, startDate, endDate)).thenReturn(loyaltyPointsList);
        when(clientDataService.getClientName(clientId)).thenReturn("João Silva");

        // Act
        List<Map<String, Object>> result = loyaltyDataService.getLoyaltyPointsByDateRange(clientId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(100, result.get(0).get("points"));
        assertEquals("João Silva", result.get(0).get("clientName"));
        
        verify(loyaltyClient, times(1)).getLoyaltyPointsByDateRange(clientId, startDate, endDate);
    }

    @Test
    void getLoyaltyAccount_Success() {
        // Arrange
        int totalPoints = 500;
        when(loyaltyClient.getTotalLoyaltyPoints(clientId)).thenReturn(totalPoints);
        when(clientDataService.getClientName(clientId)).thenReturn("João Silva");

        // Act
        Map<String, Object> result = loyaltyDataService.getLoyaltyAccount(clientId);

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.get("clientId"));
        assertEquals("João Silva", result.get("clientName"));
        assertEquals(totalPoints, result.get("totalPoints"));
        assertEquals("OURO", result.get("tier")); // 500 pontos = tier OURO
        
        verify(loyaltyClient, times(1)).getTotalLoyaltyPoints(clientId);
        verify(clientDataService, times(1)).getClientName(clientId);
    }

    @Test
    void getLoyaltyAccountSummary_Success() {
        // Arrange
        Map<Long, Integer> clientPointsMap = new HashMap<>();
        clientPointsMap.put(1L, 500); // OURO
        clientPointsMap.put(2L, 100); // BRONZE
        clientPointsMap.put(3L, 300); // PRATA
        
        when(clientDataService.getClientName(1L)).thenReturn("João Silva");
        when(clientDataService.getClientName(2L)).thenReturn("Maria Oliveira");
        when(clientDataService.getClientName(3L)).thenReturn("Pedro Santos");
        
        when(loyaltyClient.getAllClientsTotalPoints()).thenReturn(clientPointsMap);

        // Act
        Map<String, Object> result = loyaltyDataService.getLoyaltyAccountSummary();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.get("totalClients"));
        assertEquals(900, result.get("totalPoints"));
        
        // Verificar as contagens por tier
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tierDistribution = (List<Map<String, Object>>) result.get("tierDistribution");
        assertNotNull(tierDistribution);
        assertEquals(3, tierDistribution.size());
        
        // Encontrar e verificar as distribuições específicas
        Optional<Map<String, Object>> bronze = tierDistribution.stream()
            .filter(map -> "BRONZE".equals(map.get("tier")))
            .findFirst();
        assertTrue(bronze.isPresent());
        assertEquals(1, bronze.get().get("count"));
        
        Optional<Map<String, Object>> prata = tierDistribution.stream()
            .filter(map -> "PRATA".equals(map.get("tier")))
            .findFirst();
        assertTrue(prata.isPresent());
        assertEquals(1, prata.get().get("count"));
        
        Optional<Map<String, Object>> ouro = tierDistribution.stream()
            .filter(map -> "OURO".equals(map.get("tier")))
            .findFirst();
        assertTrue(ouro.isPresent());
        assertEquals(1, ouro.get().get("count"));
        
        verify(loyaltyClient, times(1)).getAllClientsTotalPoints();
    }

    @Test
    void determineTier_Bronze() {
        assertEquals("BRONZE", loyaltyDataService.determineTier(50));
        assertEquals("BRONZE", loyaltyDataService.determineTier(199));
    }

    @Test
    void determineTier_Prata() {
        assertEquals("PRATA", loyaltyDataService.determineTier(200));
        assertEquals("PRATA", loyaltyDataService.determineTier(399));
    }

    @Test
    void determineTier_Ouro() {
        assertEquals("OURO", loyaltyDataService.determineTier(400));
        assertEquals("OURO", loyaltyDataService.determineTier(1000));
    }

    @Test
    void testFallbackMethods() {
        // Arrange
        Exception exception = new RuntimeException("Service unavailable");

        // Act & Assert for getLoyaltyPointsFallback
        List<Map<String, Object>> pointsFallback = loyaltyDataService.getLoyaltyPointsFallback(clientId, exception);
        assertNotNull(pointsFallback);
        assertTrue(pointsFallback.isEmpty());

        // Act & Assert for getLoyaltyPointsByDateRangeFallback
        List<Map<String, Object>> dateRangeFallback = 
            loyaltyDataService.getLoyaltyPointsByDateRangeFallback(clientId, startDate, endDate, exception);
        assertNotNull(dateRangeFallback);
        assertTrue(dateRangeFallback.isEmpty());

        // Act & Assert for getLoyaltyAccountFallback
        Map<String, Object> accountFallback = loyaltyDataService.getLoyaltyAccountFallback(clientId, exception);
        assertNotNull(accountFallback);
        assertEquals(clientId, accountFallback.get("clientId"));
        assertEquals(0, accountFallback.get("totalPoints"));
        assertEquals("BRONZE", accountFallback.get("tier"));

        // Act & Assert for getLoyaltyAccountSummaryFallback
        Map<String, Object> summaryFallback = loyaltyDataService.getLoyaltyAccountSummaryFallback(exception);
        assertNotNull(summaryFallback);
        assertEquals(0, summaryFallback.get("totalClients"));
        assertEquals(0, summaryFallback.get("totalPoints"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tierDistribution = (List<Map<String, Object>>) summaryFallback.get("tierDistribution");
        assertNotNull(tierDistribution);
        assertEquals(3, tierDistribution.size()); // Deve ter os 3 tiers mesmo vazio
    }
}
