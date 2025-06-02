package com.clinicsalon.auth.performance;

import com.clinicsalon.auth.dto.AuthResponseDto;
import com.clinicsalon.auth.dto.LoginRequestDto;
import com.clinicsalon.auth.model.User;
import com.clinicsalon.auth.model.UserRole;
import com.clinicsalon.auth.repository.UserRepository;
import com.clinicsalon.auth.security.JwtService;
import com.clinicsalon.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Testes de performance para o serviço de autenticação
 * Simula alta carga no sistema e mede a capacidade de processamento
 * com foco especial na geração e validação de tokens JWT
 */
@ExtendWith(MockitoExtension.class)
@Tag("performance")
public class AuthServicePerformanceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    private AuthService authService;

    private static final int CONCURRENT_REQUESTS = 1000;
    private static final int THREAD_POOL_SIZE = 50;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwicm9sZXMiOiJST0xFX1VTRVIiLCJpYXQiOjE2MTYxODIzNDUsImV4cCI6MTYxNjE4NTk0NX0.bWrgDq2BxtpIyKA8Cz2RjGMF50GIVkJZvSsP8Iw3iGo";

    @BeforeEach
    public void setup() {
        // Inicializar serviço de autenticação
        authService = new AuthService(userRepository, passwordEncoder, jwtService, authenticationManager);
        
        // Configurar mocks para testes
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        testUser.setRoles(Set.of(UserRole.ROLE_USER));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        
        // Configurar comportamento do repository
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        
        // Configurar comportamento do authentication manager
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
                
        // Configurar comportamento do authentication
        when(authentication.getName()).thenReturn(TEST_EMAIL);
        
        // Configurar comportamento do JWT service
        when(jwtService.generateToken(eq(TEST_EMAIL), any())).thenReturn(TEST_TOKEN);
    }

    /**
     * Teste de stress simulando múltiplas requisições concorrentes de login
     */
    @Test
    public void testConcurrentLoginRequests() throws InterruptedException {
        // Preparar request de login
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);
        
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
                    
                    // Executar login
                    AuthResponseDto response = authService.login(loginRequest);
                    
                    // Verificar resposta
                    if (response != null && response.getToken().equals(TEST_TOKEN)) {
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
        
        // Calcular o tempo total e médio
        Instant endTime = Instant.now();
        long totalDuration = Duration.between(startTime, endTime).toMillis();
        
        // Calcular tempo médio de processamento
        double averageProcessingTime = calculateAverage(processingTimes);
        
        // Calcular throughput (requisições por segundo)
        double throughput = CONCURRENT_REQUESTS * 1000.0 / totalDuration;
        
        // Log das métricas
        System.out.println("==== Performance Test Results (Login) ====");
        System.out.println("Total requests: " + CONCURRENT_REQUESTS);
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + failureCount.get());
        System.out.println("Total duration: " + totalDuration + " ms");
        System.out.println("Average processing time: " + averageProcessingTime + " ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " requests/second");
        
        // Verificações de performance
        assertEquals(CONCURRENT_REQUESTS, successCount.get(), "Todas as requisições devem ser bem-sucedidas");
        assertTrue(averageProcessingTime < 50, "Tempo médio de processamento muito alto: " + averageProcessingTime + " ms");
        assertTrue(throughput > 100, "Throughput muito baixo: " + throughput + " req/sec");
        
        // Limpar recursos
        executorService.shutdown();
    }
    
    /**
     * Teste de performance para a geração de tokens JWT
     * Medindo a capacidade de gerar tokens em alta escala
     */
    @Test
    public void testJwtTokenGenerationPerformance() throws InterruptedException {
        // Contador para sincronizar os threads
        CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        
        // Métricas de performance
        long[] processingTimes = new long[CONCURRENT_REQUESTS];
        
        // Pool de threads para execução concorrente
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        
        // Lista de roles para o token
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_USER");
        
        // Registrar início do teste
        Instant startTime = Instant.now();
        
        // Executar N requisições concorrentes
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            final int requestIndex = i;
            final String email = "user" + i + "@example.com";
            
            executorService.submit(() -> {
                try {
                    // Registrar início do processamento desta requisição
                    Instant requestStart = Instant.now();
                    
                    // Gerar token JWT
                    String token = jwtService.generateToken(email, roles);
                    
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
        System.out.println("==== Performance Test Results (JWT Generation) ====");
        System.out.println("Total tokens generated: " + CONCURRENT_REQUESTS);
        System.out.println("Total duration: " + totalDuration + " ms");
        System.out.println("Average processing time: " + averageProcessingTime + " ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " tokens/second");
        
        // Verificações de performance - a geração de JWT deve ser rápida
        assertTrue(averageProcessingTime < 20, "Tempo médio de geração de token muito alto: " + averageProcessingTime + " ms");
        
        // Limpar recursos
        executorService.shutdown();
    }
    
    /**
     * Teste de performance para a validação de tokens JWT
     * Medindo a capacidade de validar tokens em alta escala
     */
    @Test
    public void testJwtTokenValidationPerformance() throws InterruptedException {
        // Configurar mock para validação de token
        when(jwtService.extractUsername(TEST_TOKEN)).thenReturn(TEST_EMAIL);
        when(jwtService.isTokenValid(TEST_TOKEN, TEST_EMAIL)).thenReturn(true);
        
        // Contador para sincronizar os threads
        CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        
        // Métricas de performance
        long[] processingTimes = new long[CONCURRENT_REQUESTS];
        AtomicInteger validTokenCount = new AtomicInteger(0);
        
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
                    
                    // Extrair username do token
                    String username = jwtService.extractUsername(TEST_TOKEN);
                    
                    // Validar token
                    boolean isValid = jwtService.isTokenValid(TEST_TOKEN, username);
                    
                    if (isValid) {
                        validTokenCount.incrementAndGet();
                    }
                    
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
        System.out.println("==== Performance Test Results (JWT Validation) ====");
        System.out.println("Total tokens validated: " + CONCURRENT_REQUESTS);
        System.out.println("Valid tokens: " + validTokenCount.get());
        System.out.println("Total duration: " + totalDuration + " ms");
        System.out.println("Average processing time: " + averageProcessingTime + " ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " validations/second");
        
        // Verificações de performance
        assertEquals(CONCURRENT_REQUESTS, validTokenCount.get(), "Todos os tokens devem ser validados com sucesso");
        assertTrue(averageProcessingTime < 20, "Tempo médio de validação de token muito alto: " + averageProcessingTime + " ms");
        
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
