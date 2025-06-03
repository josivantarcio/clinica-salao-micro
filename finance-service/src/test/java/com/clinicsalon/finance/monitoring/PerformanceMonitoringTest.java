package com.clinicsalon.finance.monitoring;

import com.clinicsalon.finance.dto.TransactionRequest;
import com.clinicsalon.finance.model.PaymentMethod;
import com.clinicsalon.finance.model.TransactionType;
import com.clinicsalon.finance.service.TransactionService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de integração para verificar o monitoramento de desempenho 
 * usando os aspectos definidos no módulo monitoring-commons
 */
@SpringBootTest
@ActiveProfiles("test,monitoring")
@Tag("monitoring")
@Tag("performance")
public class PerformanceMonitoringTest {

    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Test
    @DisplayName("Deve registrar métricas de desempenho para métodos anotados")
    public void shouldRecordPerformanceMetricsForAnnotatedMethods() throws Exception {
        // Arrange
        UUID appointmentId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        
        TransactionRequest request = new TransactionRequest();
        request.setAppointmentId(appointmentId);
        request.setClientId(clientId);
        request.setAmount(150.0);
        request.setType(TransactionType.PAYMENT);
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        request.setDescription("Pagamento de serviço");
        
        // Act - Executar o método anotado com @MonitorPerformance
        transactionService.createTransaction(request);
        
        // Assert - Verificar se as métricas foram registradas
        Timer timer = meterRegistry.find("method.execution.time")
                .tag("class", "TransactionServiceImpl")
                .tag("method", "createTransaction")
                .timer();
                
        assertNotNull(timer, "Timer de execução do método deve estar registrado");
        assertTrue(timer.count() > 0, "Timer deve ter registrado pelo menos uma execução");
    }
    
    @Test
    @DisplayName("Deve monitorar desempenho para múltiplas execuções concorrentes")
    public void shouldMonitorPerformanceForConcurrentExecutions() throws Exception {
        // Arrange
        int numExecutions = 10;
        ExecutorService executor = Executors.newFixedThreadPool(5);
        AtomicInteger counter = new AtomicInteger(0);
        
        // Act - Executar vários métodos de serviço em paralelo
        for (int i = 0; i < numExecutions; i++) {
            executor.submit(() -> {
                UUID id = UUID.randomUUID();
                try {
                    // Acessar métodos monitorados
                    transactionService.getTransactionById(id);
                } catch (Exception e) {
                    // Ignorar exceções de teste (IDs inexistentes)
                }
                counter.incrementAndGet();
            });
        }
        
        // Aguardar conclusão
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        // Assert - Verificar métricas de desempenho
        assertTrue(counter.get() == numExecutions, "Todas as execuções devem ser concluídas");
        
        // Verificar métricas de erro (devem existir devido a IDs não encontrados)
        assertTrue(
            meterRegistry.find("method.execution.errors")
                .tag("class", "TransactionServiceImpl")
                .tag("method", "getTransactionById")
                .counter() != null,
            "Contador de erros deve ser registrado para o método"
        );
    }
}
