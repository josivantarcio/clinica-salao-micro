package com.clinicsalon.loyalty.performance;

import com.clinicsalon.loyalty.dto.LoyaltyAccountDto;
import com.clinicsalon.loyalty.dto.PointsTransactionDto;
import com.clinicsalon.loyalty.enums.PointsTransactionStatus;
import com.clinicsalon.loyalty.enums.PointsTransactionType;
import com.clinicsalon.loyalty.model.LoyaltyAccount;
import com.clinicsalon.loyalty.repository.LoyaltyAccountRepository;
import com.clinicsalon.loyalty.service.ClientLookupService;
import com.clinicsalon.loyalty.service.LoyaltyAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Testes de performance para o serviço de fidelidade
 * Simula alta carga de transações de pontos de fidelidade e mede
 * a capacidade de processamento do serviço sob essas condições
 */
@ExtendWith(MockitoExtension.class)
@Tag("performance")
public class LoyaltyServicePerformanceTest {

    @Mock
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Mock
    private ClientLookupService clientLookupService;

    private LoyaltyAccountService loyaltyAccountService;

    private static final int CONCURRENT_REQUESTS = 500;
    private static final int THREAD_POOL_SIZE = 50;

    @BeforeEach
    public void setup() {
        loyaltyAccountService = new LoyaltyAccountService(loyaltyAccountRepository, clientLookupService);
    }

    /**
     * Teste de stress simulando múltiplas requisições concorrentes para acumular pontos
     * em contas de fidelidade
     */
    @Test
    public void testConcurrentPointsAccumulation() throws InterruptedException {
        // Configurar comportamento simulado para busca de conta existente
        when(loyaltyAccountRepository.findByClientId(anyLong()))
                .thenAnswer(invocation -> {
                    Long clientId = invocation.getArgument(0);
                    LoyaltyAccount account = new LoyaltyAccount();
                    account.setId(UUID.randomUUID().toString());
                    account.setClientId(clientId);
                    account.setCurrentPoints(1000);
                    account.setTotalAccumulatedPoints(5000);
                    account.setCreatedAt(LocalDateTime.now().minusMonths(6));
                    account.setUpdatedAt(LocalDateTime.now().minusDays(7));
                    return Optional.of(account);
                });

        // Configurar comportamento simulado para salvar a conta atualizada
        when(loyaltyAccountRepository.save(any(LoyaltyAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Registrar início do teste
        Instant startTime = Instant.now();
        
        // Contador para sincronizar os threads
        CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        
        // Métricas de performance
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        long[] processingTimes = new long[CONCURRENT_REQUESTS];
        
        // Pool de threads para execução concorrente
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        
        // Executar N requisições concorrentes
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            final int requestIndex = i;
            executorService.submit(() -> {
                try {
                    // Registrar início do processamento desta requisição
                    Instant requestStart = Instant.now();
                    
                    // Criar transação de pontos
                    PointsTransactionDto transactionDto = new PointsTransactionDto();
                    transactionDto.setClientId(100L + requestIndex % 50);
                    transactionDto.setAppointmentId(1000L + requestIndex);
                    transactionDto.setPoints(100 + requestIndex % 100);
                    transactionDto.setType(PointsTransactionType.EARN);
                    transactionDto.setDescription("Serviço realizado #" + requestIndex);
                    
                    // Processar transação de pontos
                    LoyaltyAccountDto result = loyaltyAccountService.processPointsTransaction(transactionDto);
                    
                    // Verificar resultado
                    if (result != null && result.getCurrentPoints() > 0) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                    
                    // Calcular tempo de processamento desta requisição
                    long processingTime = Duration.between(requestStart, Instant.now()).toMillis();
                    processingTimes[requestIndex] = processingTime;
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.err.println("Erro ao processar requisição #" + requestIndex + ": " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Aguardar até que todas as requisições sejam processadas ou timeout
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "Nem todas as requisições foram processadas no tempo esperado");
        
        // Calcular o tempo total
        Instant endTime = Instant.now();
        long totalDuration = Duration.between(startTime, endTime).toMillis();
        
        // Calcular tempo médio de processamento
        double averageProcessingTime = calculateAverage(processingTimes);
        
        // Calcular throughput (requisições por segundo)
        double throughput = CONCURRENT_REQUESTS * 1000.0 / totalDuration;
        
        // Log das métricas
        System.out.println("==== Performance Test Results (Points Accumulation) ====");
        System.out.println("Total requests: " + CONCURRENT_REQUESTS);
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + failureCount.get());
        System.out.println("Total duration: " + totalDuration + " ms");
        System.out.println("Average processing time: " + averageProcessingTime + " ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " requests/second");
        
        // Verificações
        assertEquals(CONCURRENT_REQUESTS, successCount.get(), "Todas as requisições devem ser bem-sucedidas");
        assertEquals(0, failureCount.get(), "Não deve haver falhas");
        
        // Limpar recursos
        executorService.shutdown();
    }
    
    /**
     * Teste de stress simulando múltiplas requisições concorrentes para resgatar pontos
     */
    @Test
    public void testConcurrentPointsRedemption() throws InterruptedException {
        // Configurar comportamento simulado para busca de conta existente
        when(loyaltyAccountRepository.findByClientId(anyLong()))
                .thenAnswer(invocation -> {
                    Long clientId = invocation.getArgument(0);
                    LoyaltyAccount account = new LoyaltyAccount();
                    account.setId(UUID.randomUUID().toString());
                    account.setClientId(clientId);
                    account.setCurrentPoints(2000);  // Saldo inicial alto para permitir resgates
                    account.setTotalAccumulatedPoints(8000);
                    account.setCreatedAt(LocalDateTime.now().minusMonths(6));
                    account.setUpdatedAt(LocalDateTime.now().minusDays(3));
                    return Optional.of(account);
                });

        // Configurar comportamento simulado para salvar a conta atualizada
        when(loyaltyAccountRepository.save(any(LoyaltyAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Contador para sincronizar os threads
        CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        
        // Métricas de performance
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger insufficientPointsCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        long[] processingTimes = new long[CONCURRENT_REQUESTS];
        
        // Pool de threads para execução concorrente
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        
        // Registrar início do teste
        Instant startTime = Instant.now();
        
        // Executar N requisições concorrentes
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            final int requestIndex = i;
            executorService.submit(() -> {
                try {
                    // Registrar início do processamento desta requisição
                    Instant requestStart = Instant.now();
                    
                    // Criar transação de resgate de pontos
                    PointsTransactionDto transactionDto = new PointsTransactionDto();
                    transactionDto.setClientId(100L + requestIndex % 50);
                    transactionDto.setAppointmentId(2000L + requestIndex);
                    
                    // Alguns resgates com valores que excedem o saldo para testar validação
                    int pointsToRedeem = (requestIndex % 10 == 0) ? 3000 : (200 + requestIndex % 500);
                    transactionDto.setPoints(pointsToRedeem);
                    
                    transactionDto.setType(PointsTransactionType.REDEEM);
                    transactionDto.setDescription("Desconto aplicado #" + requestIndex);
                    
                    // Processar transação de pontos
                    LoyaltyAccountDto result = loyaltyAccountService.processPointsTransaction(transactionDto);
                    
                    // Verificar resultado
                    if (result != null) {
                        if (result.getStatus() == PointsTransactionStatus.COMPLETED) {
                            successCount.incrementAndGet();
                        } else if (result.getStatus() == PointsTransactionStatus.INSUFFICIENT_POINTS) {
                            insufficientPointsCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                    } else {
                        errorCount.incrementAndGet();
                    }
                    
                    // Calcular tempo de processamento desta requisição
                    long processingTime = Duration.between(requestStart, Instant.now()).toMillis();
                    processingTimes[requestIndex] = processingTime;
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Aguardar até que todas as requisições sejam processadas ou timeout
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "Nem todas as requisições foram processadas no tempo esperado");
        
        // Calcular o tempo total
        Instant endTime = Instant.now();
        long totalDuration = Duration.between(startTime, endTime).toMillis();
        
        // Calcular tempo médio de processamento
        double averageProcessingTime = calculateAverage(processingTimes);
        
        // Calcular throughput (requisições por segundo)
        double throughput = CONCURRENT_REQUESTS * 1000.0 / totalDuration;
        
        // Log das métricas
        System.out.println("==== Performance Test Results (Points Redemption) ====");
        System.out.println("Total requests: " + CONCURRENT_REQUESTS);
        System.out.println("Successful redemptions: " + successCount.get());
        System.out.println("Insufficient points: " + insufficientPointsCount.get());
        System.out.println("Errors: " + errorCount.get());
        System.out.println("Total duration: " + totalDuration + " ms");
        System.out.println("Average processing time: " + averageProcessingTime + " ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " requests/second");
        
        // Verificações
        assertTrue(successCount.get() > 0, "Deve haver resgates bem-sucedidos");
        assertEquals(CONCURRENT_REQUESTS, successCount.get() + insufficientPointsCount.get(), 
                "Todas as requisições devem ser processadas (sucesso ou insuficiência de pontos)");
        assertEquals(0, errorCount.get(), "Não deve haver erros");
        
        // Limpar recursos
        executorService.shutdown();
    }
    
    /**
     * Teste de resiliência simulando falhas intermitentes no repositório
     */
    @Test
    public void testResilienceWithRepositoryFailures() throws InterruptedException {
        // Configurar comportamento simulado com falhas intermitentes
        AtomicInteger callCount = new AtomicInteger(0);
        
        when(loyaltyAccountRepository.findByClientId(anyLong()))
                .thenAnswer(invocation -> {
                    // Falhar em 30% das chamadas
                    if (callCount.incrementAndGet() % 3 == 0) {
                        throw new RuntimeException("Simulação de falha no banco de dados");
                    }
                    
                    Long clientId = invocation.getArgument(0);
                    LoyaltyAccount account = new LoyaltyAccount();
                    account.setId(UUID.randomUUID().toString());
                    account.setClientId(clientId);
                    account.setCurrentPoints(1500);
                    account.setTotalAccumulatedPoints(6000);
                    account.setCreatedAt(LocalDateTime.now().minusMonths(3));
                    account.setUpdatedAt(LocalDateTime.now().minusDays(5));
                    return Optional.of(account);
                });

        when(loyaltyAccountRepository.save(any(LoyaltyAccount.class)))
                .thenAnswer(invocation -> {
                    // Falhar em 30% das chamadas
                    if (callCount.incrementAndGet() % 3 == 0) {
                        throw new RuntimeException("Simulação de falha no banco de dados");
                    }
                    return invocation.getArgument(0);
                });

        // Contador para sincronizar os threads
        CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        
        // Métricas
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger fallbackCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        // Pool de threads
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        
        // Executar N requisições concorrentes
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            final int requestIndex = i;
            executorService.submit(() -> {
                try {
                    // Criar transação de pontos
                    PointsTransactionDto transactionDto = new PointsTransactionDto();
                    transactionDto.setClientId(100L + requestIndex % 50);
                    transactionDto.setAppointmentId(3000L + requestIndex);
                    transactionDto.setPoints(100 + requestIndex % 100);
                    transactionDto.setType(PointsTransactionType.EARN);
                    transactionDto.setDescription("Teste de resiliência #" + requestIndex);
                    
                    // Tentar processar transação, pode falhar devido às falhas simuladas
                    try {
                        LoyaltyAccountDto result = loyaltyAccountService.processPointsTransaction(transactionDto);
                        
                        if (result != null) {
                            if (result.getStatus() == PointsTransactionStatus.COMPLETED) {
                                successCount.incrementAndGet();
                            } else if (result.getStatus() == PointsTransactionStatus.PENDING) {
                                // Resposta de fallback
                                fallbackCount.incrementAndGet();
                            } else {
                                errorCount.incrementAndGet();
                            }
                        } else {
                            errorCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // Este é um teste de resiliência, então esperamos algumas falhas
                        // O importante é que algumas requisições tenham sucesso
                        errorCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Aguardar até que todas as requisições sejam processadas ou timeout
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "Nem todas as requisições foram processadas no tempo esperado");
        
        // Log dos resultados
        System.out.println("==== Resilience Test Results ====");
        System.out.println("Total requests: " + CONCURRENT_REQUESTS);
        System.out.println("Successful: " + successCount.get());
        System.out.println("Fallback responses: " + fallbackCount.get());
        System.out.println("Errors: " + errorCount.get());
        
        // Verificações
        // Devido às falhas simuladas, nem todas as requisições terão sucesso
        // mas deve haver um número significativo de sucessos ou fallbacks
        assertTrue(successCount.get() > 0, "Deve haver algumas requisições bem-sucedidas");
        
        // Limpar recursos
        executorService.shutdown();
    }
    
    /**
     * Teste de performance para busca de contas de fidelidade por cliente
     */
    @Test
    public void testAccountLookupPerformance() throws InterruptedException {
        // Configurar comportamento simulado para busca de conta
        when(loyaltyAccountRepository.findByClientId(anyLong()))
                .thenAnswer(invocation -> {
                    Long clientId = invocation.getArgument(0);
                    LoyaltyAccount account = new LoyaltyAccount();
                    account.setId(UUID.randomUUID().toString());
                    account.setClientId(clientId);
                    account.setCurrentPoints(1200);
                    account.setTotalAccumulatedPoints(5500);
                    account.setCreatedAt(LocalDateTime.now().minusMonths(4));
                    account.setUpdatedAt(LocalDateTime.now().minusDays(2));
                    return Optional.of(account);
                });

        // Registrar início do teste
        Instant startTime = Instant.now();
        
        // Contador para sincronizar os threads
        CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        
        // Métricas de performance
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        long[] processingTimes = new long[CONCURRENT_REQUESTS];
        
        // Pool de threads para execução concorrente
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        
        // Executar N requisições concorrentes
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            final int requestIndex = i;
            executorService.submit(() -> {
                try {
                    // Registrar início do processamento desta requisição
                    Instant requestStart = Instant.now();
                    
                    // Buscar conta de fidelidade
                    Long clientId = 100L + requestIndex % 50;
                    LoyaltyAccountDto result = loyaltyAccountService.getAccountByClientId(clientId);
                    
                    // Verificar resultado
                    if (result != null && result.getClientId().equals(clientId)) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                    
                    // Calcular tempo de processamento desta requisição
                    long processingTime = Duration.between(requestStart, Instant.now()).toMillis();
                    processingTimes[requestIndex] = processingTime;
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Aguardar até que todas as requisições sejam processadas ou timeout
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "Nem todas as requisições foram processadas no tempo esperado");
        
        // Calcular o tempo total
        Instant endTime = Instant.now();
        long totalDuration = Duration.between(startTime, endTime).toMillis();
        
        // Calcular tempo médio de processamento
        double averageProcessingTime = calculateAverage(processingTimes);
        
        // Calcular throughput (requisições por segundo)
        double throughput = CONCURRENT_REQUESTS * 1000.0 / totalDuration;
        
        // Log das métricas
        System.out.println("==== Performance Test Results (Account Lookup) ====");
        System.out.println("Total requests: " + CONCURRENT_REQUESTS);
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + failureCount.get());
        System.out.println("Total duration: " + totalDuration + " ms");
        System.out.println("Average processing time: " + averageProcessingTime + " ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " requests/second");
        
        // Verificações
        assertEquals(CONCURRENT_REQUESTS, successCount.get(), "Todas as buscas devem ser bem-sucedidas");
        assertEquals(0, failureCount.get(), "Não deve haver falhas");
        
        // Limpar recursos
        executorService.shutdown();
    }
    
    /**
     * Calcula a média dos valores em um array
     */
    private double calculateAverage(long[] values) {
        long sum = 0;
        for (long value : values) {
            sum += value;
        }
        return (double) sum / values.length;
    }
}
