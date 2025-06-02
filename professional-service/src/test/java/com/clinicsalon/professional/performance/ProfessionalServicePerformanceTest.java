package com.clinicsalon.professional.performance;

import com.clinicsalon.professional.dto.ProfessionalDto;
import com.clinicsalon.professional.dto.ServiceDto;
import com.clinicsalon.professional.dto.ScheduleDto;
import com.clinicsalon.professional.model.Professional;
import com.clinicsalon.professional.model.Service;
import com.clinicsalon.professional.repository.ProfessionalRepository;
import com.clinicsalon.professional.repository.ServiceRepository;
import com.clinicsalon.professional.service.ProfessionalService;
import com.clinicsalon.professional.service.ProfessionalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
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
 * Testes de performance para o serviço de profissionais
 * Simula alta carga no sistema e mede a capacidade de processamento
 */
@ExtendWith(MockitoExtension.class)
@Tag("performance")
public class ProfessionalServicePerformanceTest {

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private ServiceRepository serviceRepository;

    private ProfessionalService professionalService;

    private static final int CONCURRENT_REQUESTS = 500;
    private static final int THREAD_POOL_SIZE = 50;

    @BeforeEach
    public void setup() {
        professionalService = new ProfessionalServiceImpl(professionalRepository, serviceRepository);
    }

    /**
     * Teste de stress simulando múltiplas requisições concorrentes para busca de profissionais
     */
    @Test
    public void testConcurrentProfessionalSearch() throws InterruptedException {
        // Configurar comportamento simulado para busca de profissionais
        List<Professional> professionals = createMockProfessionals(50);
        
        when(professionalRepository.findBySpecialtiesContainingIgnoreCase(anyString()))
                .thenReturn(professionals);

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
                    
                    // Buscar profissionais por especialidade
                    String specialty = "Cabelo";
                    if (requestIndex % 3 == 0) specialty = "Unhas";
                    else if (requestIndex % 3 == 1) specialty = "Estética";
                    
                    List<ProfessionalDto> result = professionalService.findBySpecialty(specialty);
                    
                    // Verificar resultado
                    if (result != null && !result.isEmpty()) {
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
        System.out.println("==== Performance Test Results (Professional Search) ====");
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
     * Teste de stress simulando múltiplas requisições concorrentes para 
     * busca de disponibilidade de profissionais
     */
    @Test
    public void testConcurrentAvailabilitySearch() throws InterruptedException {
        // Configurar comportamento simulado para busca de profissionais
        List<Professional> professionals = createMockProfessionals(20);
        
        when(professionalRepository.findAll())
                .thenReturn(professionals);
                
        when(professionalRepository.findById(anyLong()))
                .thenAnswer(invocation -> {
                    Long id = invocation.getArgument(0);
                    Optional<Professional> professional = professionals.stream()
                        .filter(p -> p.getId() == id)
                        .findFirst();
                    return professional;
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
                    
                    // Buscar disponibilidade dos profissionais
                    long professionalId = 100L + (requestIndex % 20);
                    LocalDate date = LocalDate.now().plusDays(requestIndex % 14);
                    
                    List<ScheduleDto> result = professionalService.getAvailableSlots(professionalId, date);
                    
                    // Verificar resultado
                    if (result != null) {
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
        System.out.println("==== Performance Test Results (Availability Search) ====");
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
     * Teste de stress simulando múltiplas requisições concorrentes para busca de serviços
     */
    @Test
    public void testConcurrentServiceSearch() throws InterruptedException {
        // Configurar comportamento simulado para busca de serviços
        List<Service> services = createMockServices(30);
        
        when(serviceRepository.findByNameContainingIgnoreCase(anyString()))
                .thenReturn(services);
                
        when(serviceRepository.findByProfessionalId(anyLong()))
                .thenReturn(services.subList(0, 10));

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
                    
                    List<ServiceDto> result;
                    
                    // Alternar entre busca por nome e por profissional
                    if (requestIndex % 2 == 0) {
                        // Buscar serviços por nome
                        String searchTerm = "Corte";
                        result = professionalService.findServicesByName(searchTerm);
                    } else {
                        // Buscar serviços por profissional
                        long professionalId = 100L + (requestIndex % 20);
                        result = professionalService.findServicesByProfessional(professionalId);
                    }
                    
                    // Verificar resultado
                    if (result != null && !result.isEmpty()) {
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
        System.out.println("==== Performance Test Results (Service Search) ====");
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
     * Teste de resiliência com falhas simuladas no repositório
     */
    @Test
    public void testResilienceWithRepositoryFailures() throws InterruptedException {
        // Configurar mock para falhar em 30% das requisições
        AtomicInteger callCount = new AtomicInteger(0);
        
        when(professionalRepository.findBySpecialtiesContainingIgnoreCase(anyString()))
                .thenAnswer(invocation -> {
                    // Falhar em 30% das chamadas
                    if (callCount.incrementAndGet() % 3 == 0) {
                        throw new RuntimeException("Falha simulada no banco de dados");
                    }
                    
                    return createMockProfessionals(20);
                });
                
        // Contador para sincronizar os threads
        CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        
        // Métricas
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        // Pool de threads
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        
        // Executar N requisições concorrentes
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            executorService.submit(() -> {
                try {
                    // Buscar profissionais por especialidade (pode falhar devido às falhas simuladas)
                    List<ProfessionalDto> result = professionalService.findBySpecialty("Cabelo");
                    
                    if (result != null && !result.isEmpty()) {
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
        
        // Log dos resultados
        System.out.println("==== Resilience Test Results ====");
        System.out.println("Total requests: " + CONCURRENT_REQUESTS);
        System.out.println("Successful: " + successCount.get());
        System.out.println("Failures: " + failureCount.get());
        
        // Verificações
        // Em um cenário com 30% de falhas simuladas, esperamos pelo menos algumas requisições bem-sucedidas
        assertTrue(successCount.get() > 0, "Deve haver pelo menos algumas requisições bem-sucedidas");
        
        // Limpar recursos
        executorService.shutdown();
    }
    
    /**
     * Cria uma lista de profissionais para mock
     */
    private List<Professional> createMockProfessionals(int count) {
        List<Professional> professionals = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Professional professional = new Professional();
            professional.setId(100L + i);
            professional.setName("Profissional " + (i+1));
            professional.setEmail("profissional" + (i+1) + "@example.com");
            professional.setPhone("(11) 9" + String.format("%04d", i) + "-1234");
            
            // Definir especialidades
            List<String> specialties = new ArrayList<>();
            if (i % 3 == 0) {
                specialties.add("Cabelo");
                specialties.add("Barba");
            } else if (i % 3 == 1) {
                specialties.add("Unhas");
                specialties.add("Maquiagem");
            } else {
                specialties.add("Estética");
                specialties.add("Massagem");
            }
            professional.setSpecialties(specialties);
            
            // Horários de trabalho
            professional.setWorkStartTime(LocalTime.of(8, 0));
            professional.setWorkEndTime(LocalTime.of(18, 0));
            
            // Dias de trabalho
            List<DayOfWeek> workDays = new ArrayList<>();
            workDays.add(DayOfWeek.MONDAY);
            workDays.add(DayOfWeek.TUESDAY);
            workDays.add(DayOfWeek.WEDNESDAY);
            workDays.add(DayOfWeek.THURSDAY);
            workDays.add(DayOfWeek.FRIDAY);
            if (i % 2 == 0) workDays.add(DayOfWeek.SATURDAY);
            professional.setWorkDays(workDays);
            
            professionals.add(professional);
        }
        
        return professionals;
    }
    
    /**
     * Cria uma lista de serviços para mock
     */
    private List<Service> createMockServices(int count) {
        List<Service> services = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Service service = new Service();
            service.setId(200L + i);
            
            if (i % 3 == 0) {
                service.setName("Corte de Cabelo " + (i/3 + 1));
                service.setDescription("Corte profissional com técnicas modernas");
                service.setDuration(45);
                service.setPrice(BigDecimal.valueOf(70 + (i*2)));
                service.setCategory("Cabelo");
            } else if (i % 3 == 1) {
                service.setName("Manicure " + (i/3 + 1));
                service.setDescription("Tratamento completo para unhas");
                service.setDuration(60);
                service.setPrice(BigDecimal.valueOf(50 + (i*1.5)));
                service.setCategory("Unhas");
            } else {
                service.setName("Tratamento Facial " + (i/3 + 1));
                service.setDescription("Limpeza de pele profunda com produtos premium");
                service.setDuration(90);
                service.setPrice(BigDecimal.valueOf(120 + (i*3)));
                service.setCategory("Estética");
            }
            
            // Associar a um profissional
            service.setProfessionalId(100L + (i % 20));
            
            services.add(service);
        }
        
        return services;
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
