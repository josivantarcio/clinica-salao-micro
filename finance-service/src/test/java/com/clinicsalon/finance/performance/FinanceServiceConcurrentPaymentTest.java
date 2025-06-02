package com.clinicsalon.finance.performance;

import com.clinicsalon.finance.client.AsaasGatewayClient;
import com.clinicsalon.finance.dto.PaymentDto;
import com.clinicsalon.finance.dto.PaymentLinkDto;
import com.clinicsalon.finance.dto.RefundDto;
import com.clinicsalon.finance.model.Payment;
import com.clinicsalon.finance.model.PaymentStatus;
import com.clinicsalon.finance.repository.PaymentRepository;
import com.clinicsalon.finance.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes de performance para processamento concorrente de pagamentos
 * Simula cenários de alta carga e verifica resiliência do serviço financeiro
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Tag("performance")
public class FinanceServiceConcurrentPaymentTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AsaasGatewayClient gatewayClient;

    private PaymentService paymentService;

    private static final int CONCURRENT_REQUESTS = 500;
    private static final int THREAD_POOL_SIZE = 50;
    private static final String PAYMENT_LINK_URL = "https://pagamento.clinicasalao.com/pay/";

    @BeforeEach
    public void setup() {
        paymentService = new PaymentService(paymentRepository, gatewayClient);
    }

    /**
     * Teste de stress para criação concorrente de links de pagamento
     * Simula múltiplas requisições simultâneas para criação de links
     */
    @Test
    public void testConcurrentPaymentLinkCreation() throws InterruptedException {
        // Configurar comportamento do repositório
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(UUID.randomUUID().toString());
            return payment;
        });

        // Configurar comportamento do gateway de pagamento
        when(gatewayClient.createPaymentLink(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            Map<String, String> response = new HashMap<>();
            response.put("id", UUID.randomUUID().toString());
            response.put("paymentLink", PAYMENT_LINK_URL + UUID.randomUUID().toString());
            response.put("status", "PENDING");
            return response;
        });

        // Métricas de performance
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger errorCount = new AtomicInteger(0);
        final long[] processingTimes = new long[CONCURRENT_REQUESTS];
        final CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // Registrar início do teste
        final Instant startTime = Instant.now();

        // Executar requisições concorrentes
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            final int index = i;
            executorService.submit(() -> {
                Instant requestStart = Instant.now();
                try {
                    // Criar DTO para pagamento
                    PaymentLinkDto paymentLinkDto = new PaymentLinkDto();
                    paymentLinkDto.setAppointmentId(1000L + index);
                    paymentLinkDto.setAmount(BigDecimal.valueOf(100.0 + (index % 50)));
                    paymentLinkDto.setClientName("Cliente " + index);
                    paymentLinkDto.setClientEmail("cliente" + index + "@example.com");
                    paymentLinkDto.setDescription("Agendamento #" + (1000 + index));

                    // Chamar serviço
                    Map<String, String> result = paymentService.createPaymentLink(paymentLinkDto);

                    // Verificar resultado
                    if (result != null && result.containsKey("paymentLink")) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("Erro na requisição #" + index + ": " + e.getMessage());
                } finally {
                    // Registrar tempo de processamento
                    processingTimes[index] = Duration.between(requestStart, Instant.now()).toMillis();
                    latch.countDown();
                }
            });
        }

        // Aguardar conclusão de todas as requisições ou timeout
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        
        // Registrar fim do teste
        Instant endTime = Instant.now();
        long totalDuration = Duration.between(startTime, endTime).toMillis();

        // Calcular métricas
        double averageTime = calculateAverage(processingTimes);
        long minTime = findMin(processingTimes);
        long maxTime = findMax(processingTimes);
        double throughput = (successCount.get() * 1000.0) / totalDuration;

        // Log dos resultados
        System.out.println("=== Teste de Criação Concorrente de Links de Pagamento ===");
        System.out.println("Total de requisições: " + CONCURRENT_REQUESTS);
        System.out.println("Sucesso: " + successCount.get());
        System.out.println("Erros: " + errorCount.get());
        System.out.println("Tempo total: " + totalDuration + "ms");
        System.out.println("Tempo médio por requisição: " + averageTime + "ms");
        System.out.println("Tempo mínimo: " + minTime + "ms");
        System.out.println("Tempo máximo: " + maxTime + "ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " req/s");

        // Asserções
        assertTrue(completed, "O teste não foi concluído no tempo esperado");
        assertTrue(successCount.get() > 0, "Nenhuma requisição foi bem-sucedida");
        assertTrue(errorCount.get() < CONCURRENT_REQUESTS * 0.1, "Taxa de erro superior a 10%");

        // Cleanup
        executorService.shutdown();
    }

    /**
     * Teste de resiliência simulando falhas intermitentes no gateway de pagamento
     */
    @Test
    public void testPaymentGatewayResilience() throws InterruptedException {
        // Configurar comportamento do repositório
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(UUID.randomUUID().toString());
            return payment;
        });

        // Configurar falhas intermitentes no gateway (50% de falha)
        when(gatewayClient.createPaymentLink(any(Payment.class))).thenAnswer(invocation -> {
            // Simular latência variável
            Thread.sleep((long) (Math.random() * 50));
            
            // Simular falha aleatória
            if (Math.random() < 0.5) {
                throw new RuntimeException("Gateway indisponível (simulação)");
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("id", UUID.randomUUID().toString());
            response.put("paymentLink", PAYMENT_LINK_URL + UUID.randomUUID().toString());
            response.put("status", "PENDING");
            return response;
        });

        // Métricas
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger fallbackCount = new AtomicInteger(0);
        final AtomicInteger errorCount = new AtomicInteger(0);
        final CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // Executar requisições concorrentes
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    // Criar DTO para pagamento
                    PaymentLinkDto paymentLinkDto = new PaymentLinkDto();
                    paymentLinkDto.setAppointmentId(2000L + index);
                    paymentLinkDto.setAmount(BigDecimal.valueOf(100.0 + (index % 50)));
                    paymentLinkDto.setClientName("Cliente " + index);
                    paymentLinkDto.setClientEmail("cliente" + index + "@example.com");
                    paymentLinkDto.setDescription("Agendamento #" + (2000 + index));

                    // Chamar serviço
                    Map<String, String> result = paymentService.createPaymentLink(paymentLinkDto);

                    // Verificar tipo de resposta
                    if (result != null) {
                        if (result.containsKey("paymentLink") && result.get("paymentLink").startsWith("https://")) {
                            // Resposta direta do gateway
                            successCount.incrementAndGet();
                        } else if (result.containsKey("message") && result.get("message").contains("fallback")) {
                            // Resposta de fallback
                            fallbackCount.incrementAndGet();
                        } else {
                            // Resposta inesperada
                            errorCount.incrementAndGet();
                        }
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("Erro na requisição #" + index + ": " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // Aguardar conclusão de todas as requisições ou timeout
        boolean completed = latch.await(30, TimeUnit.SECONDS);

        // Log dos resultados
        System.out.println("=== Teste de Resiliência com Falhas no Gateway ===");
        System.out.println("Total de requisições: " + CONCURRENT_REQUESTS);
        System.out.println("Sucesso direto: " + successCount.get());
        System.out.println("Respostas fallback: " + fallbackCount.get());
        System.out.println("Erros: " + errorCount.get());

        // Asserções
        assertTrue(completed, "O teste não foi concluído no tempo esperado");
        assertTrue(successCount.get() + fallbackCount.get() > 0, "Nenhuma requisição foi bem-sucedida ou tratada por fallback");

        // Cleanup
        executorService.shutdown();
    }

    /**
     * Teste de stress para processamento concorrente de reembolsos
     */
    @Test
    public void testConcurrentRefundProcessing() throws InterruptedException {
        // Configurar payments existentes
        when(paymentRepository.findByAppointmentId(anyLong())).thenAnswer(invocation -> {
            Long appointmentId = invocation.getArgument(0);
            Payment payment = new Payment();
            payment.setId(UUID.randomUUID().toString());
            payment.setAppointmentId(appointmentId);
            payment.setAmount(BigDecimal.valueOf(150.0));
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaymentDate(LocalDateTime.now().minusDays(1));
            payment.setClientName("Cliente Teste");
            payment.setClientEmail("cliente@example.com");
            payment.setGatewayPaymentId("gateway-" + UUID.randomUUID().toString());
            return Optional.of(payment);
        });

        // Configurar comportamento do gateway para reembolsos
        when(gatewayClient.processRefund(anyString())).thenAnswer(invocation -> {
            // Simular latência variável
            Thread.sleep((long) (Math.random() * 100));
            
            Map<String, String> response = new HashMap<>();
            response.put("refundId", "ref-" + UUID.randomUUID().toString());
            response.put("status", "REFUNDED");
            return response;
        });

        // Métricas de performance
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger errorCount = new AtomicInteger(0);
        final long[] processingTimes = new long[CONCURRENT_REQUESTS];
        final CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // Registrar início do teste
        final Instant startTime = Instant.now();

        // Executar requisições concorrentes
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            final int index = i;
            executorService.submit(() -> {
                Instant requestStart = Instant.now();
                try {
                    // Criar DTO para reembolso
                    RefundDto refundDto = new RefundDto();
                    refundDto.setAppointmentId(3000L + index);
                    refundDto.setReason("Cancelamento solicitado pelo cliente");

                    // Chamar serviço
                    Map<String, String> result = paymentService.processRefund(refundDto);

                    // Verificar resultado
                    if (result != null && result.containsKey("status") && "REFUNDED".equals(result.get("status"))) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("Erro na requisição #" + index + ": " + e.getMessage());
                } finally {
                    // Registrar tempo de processamento
                    processingTimes[index] = Duration.between(requestStart, Instant.now()).toMillis();
                    latch.countDown();
                }
            });
        }

        // Aguardar conclusão de todas as requisições ou timeout
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        
        // Registrar fim do teste
        Instant endTime = Instant.now();
        long totalDuration = Duration.between(startTime, endTime).toMillis();

        // Calcular métricas
        double averageTime = calculateAverage(processingTimes);
        long minTime = findMin(processingTimes);
        long maxTime = findMax(processingTimes);
        double throughput = (successCount.get() * 1000.0) / totalDuration;

        // Log dos resultados
        System.out.println("=== Teste de Processamento Concorrente de Reembolsos ===");
        System.out.println("Total de requisições: " + CONCURRENT_REQUESTS);
        System.out.println("Sucesso: " + successCount.get());
        System.out.println("Erros: " + errorCount.get());
        System.out.println("Tempo total: " + totalDuration + "ms");
        System.out.println("Tempo médio por requisição: " + averageTime + "ms");
        System.out.println("Tempo mínimo: " + minTime + "ms");
        System.out.println("Tempo máximo: " + maxTime + "ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " req/s");

        // Asserções
        assertTrue(completed, "O teste não foi concluído no tempo esperado");
        assertTrue(successCount.get() > 0, "Nenhuma requisição foi bem-sucedida");
        assertTrue(errorCount.get() < CONCURRENT_REQUESTS * 0.1, "Taxa de erro superior a 10%");

        // Cleanup
        executorService.shutdown();
    }

    /**
     * Teste de stress para consulta concorrente de status de pagamentos
     */
    @Test
    public void testConcurrentPaymentStatusChecking() throws InterruptedException {
        // Configurar consulta de pagamentos
        when(paymentRepository.findByAppointmentId(anyLong())).thenAnswer(invocation -> {
            Long appointmentId = invocation.getArgument(0);
            Payment payment = new Payment();
            payment.setId(UUID.randomUUID().toString());
            payment.setAppointmentId(appointmentId);
            payment.setAmount(BigDecimal.valueOf(150.0));
            
            // Simular diferentes status para diversificar resultados
            int statusIndex = (int)(appointmentId % 4);
            switch (statusIndex) {
                case 0:
                    payment.setStatus(PaymentStatus.PENDING);
                    break;
                case 1:
                    payment.setStatus(PaymentStatus.PAID);
                    payment.setPaymentDate(LocalDateTime.now().minusHours(2));
                    break;
                case 2:
                    payment.setStatus(PaymentStatus.REFUNDED);
                    payment.setPaymentDate(LocalDateTime.now().minusDays(1));
                    payment.setRefundDate(LocalDateTime.now().minusHours(1));
                    break;
                case 3:
                    payment.setStatus(PaymentStatus.CANCELLED);
                    break;
            }
            
            payment.setClientName("Cliente Teste");
            payment.setClientEmail("cliente@example.com");
            payment.setGatewayPaymentId("gateway-" + UUID.randomUUID().toString());
            return Optional.of(payment);
        });

        // Configurar consulta no gateway
        when(gatewayClient.getPaymentStatus(anyString())).thenAnswer(invocation -> {
            // Simular latência
            Thread.sleep((long) (Math.random() * 30));
            
            String gatewayId = invocation.getArgument(0);
            int statusIndex = Math.abs(gatewayId.hashCode() % 4);
            
            Map<String, String> response = new HashMap<>();
            switch (statusIndex) {
                case 0:
                    response.put("status", "PENDING");
                    break;
                case 1:
                    response.put("status", "PAID");
                    break;
                case 2:
                    response.put("status", "REFUNDED");
                    break;
                case 3:
                    response.put("status", "CANCELLED");
                    break;
            }
            return response;
        });

        // Métricas de performance
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger errorCount = new AtomicInteger(0);
        final long[] processingTimes = new long[CONCURRENT_REQUESTS];
        final CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // Registrar início do teste
        final Instant startTime = Instant.now();

        // Executar requisições concorrentes
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            final int index = i;
            executorService.submit(() -> {
                Instant requestStart = Instant.now();
                try {
                    // Chamar serviço para verificar status
                    Long appointmentId = 4000L + index;
                    Map<String, String> result = paymentService.getPaymentStatus(appointmentId);

                    // Verificar resultado
                    if (result != null && result.containsKey("status")) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("Erro na requisição #" + index + ": " + e.getMessage());
                } finally {
                    // Registrar tempo de processamento
                    processingTimes[index] = Duration.between(requestStart, Instant.now()).toMillis();
                    latch.countDown();
                }
            });
        }

        // Aguardar conclusão de todas as requisições ou timeout
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        
        // Registrar fim do teste
        Instant endTime = Instant.now();
        long totalDuration = Duration.between(startTime, endTime).toMillis();

        // Calcular métricas
        double averageTime = calculateAverage(processingTimes);
        long minTime = findMin(processingTimes);
        long maxTime = findMax(processingTimes);
        double throughput = (successCount.get() * 1000.0) / totalDuration;

        // Log dos resultados
        System.out.println("=== Teste de Consulta Concorrente de Status de Pagamentos ===");
        System.out.println("Total de requisições: " + CONCURRENT_REQUESTS);
        System.out.println("Sucesso: " + successCount.get());
        System.out.println("Erros: " + errorCount.get());
        System.out.println("Tempo total: " + totalDuration + "ms");
        System.out.println("Tempo médio por requisição: " + averageTime + "ms");
        System.out.println("Tempo mínimo: " + minTime + "ms");
        System.out.println("Tempo máximo: " + maxTime + "ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " req/s");

        // Asserções
        assertTrue(completed, "O teste não foi concluído no tempo esperado");
        assertTrue(successCount.get() > 0, "Nenhuma requisição foi bem-sucedida");
        assertTrue(errorCount.get() < CONCURRENT_REQUESTS * 0.1, "Taxa de erro superior a 10%");

        // Cleanup
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

    /**
     * Encontra o valor mínimo em um array
     */
    private long findMin(long[] values) {
        long min = Long.MAX_VALUE;
        for (long value : values) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    /**
     * Encontra o valor máximo em um array
     */
    private long findMax(long[] values) {
        long max = Long.MIN_VALUE;
        for (long value : values) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }
}
