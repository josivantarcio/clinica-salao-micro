package com.clinicsalon.report.performance;

import com.clinicsalon.report.client.AppointmentClient;
import com.clinicsalon.report.client.ClientClient;
import com.clinicsalon.report.client.LoyaltyClient;
import com.clinicsalon.report.client.ProfessionalClient;
import com.clinicsalon.report.client.ProfessionalDto;
import com.clinicsalon.report.dto.ReportRequest;
import com.clinicsalon.report.dto.ReportResponse;
import com.clinicsalon.report.dto.ReportType;
import com.clinicsalon.report.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Testes de performance para o serviço de relatórios
 * Simula alta carga no sistema e mede a capacidade de processamento sob estresse
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Tag("performance")
public class ReportServicePerformanceTest {

    @Autowired
    private ReportService reportService;

    @MockBean
    private ProfessionalClient professionalClient;

    @MockBean
    private ClientClient clientClient;

    @MockBean
    private LoyaltyClient loyaltyClient;

    @MockBean
    private AppointmentClient appointmentClient;

    private static final int CONCURRENT_REQUESTS = 200;
    private static final int THREAD_POOL_SIZE = 30;

    @BeforeEach
    public void setup() {
        // Configuração padrão para o cliente de profissionais
        ProfessionalDto professionalDto = ProfessionalDto.builder()
                .id(1L)
                .name("João Silva")
                .specialization("Cabeleireiro")
                .email("joao.silva@example.com")
                .phone("11987654321")
                .active(true)
                .build();
        
        when(professionalClient.getProfessionalById(anyLong())).thenReturn(Optional.of(professionalDto));
        
        // Configuração padrão para agendamentos
        List<Map<String, Object>> appointments = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Map<String, Object> appointment = new HashMap<>();
            appointment.put("appointmentId", (long) i);
            appointment.put("serviceDate", LocalDateTime.now().plusDays(i % 7));
            appointment.put("serviceName", "Serviço " + (i % 5 + 1));
            appointment.put("clientName", "Cliente " + (i % 10 + 1));
            appointment.put("status", i % 3 == 0 ? "CONFIRMADO" : (i % 3 == 1 ? "PENDENTE" : "CONCLUÍDO"));
            appointment.put("price", 50.0 + (i * 10));
            appointments.add(appointment);
        }
        
        when(appointmentClient.getProfessionalAppointments(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(appointments);
        
        // Configuração padrão para pontos de fidelidade
        Map<Long, Integer> clientPointsMap = new HashMap<>();
        for (int i = 1; i <= 100; i++) {
            clientPointsMap.put((long) i, 100 + (i * 5));
            when(clientClient.getClientName((long) i)).thenReturn("Cliente " + i);
        }
        
        when(loyaltyClient.getAllClientsTotalPoints()).thenReturn(clientPointsMap);
        
        // Configuração padrão para dados de receita
        List<Map<String, Object>> revenueData = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Object> serviceRevenue = new HashMap<>();
            serviceRevenue.put("serviceName", "Serviço " + (i + 1));
            serviceRevenue.put("count", 10 + (i * 2));
            serviceRevenue.put("percentage", 5.0 + (i * 2.5));
            serviceRevenue.put("revenue", 500.0 + (i * 150));
            revenueData.add(serviceRevenue);
        }
        
        Map<String, Object> revenueSummary = new HashMap<>();
        revenueSummary.put("totalRevenue", 5250.0);
        revenueSummary.put("appointmentsCount", 105);
        revenueSummary.put("averageTicket", 50.0);
        revenueSummary.put("mostPopularService", "Serviço 1");
        revenueSummary.put("mostRevenueService", "Serviço 10");
        
        when(appointmentClient.getRevenueSummary(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(revenueSummary);
        
        when(appointmentClient.getServiceRevenueBreakdown(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(revenueData);
    }

    /**
     * Teste de performance para geração concorrente de relatórios de agenda de profissionais
     */
    @Test
    public void testConcurrentProfessionalScheduleReportGeneration() throws InterruptedException {
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
                    
                    // Criar requisição para geração de relatório
                    Long professionalId = (long) (requestIndex % 5 + 1);
                    LocalDate startDate = LocalDate.now().minusDays(requestIndex % 30 + 1);
                    LocalDate endDate = LocalDate.now();
                    
                    ReportRequest request = ReportRequest.builder()
                            .reportType(ReportType.PROFESSIONAL_SCHEDULE)
                            .startDate(startDate)
                            .endDate(endDate)
                            .professionalId(professionalId)
                            .format("PDF")
                            .build();
                    
                    // Gerar relatório
                    ReportResponse response = reportService.generateReport(request);
                    
                    // Verificar resultado
                    if (response != null && "COMPLETED".equals(response.getStatus())) {
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
        boolean completed = latch.await(60, TimeUnit.SECONDS);
        assertTrue(completed, "Nem todas as requisições foram processadas no tempo esperado");
        
        // Calcular o tempo total
        Instant endTime = Instant.now();
        long totalDuration = Duration.between(startTime, endTime).toMillis();
        
        // Calcular tempo médio de processamento
        double averageProcessingTime = calculateAverage(processingTimes);
        
        // Calcular throughput (requisições por segundo)
        double throughput = CONCURRENT_REQUESTS * 1000.0 / totalDuration;
        
        // Log das métricas
        System.out.println("==== Performance Test Results (Professional Schedule Reports) ====");
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
     * Teste de performance para geração concorrente de relatórios de fidelidade
     */
    @Test
    public void testConcurrentLoyaltyPointsReportGeneration() throws InterruptedException {
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
                    
                    // Criar requisição para geração de relatório
                    LocalDate startDate = LocalDate.now().minusDays(requestIndex % 30 + 30);
                    LocalDate endDate = LocalDate.now();
                    
                    ReportRequest request = ReportRequest.builder()
                            .reportType(ReportType.LOYALTY_POINTS_SUMMARY)
                            .startDate(startDate)
                            .endDate(endDate)
                            .format(requestIndex % 2 == 0 ? "PDF" : "EXCEL")
                            .build();
                    
                    // Gerar relatório
                    ReportResponse response = reportService.generateReport(request);
                    
                    // Verificar resultado
                    if (response != null && "COMPLETED".equals(response.getStatus())) {
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
        boolean completed = latch.await(60, TimeUnit.SECONDS);
        assertTrue(completed, "Nem todas as requisições foram processadas no tempo esperado");
        
        // Calcular o tempo total
        Instant endTime = Instant.now();
        long totalDuration = Duration.between(startTime, endTime).toMillis();
        
        // Calcular tempo médio de processamento
        double averageProcessingTime = calculateAverage(processingTimes);
        
        // Calcular throughput (requisições por segundo)
        double throughput = CONCURRENT_REQUESTS * 1000.0 / totalDuration;
        
        // Log das métricas
        System.out.println("==== Performance Test Results (Loyalty Points Reports) ====");
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
     * Teste de performance para geração concorrente de relatórios de receita
     */
    @Test
    public void testConcurrentRevenueSummaryReportGeneration() throws InterruptedException {
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
                    
                    // Criar requisição para geração de relatório
                    int periodDays = (requestIndex % 4 + 1) * 30; // 30, 60, 90 ou 120 dias
                    LocalDate startDate = LocalDate.now().minusDays(periodDays);
                    LocalDate endDate = LocalDate.now();
                    
                    ReportRequest request = ReportRequest.builder()
                            .reportType(ReportType.REVENUE_SUMMARY)
                            .startDate(startDate)
                            .endDate(endDate)
                            .format(requestIndex % 2 == 0 ? "PDF" : "EXCEL")
                            .build();
                    
                    // Gerar relatório
                    ReportResponse response = reportService.generateReport(request);
                    
                    // Verificar resultado
                    if (response != null && "COMPLETED".equals(response.getStatus())) {
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
        boolean completed = latch.await(60, TimeUnit.SECONDS);
        assertTrue(completed, "Nem todas as requisições foram processadas no tempo esperado");
        
        // Calcular o tempo total
        Instant endTime = Instant.now();
        long totalDuration = Duration.between(startTime, endTime).toMillis();
        
        // Calcular tempo médio de processamento
        double averageProcessingTime = calculateAverage(processingTimes);
        
        // Calcular throughput (requisições por segundo)
        double throughput = CONCURRENT_REQUESTS * 1000.0 / totalDuration;
        
        // Log das métricas
        System.out.println("==== Performance Test Results (Revenue Summary Reports) ====");
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
     * Teste de performance para geração mista de relatórios (múltiplos tipos)
     */
    @Test
    public void testMixedReportsGeneration() throws InterruptedException {
        // Registrar início do teste
        Instant startTime = Instant.now();
        
        // Contador para sincronizar os threads
        CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        
        // Métricas de performance
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        long[] processingTimes = new long[CONCURRENT_REQUESTS];
        
        // Métricas por tipo de relatório
        AtomicInteger scheduleReportCount = new AtomicInteger(0);
        AtomicInteger loyaltyReportCount = new AtomicInteger(0);
        AtomicInteger revenueReportCount = new AtomicInteger(0);
        
        // Pool de threads para execução concorrente
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        
        // Executar N requisições concorrentes
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            final int requestIndex = i;
            executorService.submit(() -> {
                try {
                    // Registrar início do processamento desta requisição
                    Instant requestStart = Instant.now();
                    
                    // Selecionar tipo de relatório aleatoriamente
                    ReportType reportType;
                    ReportRequest.ReportRequestBuilder requestBuilder = ReportRequest.builder();
                    
                    switch (requestIndex % 3) {
                        case 0:
                            reportType = ReportType.PROFESSIONAL_SCHEDULE;
                            requestBuilder.professionalId((long) (requestIndex % 5 + 1));
                            scheduleReportCount.incrementAndGet();
                            break;
                        case 1:
                            reportType = ReportType.LOYALTY_POINTS_SUMMARY;
                            loyaltyReportCount.incrementAndGet();
                            break;
                        default:
                            reportType = ReportType.REVENUE_SUMMARY;
                            revenueReportCount.incrementAndGet();
                            break;
                    }
                    
                    // Configurar datas
                    LocalDate startDate = LocalDate.now().minusDays((requestIndex % 4 + 1) * 30);
                    LocalDate endDate = LocalDate.now();
                    
                    // Finalizar construção da requisição
                    ReportRequest request = requestBuilder
                            .reportType(reportType)
                            .startDate(startDate)
                            .endDate(endDate)
                            .format(requestIndex % 2 == 0 ? "PDF" : "EXCEL")
                            .build();
                    
                    // Gerar relatório
                    ReportResponse response = reportService.generateReport(request);
                    
                    // Verificar resultado
                    if (response != null && "COMPLETED".equals(response.getStatus())) {
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
        boolean completed = latch.await(60, TimeUnit.SECONDS);
        assertTrue(completed, "Nem todas as requisições foram processadas no tempo esperado");
        
        // Calcular o tempo total
        Instant endTime = Instant.now();
        long totalDuration = Duration.between(startTime, endTime).toMillis();
        
        // Calcular tempo médio de processamento
        double averageProcessingTime = calculateAverage(processingTimes);
        
        // Calcular throughput (requisições por segundo)
        double throughput = CONCURRENT_REQUESTS * 1000.0 / totalDuration;
        
        // Log das métricas
        System.out.println("==== Performance Test Results (Mixed Reports) ====");
        System.out.println("Total requests: " + CONCURRENT_REQUESTS);
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + failureCount.get());
        System.out.println("Schedule reports: " + scheduleReportCount.get());
        System.out.println("Loyalty reports: " + loyaltyReportCount.get());
        System.out.println("Revenue reports: " + revenueReportCount.get());
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
     * Teste de resiliência simulando falhas nos serviços externos
     */
    @Test
    public void testResilienceWithExternalServiceFailures() throws InterruptedException {
        // Configurar falhas intermitentes no serviço de appointments (1 a cada 3 chamadas falha)
        AtomicInteger appointmentCallCount = new AtomicInteger(0);
        when(appointmentClient.getRevenueSummary(any(LocalDate.class), any(LocalDate.class)))
                .thenAnswer(invocation -> {
                    if (appointmentCallCount.incrementAndGet() % 3 == 0) {
                        throw new RuntimeException("Falha simulada no serviço de agendamentos");
                    }
                    
                    Map<String, Object> revenueSummary = new HashMap<>();
                    revenueSummary.put("totalRevenue", 5250.0);
                    revenueSummary.put("appointmentsCount", 105);
                    revenueSummary.put("averageTicket", 50.0);
                    revenueSummary.put("mostPopularService", "Serviço 1");
                    revenueSummary.put("mostRevenueService", "Serviço 10");
                    return revenueSummary;
                });
        
        // Configurar falhas intermitentes no serviço de clientes (1 a cada 4 chamadas falha)
        AtomicInteger clientCallCount = new AtomicInteger(0);
        when(clientClient.getClientName(anyLong()))
                .thenAnswer(invocation -> {
                    if (clientCallCount.incrementAndGet() % 4 == 0) {
                        throw new RuntimeException("Falha simulada no serviço de clientes");
                    }
                    
                    Long clientId = invocation.getArgument(0);
                    return "Cliente " + clientId;
                });
        
        // Contador para sincronizar os threads
        CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        
        // Métricas
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger recoveredCount = new AtomicInteger(0);
        
        // Pool de threads
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        
        // Executar N requisições concorrentes
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            final int requestIndex = i;
            executorService.submit(() -> {
                try {
                    // Selecionar tipo de relatório (alternando entre os 3 tipos)
                    ReportType reportType;
                    ReportRequest.ReportRequestBuilder requestBuilder = ReportRequest.builder();
                    
                    switch (requestIndex % 3) {
                        case 0:
                            reportType = ReportType.PROFESSIONAL_SCHEDULE;
                            requestBuilder.professionalId((long) (requestIndex % 5 + 1));
                            break;
                        case 1:
                            reportType = ReportType.LOYALTY_POINTS_SUMMARY;
                            break;
                        default:
                            reportType = ReportType.REVENUE_SUMMARY;
                            break;
                    }
                    
                    // Configurar datas
                    LocalDate startDate = LocalDate.now().minusDays(30);
                    LocalDate endDate = LocalDate.now();
                    
                    // Finalizar construção da requisição
                    ReportRequest request = requestBuilder
                            .reportType(reportType)
                            .startDate(startDate)
                            .endDate(endDate)
                            .format("PDF")
                            .build();
                    
                    // Gerar relatório (pode falhar devido às falhas simuladas)
                    ReportResponse response = reportService.generateReport(request);
                    
                    // Verificar resultado
                    if (response != null) {
                        if ("COMPLETED".equals(response.getStatus())) {
                            successCount.incrementAndGet();
                        } else if ("PARTIAL".equals(response.getStatus())) {
                            // Relatório foi gerado parcialmente (recuperação de falha)
                            recoveredCount.incrementAndGet();
                        } else {
                            failureCount.incrementAndGet();
                        }
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
        boolean completed = latch.await(60, TimeUnit.SECONDS);
        assertTrue(completed, "Nem todas as requisições foram processadas no tempo esperado");
        
        // Log dos resultados
        System.out.println("==== Resilience Test Results ====");
        System.out.println("Total requests: " + CONCURRENT_REQUESTS);
        System.out.println("Successful: " + successCount.get());
        System.out.println("Partially successful (recovered): " + recoveredCount.get());
        System.out.println("Failures: " + failureCount.get());
        
        // Verificações
        // Como estamos simulando falhas, esperamos que algumas requisições sejam bem-sucedidas
        // e outras possam falhar ou ser parcialmente bem-sucedidas
        assertTrue(successCount.get() + recoveredCount.get() > 0, "Deve haver pelo menos algumas requisições bem-sucedidas ou parcialmente recuperadas");
        
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
