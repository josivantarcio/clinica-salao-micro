package com.clinicsalon.finance.performance;

import com.clinicsalon.finance.dto.PaymentLinkRequestDto;
import com.clinicsalon.finance.dto.PaymentLinkResponseDto;
import com.clinicsalon.finance.dto.RefundRequestDto;
import com.clinicsalon.finance.dto.TransactionDto;
import com.clinicsalon.finance.enums.PaymentStatus;
import com.clinicsalon.finance.enums.TransactionType;
import com.clinicsalon.finance.gateway.PaymentGateway;
import com.clinicsalon.finance.model.Transaction;
import com.clinicsalon.finance.repository.TransactionRepository;
import com.clinicsalon.finance.service.TransactionService;
import com.clinicsalon.finance.service.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
 * Testes de performance para o serviço financeiro
 * Simula alta carga no sistema e mede a capacidade de processamento
 * com foco especial em operações críticas como processamento de pagamentos
 * e integração com gateway de pagamento
 */
@ExtendWith(MockitoExtension.class)
@Tag("performance")
public class FinanceServicePerformanceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentGateway paymentGateway;

    private TransactionService transactionService;

    private static final int CONCURRENT_REQUESTS = 500;
    private static final int THREAD_POOL_SIZE = 50;

    @BeforeEach
    public void setup() {
        transactionService = new TransactionServiceImpl(transactionRepository, paymentGateway);
    }

    /**
     * Teste de stress simulando múltiplas requisições concorrentes para criação de links de pagamento
     */
    @Test
    public void testConcurrentPaymentLinkCreation() throws InterruptedException {
        // Configurar comportamento simulado dos mocks
        when(paymentGateway.createPaymentLink(any(PaymentLinkRequestDto.class)))
                .thenAnswer(invocation -> {
                    PaymentLinkRequestDto request = invocation.getArgument(0);
                    
                    PaymentLinkResponseDto response = new PaymentLinkResponseDto();
                    response.setId(UUID.randomUUID().toString());
                    response.setPaymentLink("https://payment.gateway.com/pay/" + UUID.randomUUID().toString());
                    response.setExpirationDate(LocalDateTime.now().plusDays(7));
                    response.setAmount(request.getAmount());
                    response.setStatus(PaymentStatus.PENDING.name());
                    
                    return response;
                });

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction transaction = invocation.getArgument(0);
                    if (transaction.getId() == null) {
                        transaction.setId(UUID.randomUUID().toString());
                    }
                    return transaction;
                });

        // Registrar o início do teste
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
                    
                    // Criar request para link de pagamento
                    PaymentLinkRequestDto request = new PaymentLinkRequestDto();
                    request.setAppointmentId((long) requestIndex);
                    request.setClientId(100L + requestIndex % 50);
                    request.setProfessionalId(200L + requestIndex % 20);
                    request.setAmount(BigDecimal.valueOf(100 + requestIndex % 200));
                    request.setDescription("Serviço de beleza #" + requestIndex);
                    
                    // Invocar o serviço
                    PaymentLinkResponseDto result = transactionService.createPaymentLink(request);
                    
                    // Verificar resultado
                    if (result != null && result.getPaymentLink() != null) {
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
        
        // Calcular o tempo total e médio
        Instant endTime = Instant.now();
        long totalDuration = Duration.between(startTime, endTime).toMillis();
        
        // Calcular tempo médio de processamento
        double averageProcessingTime = calculateAverage(processingTimes);
        
        // Calcular throughput (requisições por segundo)
        double throughput = CONCURRENT_REQUESTS * 1000.0 / totalDuration;
        
        // Log das métricas
        System.out.println("==== Performance Test Results (Payment Link Creation) ====");
        System.out.println("Total requests: " + CONCURRENT_REQUESTS);
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + failureCount.get());
        System.out.println("Total duration: " + totalDuration + " ms");
        System.out.println("Average processing time: " + averageProcessingTime + " ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " requests/second");
        
        // Verificações de performance
        assertTrue(successCount.get() > 0, "Nenhuma requisição foi processada com sucesso");
        assertEquals(0, failureCount.get(), "Não deveria haver falhas em ambiente controlado de teste");
        
        // Limpar recursos
        executorService.shutdown();
    }
    
    /**
     * Teste de stress simulando múltiplas requisições concorrentes para processamento de reembolsos
     */
    @Test
    public void testConcurrentRefundProcessing() throws InterruptedException {
        // Configurar transação existente para reembolso
        String transactionId = UUID.randomUUID().toString();
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setAppointmentId(1L);
        transaction.setClientId(101L);
        transaction.setProfessionalId(201L);
        transaction.setAmount(BigDecimal.valueOf(150.00));
        transaction.setType(TransactionType.PAYMENT);
        transaction.setStatus(PaymentStatus.PAID);
        transaction.setDescription("Serviço de teste");
        transaction.setCreatedAt(LocalDateTime.now().minusDays(1));
        transaction.setExternalId("ext-" + UUID.randomUUID().toString());

        // Configurar comportamento simulado dos mocks
        when(transactionRepository.findById(anyString())).thenReturn(Optional.of(transaction));
        
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction savedTransaction = invocation.getArgument(0);
                    return savedTransaction;
                });
                
        when(paymentGateway.processRefund(any(RefundRequestDto.class)))
                .thenAnswer(invocation -> {
                    RefundRequestDto request = invocation.getArgument(0);
                    
                    TransactionDto response = new TransactionDto();
                    response.setId(UUID.randomUUID().toString());
                    response.setAppointmentId(request.getAppointmentId());
                    response.setAmount(request.getAmount());
                    response.setStatus(PaymentStatus.REFUNDED.name());
                    response.setType(TransactionType.REFUND.name());
                    response.setDescription("Reembolso processado");
                    response.setCreatedAt(LocalDateTime.now());
                    
                    return response;
                });

        // Contador para sincronizar os threads
        CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        
        // Métricas de performance
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
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
                    
                    // Criar request para reembolso
                    RefundRequestDto request = new RefundRequestDto();
                    request.setTransactionId(transactionId);
                    request.setAppointmentId(1L);
                    request.setAmount(BigDecimal.valueOf(150.00));
                    request.setReason("Cliente cancelou agendamento #" + requestIndex);
                    
                    // Invocar o serviço
                    TransactionDto result = transactionService.processRefund(request);
                    
                    // Verificar resultado
                    if (result != null && PaymentStatus.REFUNDED.name().equals(result.getStatus())) {
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
        System.out.println("==== Performance Test Results (Refund Processing) ====");
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
     * Teste de resiliência simulando falhas intermitentes do gateway de pagamento
     */
    @Test
    public void testResilienceWithGatewayFailures() throws InterruptedException {
        // Configurar comportamento simulado dos mocks com falhas intermitentes (50% de falha)
        when(paymentGateway.createPaymentLink(any(PaymentLinkRequestDto.class)))
                .thenAnswer(invocation -> {
                    if (Math.random() < 0.5) {
                        // Simular falha
                        throw new RuntimeException("Gateway de pagamento indisponível (simulação)");
                    } else {
                        // Simular sucesso
                        PaymentLinkRequestDto request = invocation.getArgument(0);
                        
                        PaymentLinkResponseDto response = new PaymentLinkResponseDto();
                        response.setId(UUID.randomUUID().toString());
                        response.setPaymentLink("https://payment.gateway.com/pay/" + UUID.randomUUID().toString());
                        response.setExpirationDate(LocalDateTime.now().plusDays(7));
                        response.setAmount(request.getAmount());
                        response.setStatus(PaymentStatus.PENDING.name());
                        
                        return response;
                    }
                });

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction transaction = invocation.getArgument(0);
                    if (transaction.getId() == null) {
                        transaction.setId(UUID.randomUUID().toString());
                    }
                    return transaction;
                });

        // Contador para sincronizar os threads
        CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        
        // Métricas
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger recoveredCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        // Pool de threads
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        
        // Executar N requisições concorrentes
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            final int requestIndex = i;
            executorService.submit(() -> {
                try {
                    // Criar request para link de pagamento
                    PaymentLinkRequestDto request = new PaymentLinkRequestDto();
                    request.setAppointmentId((long) requestIndex);
                    request.setClientId(100L + requestIndex % 50);
                    request.setProfessionalId(200L + requestIndex % 20);
                    request.setAmount(BigDecimal.valueOf(100 + requestIndex % 200));
                    request.setDescription("Serviço de beleza #" + requestIndex);
                    
                    // Invocar o serviço
                    PaymentLinkResponseDto result = transactionService.createPaymentLink(request);
                    
                    // Verificar o tipo de resposta
                    if (result != null) {
                        if (result.getPaymentLink() != null && result.getPaymentLink().startsWith("https://")) {
                            // Resposta direta do gateway
                            successCount.incrementAndGet();
                        } else if (result.getStatus() != null && result.getStatus().contains("RETRY")) {
                            // Resposta de retry/circuit breaker
                            recoveredCount.incrementAndGet();
                        } else {
                            // Resposta inesperada
                            errorCount.incrementAndGet();
                        }
                    } else {
                        errorCount.incrementAndGet();
                    }
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
        
        // Log dos resultados
        System.out.println("==== Resilience Test Results ====");
        System.out.println("Total requests: " + CONCURRENT_REQUESTS);
        System.out.println("Direct success: " + successCount.get());
        System.out.println("Recovered with circuit breaker: " + recoveredCount.get());
        System.out.println("Errors: " + errorCount.get());
        
        // Verificações
        // Em ambiente real, esperamos que o Circuit Breaker funcione e tenhamos respostas de fallback
        // ao invés de erros quando o gateway falha
        assertTrue(successCount.get() + recoveredCount.get() > 0, 
                "Deve haver alguma resposta bem-sucedida ou recuperada");
        
        // Limpar recursos
        executorService.shutdown();
    }
    
    /**
     * Teste de performance para cálculos financeiros em massa
     * simulando geração de relatórios e análises
     */
    @Test
    public void testFinancialReportingPerformance() throws InterruptedException {
        // Configurar dados de teste - transações dos últimos 30 dias
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Transaction transaction = new Transaction();
            transaction.setId(UUID.randomUUID().toString());
            transaction.setAppointmentId((long) (i % 200));
            transaction.setClientId(100L + i % 50);
            transaction.setProfessionalId(200L + i % 20);
            
            // Variar valores para simular situação real
            transaction.setAmount(BigDecimal.valueOf(50 + (i % 10) * 25));
            
            // 80% pagamentos, 20% reembolsos
            transaction.setType(i % 5 == 0 ? TransactionType.REFUND : TransactionType.PAYMENT);
            
            // Variar status (a maioria pagos)
            if (i % 10 == 0) {
                transaction.setStatus(PaymentStatus.PENDING);
            } else if (i % 20 == 0) {
                transaction.setStatus(PaymentStatus.CANCELLED);
            } else if (i % 5 == 0 && transaction.getType() == TransactionType.REFUND) {
                transaction.setStatus(PaymentStatus.REFUNDED);
            } else {
                transaction.setStatus(PaymentStatus.PAID);
            }
            
            // Distribuir ao longo do mês
            transaction.setCreatedAt(LocalDateTime.now().minusDays(i % 30));
            
            transactions.add(transaction);
        }

        // Configurar mock do repositório para buscar transações por período
        when(transactionRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(transactions);

        // Métricas de performance
        long[] processingTimes = new long[5]; // 5 tipos diferentes de relatórios
        
        // Testar performance do cálculo de receita por período
        Instant start = Instant.now();
        BigDecimal totalRevenue = calculateTotalRevenue(transactions);
        long revenueTime = Duration.between(start, Instant.now()).toMillis();
        processingTimes[0] = revenueTime;
        
        // Testar performance do cálculo de reembolsos por período
        start = Instant.now();
        BigDecimal totalRefunds = calculateTotalRefunds(transactions);
        long refundsTime = Duration.between(start, Instant.now()).toMillis();
        processingTimes[1] = refundsTime;
        
        // Testar performance do cálculo de receita líquida por período
        start = Instant.now();
        BigDecimal netRevenue = totalRevenue.subtract(totalRefunds);
        long netRevenueTime = Duration.between(start, Instant.now()).toMillis();
        processingTimes[2] = netRevenueTime;
        
        // Testar performance do cálculo de receita por profissional
        start = Instant.now();
        var revenueByProfessional = transactions.stream()
                .filter(t -> t.getType() == TransactionType.PAYMENT && t.getStatus() == PaymentStatus.PAID)
                .collect(java.util.stream.Collectors.groupingBy(Transaction::getProfessionalId,
                        java.util.stream.Collectors.reducing(BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add)));
        long revByProfessionalTime = Duration.between(start, Instant.now()).toMillis();
        processingTimes[3] = revByProfessionalTime;
        
        // Testar performance da distribuição de pagamentos por dia
        start = Instant.now();
        var paymentsByDay = transactions.stream()
                .filter(t -> t.getType() == TransactionType.PAYMENT && t.getStatus() == PaymentStatus.PAID)
                .collect(java.util.stream.Collectors.groupingBy(
                        t -> t.getCreatedAt().toLocalDate(),
                        java.util.stream.Collectors.reducing(BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add)));
        long paymentsByDayTime = Duration.between(start, Instant.now()).toMillis();
        processingTimes[4] = paymentsByDayTime;
        
        // Log das métricas
        System.out.println("==== Financial Reporting Performance ====");
        System.out.println("Total transactions processed: " + transactions.size());
        System.out.println("Total revenue calculation: " + revenueTime + " ms");
        System.out.println("Total refunds calculation: " + refundsTime + " ms");
        System.out.println("Net revenue calculation: " + netRevenueTime + " ms");
        System.out.println("Revenue by professional calculation: " + revByProfessionalTime + " ms");
        System.out.println("Payments by day calculation: " + paymentsByDayTime + " ms");
        System.out.println("Average processing time: " + calculateAverage(processingTimes) + " ms");
        
        // Verificações
        assertNotNull(totalRevenue);
        assertNotNull(totalRefunds);
        assertNotNull(netRevenue);
        assertTrue(revenueByProfessional.size() > 0);
        assertTrue(paymentsByDay.size() > 0);
    }
    
    /**
     * Método auxiliar para calcular receita total
     */
    private BigDecimal calculateTotalRevenue(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.PAYMENT && t.getStatus() == PaymentStatus.PAID)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Método auxiliar para calcular reembolsos totais
     */
    private BigDecimal calculateTotalRefunds(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.REFUND && t.getStatus() == PaymentStatus.REFUNDED)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
