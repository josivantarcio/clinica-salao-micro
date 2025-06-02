package com.clinicsalon.auth.integration;

import com.clinicsalon.auth.dto.AuthResponseDto;
import com.clinicsalon.auth.dto.LoginRequestDto;
import com.clinicsalon.auth.dto.RegisterRequestDto;
import com.clinicsalon.auth.model.User;
import com.clinicsalon.auth.model.UserRole;
import com.clinicsalon.auth.repository.UserRepository;
import com.clinicsalon.auth.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para o serviço de autenticação
 * Verifica o fluxo completo de registro, login e validação de token
 * usando um servidor embarcado e banco de dados em memória
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Tag("integration")
public class AuthServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private final String baseUrl = "http://localhost:";
    private static final String TEST_EMAIL = "integration@example.com";
    private static final String TEST_PASSWORD = "Password123!";

    @BeforeEach
    public void setup() {
        // Limpar dados de teste anteriores
        userRepository.deleteAll();
    }

    @AfterEach
    public void cleanup() {
        // Limpar dados após testes
        userRepository.deleteAll();
    }

    /**
     * Testa o fluxo completo de registro de usuário
     */
    @Test
    public void testRegisterUser() {
        // Preparar request de registro
        RegisterRequestDto registerRequest = new RegisterRequestDto();
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setFirstName("Integration");
        registerRequest.setLastName("Test");

        // Enviar requisição de registro
        ResponseEntity<AuthResponseDto> response = restTemplate.postForEntity(
                baseUrl + port + "/auth/register",
                registerRequest,
                AuthResponseDto.class
        );

        // Verificar resposta
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());

        // Verificar que o usuário foi criado no banco
        Optional<User> savedUser = userRepository.findByEmail(TEST_EMAIL);
        assertTrue(savedUser.isPresent());
        assertEquals("Integration", savedUser.get().getFirstName());
        assertEquals("Test", savedUser.get().getLastName());
        assertTrue(savedUser.get().getRoles().contains(UserRole.ROLE_USER));
    }

    /**
     * Testa o fluxo completo de login
     */
    @Test
    public void testLogin() {
        // Primeiro registrar um usuário
        RegisterRequestDto registerRequest = new RegisterRequestDto();
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setFirstName("Integration");
        registerRequest.setLastName("Test");

        restTemplate.postForEntity(
                baseUrl + port + "/auth/register",
                registerRequest,
                AuthResponseDto.class
        );

        // Preparar request de login
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        // Enviar requisição de login
        ResponseEntity<AuthResponseDto> response = restTemplate.postForEntity(
                baseUrl + port + "/auth/login",
                loginRequest,
                AuthResponseDto.class
        );

        // Verificar resposta
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());

        // Verificar que o token é válido
        String token = response.getBody().getToken();
        String username = jwtService.extractUsername(token);
        assertEquals(TEST_EMAIL, username);
        assertTrue(jwtService.isTokenValid(token, TEST_EMAIL));
    }

    /**
     * Testa tentativa de login com credenciais inválidas
     */
    @Test
    public void testLoginWithInvalidCredentials() {
        // Preparar request de login com senha errada
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword("wrongpassword");

        // Enviar requisição de login
        ResponseEntity<AuthResponseDto> response = restTemplate.postForEntity(
                baseUrl + port + "/auth/login",
                loginRequest,
                AuthResponseDto.class
        );

        // Verificar resposta (deve ser 401 Unauthorized)
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Testa o acesso a endpoint protegido com token válido
     */
    @Test
    public void testAccessProtectedEndpointWithValidToken() {
        // Primeiro registrar um usuário
        RegisterRequestDto registerRequest = new RegisterRequestDto();
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setFirstName("Integration");
        registerRequest.setLastName("Test");

        ResponseEntity<AuthResponseDto> registerResponse = restTemplate.postForEntity(
                baseUrl + port + "/auth/register",
                registerRequest,
                AuthResponseDto.class
        );

        String token = registerResponse.getBody().getToken();

        // Configurar headers com o token JWT
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Acessar endpoint protegido
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + port + "/auth/validate",
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        // Verificar resposta
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Testa o acesso a endpoint protegido sem token
     */
    @Test
    public void testAccessProtectedEndpointWithoutToken() {
        // Acessar endpoint protegido sem token
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + port + "/auth/validate",
                String.class
        );

        // Verificar resposta (deve ser 401 Unauthorized)
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Testa validação de token com roles
     */
    @Test
    public void testTokenRolesValidation() {
        // Criar um usuário com role de admin
        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"); // encoded "password"
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRoles(Set.of(UserRole.ROLE_ADMIN));
        userRepository.save(adminUser);

        // Gerar token manualmente com role de admin
        String adminToken = jwtService.generateToken("admin@example.com", List.of("ROLE_ADMIN"));

        // Validar que o token contém a role correta
        List<String> roles = jwtService.extractRoles(adminToken);
        assertTrue(roles.contains("ROLE_ADMIN"));
        assertFalse(roles.contains("ROLE_USER"));
    }

    /**
     * Testa o fluxo de atualização de token
     */
    @Test
    public void testTokenRefresh() {
        // Primeiro registrar um usuário
        RegisterRequestDto registerRequest = new RegisterRequestDto();
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setFirstName("Integration");
        registerRequest.setLastName("Test");

        ResponseEntity<AuthResponseDto> registerResponse = restTemplate.postForEntity(
                baseUrl + port + "/auth/register",
                registerRequest,
                AuthResponseDto.class
        );

        String initialToken = registerResponse.getBody().getToken();

        // Configurar headers com o token JWT
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(initialToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Solicitar refresh do token
        ResponseEntity<AuthResponseDto> refreshResponse = restTemplate.exchange(
                baseUrl + port + "/auth/refresh",
                HttpMethod.POST,
                requestEntity,
                AuthResponseDto.class
        );

        // Verificar resposta
        assertEquals(HttpStatus.OK, refreshResponse.getStatusCode());
        assertNotNull(refreshResponse.getBody());
        assertNotNull(refreshResponse.getBody().getToken());

        // Verificar que o novo token é diferente do anterior
        String refreshedToken = refreshResponse.getBody().getToken();
        assertNotEquals(initialToken, refreshedToken);

        // Verificar que o novo token é válido
        String username = jwtService.extractUsername(refreshedToken);
        assertEquals(TEST_EMAIL, username);
        assertTrue(jwtService.isTokenValid(refreshedToken, TEST_EMAIL));
    }
}
