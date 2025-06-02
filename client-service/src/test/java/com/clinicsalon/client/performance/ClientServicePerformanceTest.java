package com.clinicsalon.client.performance;

import com.clinicsalon.client.dto.ClientDto;
import com.clinicsalon.client.model.Client;
import com.clinicsalon.client.repository.ClientRepository;
import com.clinicsalon.client.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
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
 * Testes de performance para o serviço de clientes
 * Simula alta carga no sistema e mede a capacidade de processamento
 */
@ExtendWith(MockitoExtension.class)
@Tag("performance")
public class ClientServicePerformanceTest {

    @Mock
    private ClientRepository clientRepository;

    private ClientService clientService;

    private static final int CONCURRENT_REQUESTS = 500;
    private static final int THREAD_POOL_SIZE = 50;

    @BeforeEach
    public void setup() {
        clientService = new ClientService(clientRepository);
    }

    /**
     * Teste de stress simulando múltiplas requisições concorrentes de busca de clientes
     */
    @Test
    public void testConcurrentClientSearch() throws InterruptedException {
        // Configurar dados de teste
        List<Client> clients = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Client client = new Client();
            client.setId((long) i);
            client.setName("Cliente " + i);
            client.setEmail("cliente" + i + "@example.com");
            client.setCpf("123.456.789-" + String.format("%02d", i));
            client.setPhone("(11) 9" + String.format("%04d", i) + "-1234");
            client.setBirthDate(LocalDate.now().minusYears(20 + i % 40));
            clients.add(client);
        }

        // Configurar comportamento simulado do repositório
        when(clientRepository.findAll(any(Pageable.class)))
                .thenAnswer(invocation -> {
                    Pageable pageable = invocation.getArgument(0);
                    int pageSize = pageable.getPageSize();
                    int pageNumber = pageable.getPageNumber();
                    int start = pageNumber * pageSize;
                    int end = Math.min(start + pageSize, clients.size());
                    
                    List<Client> pageContent = clients.subList(start, end);
                    return new PageImpl<>(pageContent, pageable, clients.size());
                });

        when(clientRepository.findByNameContainingIgnoreCase(anyString(), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    String searchTerm = invocation.getArgument(0);
                    Pageable pageable = invocation.getArgument(1);
                    
                    List<Client> filteredClients = clients.stream()
                            .filter(c -> c.getName().toLowerCase().contains(searchTerm.toLowerCase()))
                            .toList();
                    
                    int pageSize = pageable.getPageSize();
                    int pageNumber = pageable.getPageNumber();
                    int start = pageNumber * pageSize;
                    int end = Math.min(start + pageSize, filteredClients.size());
                    
                    List<Client> pageContent = filteredClients.subList(start, end);
                    return new PageImpl<>(pageContent, pageable, filteredClients.size());
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
            final String searchTerm = i % 5 == 0 ? "Cliente" : "";  // Varia entre busca e listagem
            final int page = i % 10;
            
            executorService.submit(() -> {
                try {
                    // Registrar início do processamento desta requisição
                    Instant requestStart = Instant.now();
                    
                    // Buscar clientes
                    Page<ClientDto> result;
                    if (searchTerm.isEmpty()) {
                        result = clientService.getAllClients(page, 10);
                    } else {
                        result = clientService.searchClients(searchTerm, page, 10);
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
        System.out.println("==== Performance Test Results (Client Search) ====");
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
     * Teste de stress simulando múltiplas requisições concorrentes de recuperação de cliente por ID
     */
    @Test
    public void testConcurrentClientRetrieval() throws InterruptedException {
        // Configurar cliente de teste
        Client testClient = new Client();
        testClient.setId(1L);
        testClient.setName("Cliente Teste");
        testClient.setEmail("cliente.teste@example.com");
        testClient.setCpf("123.456.789-00");
        testClient.setPhone("(11) 91234-5678");
        testClient.setBirthDate(LocalDate.now().minusYears(30));

        // Configurar comportamento simulado do repositório
        when(clientRepository.findById(anyLong())).thenReturn(Optional.of(testClient));

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
                    
                    // Buscar cliente por ID
                    ClientDto client = clientService.getClientById(1L);
                    
                    // Verificar resultado
                    if (client != null && client.getId() == 1L) {
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
        System.out.println("==== Performance Test Results (Client Retrieval) ====");
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
     * Teste de performance para criação de clientes em massa
     */
    @Test
    public void testBulkClientCreation() throws InterruptedException {
        // Configurar comportamento simulado do repositório para salvar
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
            Client client = invocation.getArgument(0);
            if (client.getId() == null) {
                client.setId(1L + (long) (Math.random() * 1000));
            }
            return client;
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
                    
                    // Criar DTO para novo cliente
                    ClientDto newClientDto = new ClientDto();
                    newClientDto.setName("Cliente Novo " + UUID.randomUUID().toString().substring(0, 8));
                    newClientDto.setEmail("cliente" + requestIndex + "@example.com");
                    newClientDto.setCpf("123.456.789-" + String.format("%02d", requestIndex % 100));
                    newClientDto.setPhone("(11) 9" + String.format("%04d", requestIndex % 10000) + "-1234");
                    newClientDto.setBirthDate(LocalDate.now().minusYears(20 + requestIndex % 40));
                    
                    // Criar cliente
                    ClientDto result = clientService.createClient(newClientDto);
                    
                    // Verificar resultado
                    if (result != null && result.getId() != null) {
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
        System.out.println("==== Performance Test Results (Client Creation) ====");
        System.out.println("Total clients created: " + CONCURRENT_REQUESTS);
        System.out.println("Successful operations: " + successCount.get());
        System.out.println("Failed operations: " + failureCount.get());
        System.out.println("Total duration: " + totalDuration + " ms");
        System.out.println("Average processing time: " + averageProcessingTime + " ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " clients/second");
        
        // Verificações
        assertEquals(CONCURRENT_REQUESTS, successCount.get(), "Todas as criações devem ser bem-sucedidas");
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
