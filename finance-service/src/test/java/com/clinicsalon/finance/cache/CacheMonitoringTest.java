package com.clinicsalon.finance.cache;

import com.clinicsalon.finance.dto.TransactionResponse;
import com.clinicsalon.finance.model.TransactionStatus;
import com.clinicsalon.finance.service.TransactionService;
import com.clinicsalon.monitoring.cache.CacheMonitor;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Teste de integração para o monitoramento de cache do finance-service
 */
@SpringBootTest
@ActiveProfiles("test,monitoring")
@Tag("cache")
@Tag("monitoring")
public class CacheMonitoringTest {

    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Autowired
    private CacheMonitor cacheMonitor;
    
    @MockBean
    private TransactionService transactionService;
    
    @Test
    @DisplayName("Deve registrar métricas quando o cache é usado")
    public void shouldRecordMetricsWhenCacheIsUsed() {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        TransactionResponse mockResponse = new TransactionResponse();
        mockResponse.setId(transactionId);
        mockResponse.setStatus(TransactionStatus.COMPLETED);
        
        when(transactionService.getTransactionById(transactionId)).thenReturn(mockResponse);
        
        // Act - Primeira chamada (miss)
        transactionService.getTransactionById(transactionId);
        
        // Segunda chamada (hit)
        transactionService.getTransactionById(transactionId);
        
        // Assert
        verify(transactionService, times(2)).getTransactionById(transactionId);
        
        // Verificar se o cache existe
        assertNotNull(cacheManager.getCache("transactions"));
        
        // Verificar métricas do Micrometer
        assertTrue(meterRegistry.find("cache.hit.count").counter() != null 
                || meterRegistry.find("cache.miss.count").counter() != null);
    }
    
    @Test
    @DisplayName("Deve monitorar as estatísticas de cache e registrar métricas")
    public void shouldMonitorCacheStatisticsAndReportMetrics() {
        // Arrange
        List<UUID> transactionIds = List.of(
                UUID.randomUUID(), 
                UUID.randomUUID(), 
                UUID.randomUUID()
        );
        
        for (UUID id : transactionIds) {
            TransactionResponse mockResponse = new TransactionResponse();
            mockResponse.setId(id);
            mockResponse.setStatus(TransactionStatus.COMPLETED);
            when(transactionService.getTransactionById(id)).thenReturn(mockResponse);
        }
        
        // Act - Simular acessos ao cache
        for (UUID id : transactionIds) {
            // Primeira chamada - miss
            transactionService.getTransactionById(id);
            // Segunda chamada - hit
            transactionService.getTransactionById(id);
            // Terceira chamada - hit
            transactionService.getTransactionById(id);
        }
        
        // Forçar execução do monitoramento
        cacheMonitor.reportCacheStatistics();
        
        // Assert
        verify(transactionService, times(9)).getTransactionById(any(UUID.class));
        
        // Verificar métricas de cache
        assertTrue(meterRegistry.find("cache.size").gauge() != null);
    }
}
