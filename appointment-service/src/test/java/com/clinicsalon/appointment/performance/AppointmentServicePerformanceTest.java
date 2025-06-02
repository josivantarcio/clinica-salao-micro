package com.clinicsalon.appointment.performance;

import com.clinicsalon.appointment.client.FinanceServiceClient;
import com.clinicsalon.appointment.client.LoyaltyServiceClient;
import com.clinicsalon.appointment.model.Appointment;
import com.clinicsalon.appointment.model.AppointmentStatus;
import com.clinicsalon.appointment.model.PaymentStatus;
import com.clinicsalon.appointment.repository.AppointmentRepository;
import com.clinicsalon.appointment.service.AppointmentPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes de performance para o serviço de agendamento
 * Simula carga alta no sistema e mede a capacidade de processamento
 * principalmente nas integrações com finance-service e loyalty-service
 */
@ExtendWith(MockitoExtension.class)
@Tag("performance")
public class AppointmentServicePerformanceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private FinanceServiceClient financeServiceClient;

    @Mock
    private LoyaltyServiceClient loyaltyServiceClient;

    private AppointmentPaymentService appointmentPaymentService;

    private static final int CONCURRENT_REQUESTS = 500;
    private static final int THREAD_POOL_SIZE = 50;
    private static final String PAYMENT_LINK = "https://payment.gateway.com/pay/";

    @BeforeEach
    public void setup() {
        appointmentPaymentService = new AppointmentPaymentService(
                appointmentRepository, financeServiceClient, loyaltyServiceClient);
    }

    /**
     * Teste de stress simulando múltiplas requisições concorrentes para
     * criação de links de pagamento
     */
    @Test
    public void testConcurrentPaymentLinkCreation() throws InterruptedException {
        // Configurar comportamento simulado dos mocks
        Appointment testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setStatus(AppointmentStatus.CONFIRMED);
        testAppointment.setPaymentStatus(PaymentStatus.PENDING);
        testAppointment.setPrice(BigDecimal.valueOf(150.00));
        testAppointment.setClientId(101L);
        testAppointment.setProfessionalId(201L);
        testAppointment.setDateTime(LocalDateTime.now().plusDays(1));

        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));
        
        Map<String, String> paymentResponse = new HashMap<>();
        paymentResponse.put("paymentLink", PAYMENT_LINK);
        when(financeServiceClient.createPaymentLink(anyLong(), anyDouble(), any(), any(), any()))
                .thenReturn(paymentResponse);

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
                    
                    // Invocar o serviço
                    Map<String, String> result = appointmentPaymentService.createPaymentLink(1L);
                    
                    // Verificar resultado
                    if (result != null && result.containsKey("paymentLink")) {
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
        
        // Verificar que os métodos foram chamados o número correto de vezes
        verify(appointmentRepository, times(CONCURRENT_REQUESTS)).findById(anyLong());
        verify(financeServiceClient, times(CONCURRENT_REQUESTS)).createPaymentLink(
                anyLong(), anyDouble(), any(), any(), any());
        
        // Limpar recursos
        executorService.shutdown();
    }
    
    /**
     * Teste de stress simulando múltiplas requisições concorrentes para
     * verificação de status de pagamento
     */
    @Test
    public void testConcurrentPaymentStatusCheck() throws InterruptedException {
        // Configurar comportamento simulado dos mocks
        Appointment testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setStatus(AppointmentStatus.CONFIRMED);
        testAppointment.setPaymentStatus(PaymentStatus.PENDING);
        testAppointment.setPrice(BigDecimal.valueOf(150.00));
        testAppointment.setClientId(101L);
        testAppointment.setProfessionalId(201L);
        testAppointment.setDateTime(LocalDateTime.now().plusDays(1));

        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));
        
        Map<String, String> statusResponse = new HashMap<>();
        statusResponse.put("status", "PAID");
        when(financeServiceClient.getPaymentStatusByAppointmentId(anyLong()))
                .thenReturn(statusResponse);

        // Contador para sincronizar os threads
        CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        
        // Métricas de performance
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        // Simular concorrência alta com múltiplos threads
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        
        // Registrar o início do teste
        Instant startTime = Instant.now();
        
        // Executar N requisições concorrentes
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            executorService.submit(() -> {
                try {
                    // Invocar o serviço
                    Map<String, String> result = appointmentPaymentService.getPaymentStatus(1L);
                    
                    // Verificar resultado
                    if (result != null && result.containsKey("status")) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
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
        
        // Calcular throughput (requisições por segundo)
        double throughput = CONCURRENT_REQUESTS * 1000.0 / totalDuration;
        
        // Log das métricas
        System.out.println("==== Performance Test Results (Payment Status Check) ====");
        System.out.println("Total requests: " + CONCURRENT_REQUESTS);
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + failureCount.get());
        System.out.println("Total duration: " + totalDuration + " ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " requests/second");
        
        // Verificações
        assertEquals(CONCURRENT_REQUESTS, successCount.get(), "Todas as requisições devem ser bem-sucedidas");
        assertEquals(0, failureCount.get(), "Não deve haver falhas");
        
        // Verificar que os métodos foram chamados o número correto de vezes
        verify(appointmentRepository, times(CONCURRENT_REQUESTS)).findById(anyLong());
        verify(financeServiceClient, times(CONCURRENT_REQUESTS)).getPaymentStatusByAppointmentId(anyLong());
        
        // Limpar recursos
        executorService.shutdown();
    }
    
    /**
     * Teste de resiliência simulando falhas intermitentes do finance-service
     */
    @Test
    public void testResilienceWithFinanceServiceFailures() throws InterruptedException {
        // Configurar comportamento simulado dos mocks
        Appointment testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setStatus(AppointmentStatus.CONFIRMED);
        testAppointment.setPaymentStatus(PaymentStatus.PENDING);
        testAppointment.setPrice(BigDecimal.valueOf(150.00));
        testAppointment.setClientId(101L);
        testAppointment.setProfessionalId(201L);
        testAppointment.setDateTime(LocalDateTime.now().plusDays(1));

        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));
        
        // Simular falhas intermitentes no finance-service (50% de falha)
        when(financeServiceClient.createPaymentLink(anyLong(), anyDouble(), any(), any(), any()))
                .thenAnswer(invocation -> {
                    if (Math.random() < 0.5) {
                        // Simular falha
                        throw new RuntimeException("Serviço indisponível (simulação)");
                    } else {
                        // Simular sucesso
                        Map<String, String> response = new HashMap<>();
                        response.put("paymentLink", PAYMENT_LINK + UUID.randomUUID().toString());
                        return response;
                    }
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
            executorService.submit(() -> {
                try {
                    // Invocar o serviço
                    Map<String, String> result = appointmentPaymentService.createPaymentLink(1L);
                    
                    // Verificar o tipo de resposta
                    if (result != null) {
                        if (result.containsKey("paymentLink") && result.get("paymentLink").startsWith("https://")) {
                            // Resposta direta do serviço
                            successCount.incrementAndGet();
                        } else if (result.containsKey("message") && result.get("message").contains("fallback")) {
                            // Resposta do fallback
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
        System.out.println("Fallback responses: " + fallbackCount.get());
        System.out.println("Errors: " + errorCount.get());
        
        // Verificações
        // Em ambiente real, esperamos que o Circuit Breaker funcione e tenhamos respostas de fallback
        // ao invés de erros quando o serviço falha
        assertTrue(successCount.get() + fallbackCount.get() > 0, 
                "Deve haver alguma resposta bem-sucedida ou de fallback");
        
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
