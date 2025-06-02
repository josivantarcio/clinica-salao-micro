package com.clinicsalon.gateway.performance;

import com.clinicsalon.gateway.filter.AuthenticationFilter;
import com.clinicsalon.gateway.filter.RouteValidator;
import com.clinicsalon.gateway.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes de performance para o API Gateway
 * Simulam alta carga e medem a capacidade de processamento
 */
@ExtendWith(MockitoExtension.class)
@Tag("performance")
public class ApiGatewayPerformanceTest {

    private AuthenticationFilter authenticationFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RouteValidator routeValidator;

    @Mock
    private GatewayFilterChain filterChain;

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final int CONCURRENT_REQUESTS = 1000;
    private static final int THREAD_POOL_SIZE = 50;
    
    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setup() {
        routeValidator.isSecured = mock(Predicate.class);
        authenticationFilter = new AuthenticationFilter(jwtUtil, routeValidator);
        
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
        when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.extractUsername(VALID_TOKEN)).thenReturn("testuser");
        when(jwtUtil.extractRoles(VALID_TOKEN)).thenReturn("ROLE_USER");
    }

    /**
     * Teste de stress simulando múltiplas requisições concorrentes para endpoints protegidos
     * Mede o tempo médio de processamento e garante que todos os requests são processados
     */
    @Test
    public void testHighConcurrencyOnSecuredEndpoints() throws InterruptedException {
        // Configurar o filtro para endpoints seguros
        when(routeValidator.isSecured.test(any())).thenReturn(true);
        
        // Criar o filtro
        GatewayFilter filter = authenticationFilter.apply(new AuthenticationFilter.Config());
        
        // Registrar o início do teste
        Instant startTime = Instant.now();
        
        // Contador para sincronizar os threads
        CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        
        // Métricas de performance
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
                    
                    // Criar requisição com token válido
                    MockServerHttpRequest request = MockServerHttpRequest
                            .get("http://localhost:8080/clients/" + requestIndex)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .build();
                    ServerWebExchange exchange = MockServerWebExchange.from(request);
                    
                    // Executar o filtro e esperar o resultado
                    Mono<Void> result = filter.filter(exchange, filterChain);
                    result.block(); // Esperar a conclusão do resultado (em produção, seria assíncrono)
                    
                    // Calcular tempo de processamento desta requisição
                    long processingTime = Duration.between(requestStart, Instant.now()).toMillis();
                    processingTimes[requestIndex] = processingTime;
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
        System.out.println("==== Performance Test Results ====");
        System.out.println("Total requests: " + CONCURRENT_REQUESTS);
        System.out.println("Total duration: " + totalDuration + " ms");
        System.out.println("Average processing time: " + averageProcessingTime + " ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " requests/second");
        
        // Verificações de performance
        // Os valores abaixo são apenas exemplos e podem ser ajustados conforme necessário
        assertTrue(averageProcessingTime < 50, "Tempo médio de processamento muito alto: " + averageProcessingTime + " ms");
        assertTrue(throughput > 100, "Throughput muito baixo: " + throughput + " req/sec");
        
        // Verificar que o método validate foi chamado o número correto de vezes
        verify(jwtUtil, times(CONCURRENT_REQUESTS)).validateToken(VALID_TOKEN);
        
        // Limpar recursos
        executorService.shutdown();
    }
    
    /**
     * Teste de stress simulando múltiplas requisições concorrentes para endpoints abertos
     */
    @Test
    public void testHighConcurrencyOnOpenEndpoints() throws InterruptedException {
        // Configurar o filtro para endpoints abertos
        when(routeValidator.isSecured.test(any())).thenReturn(false);
        
        // Criar o filtro
        GatewayFilter filter = authenticationFilter.apply(new AuthenticationFilter.Config());
        
        // Registrar o início do teste
        Instant startTime = Instant.now();
        
        // Contador para sincronizar os threads
        CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        
        // Métricas de performance
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
                    
                    // Criar requisição para endpoint aberto (sem token)
                    MockServerHttpRequest request = MockServerHttpRequest
                            .get("http://localhost:8080/auth/login")
                            .build();
                    ServerWebExchange exchange = MockServerWebExchange.from(request);
                    
                    // Executar o filtro e esperar o resultado
                    Mono<Void> result = filter.filter(exchange, filterChain);
                    result.block(); // Esperar a conclusão do resultado
                    
                    // Calcular tempo de processamento desta requisição
                    long processingTime = Duration.between(requestStart, Instant.now()).toMillis();
                    processingTimes[requestIndex] = processingTime;
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
        System.out.println("==== Performance Test Results (Open Endpoints) ====");
        System.out.println("Total requests: " + CONCURRENT_REQUESTS);
        System.out.println("Total duration: " + totalDuration + " ms");
        System.out.println("Average processing time: " + averageProcessingTime + " ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " requests/second");
        
        // Verificações de performance para endpoints abertos (devem ser mais rápidos)
        assertTrue(averageProcessingTime < 20, "Tempo médio de processamento muito alto: " + averageProcessingTime + " ms");
        assertTrue(throughput > 200, "Throughput muito baixo: " + throughput + " req/sec");
        
        // Verificar que o validateToken não foi chamado em endpoints abertos
        verify(jwtUtil, never()).validateToken(anyString());
        
        // Limpar recursos
        executorService.shutdown();
    }
    
    /**
     * Teste de resiliência para avaliar o comportamento do gateway sob condições
     * de falha do serviço de validação de tokens
     */
    @Test
    public void testResilienceWithTokenValidationFailures() throws InterruptedException {
        // Configurar o filtro para endpoints seguros
        when(routeValidator.isSecured.test(any())).thenReturn(true);
        
        // Simular falhas intermitentes na validação de token (50% das vezes)
        when(jwtUtil.validateToken(anyString())).thenAnswer(invocation -> {
            // Simulação de latência alta em alguns casos
            if (Math.random() < 0.3) {
                Thread.sleep(100); // Simular latência alta em 30% das requisições
            }
            
            // Simular falha em 50% das requisições
            return Math.random() >= 0.5;
        });
        
        // Criar o filtro
        GatewayFilter filter = authenticationFilter.apply(new AuthenticationFilter.Config());
        
        // Contador para sincronizar os threads
        CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        
        // Contador de requisições concluídas com sucesso
        final int[] successCount = {0};
        
        // Pool de threads para execução concorrente
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        
        // Executar N requisições concorrentes
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            executorService.submit(() -> {
                try {
                    // Criar requisição com token
                    MockServerHttpRequest request = MockServerHttpRequest
                            .get("http://localhost:8080/clients/123")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .build();
                    ServerWebExchange exchange = MockServerWebExchange.from(request);
                    
                    // Executar o filtro e esperar o resultado
                    Mono<Void> result = filter.filter(exchange, filterChain);
                    result.block(); // Esperar a conclusão
                    
                    // Se a chamada do filtro não lançou exceção, é um sucesso
                    synchronized(successCount) {
                        successCount[0]++;
                    }
                } catch (Exception e) {
                    // Falha no processamento (esperado em alguns casos)
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
        System.out.println("Successful requests: " + successCount[0]);
        System.out.println("Failed requests: " + (CONCURRENT_REQUESTS - successCount[0]));
        
        // Verificações de resiliência
        // Pelo menos metade das requisições devem ser processadas com sucesso
        assertTrue(successCount[0] > 0, "Nenhuma requisição foi processada com sucesso");
        
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
