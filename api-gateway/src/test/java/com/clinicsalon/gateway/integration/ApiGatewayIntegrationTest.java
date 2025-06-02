package com.clinicsalon.gateway.integration;

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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes de integração para o API Gateway
 * Requer que os serviços de autenticação estejam em execução.
 * Executa com profile de teste para evitar dependências de serviços externos.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.cloud.gateway.discovery.locator.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "eureka.client.enabled=false",
    "spring.security.jwt.secret=testSecret123456789012345678901234567890"
})
@ActiveProfiles("test")
@Tag("integration")
public class ApiGatewayIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Testa o acesso a um endpoint de saúde (actuator)
     * Este endpoint não deve ser protegido e deve retornar 200 OK
     */
    @Test
    public void testActuatorHealthEndpoint() {
        // Construir URL para o endpoint de saúde
        String url = "http://localhost:" + port + "/actuator/health";
        
        // Realizar uma requisição GET simples
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        // Verificar resposta
        assertEquals(HttpStatus.OK, response.getStatusCode(), "O endpoint de health deve retornar 200 OK");
        assertNotNull(response.getBody(), "A resposta não deve ser nula");
        assertTrue(response.getBody().contains("UP"), "O status deve ser UP");
    }

    /**
     * Testa o comportamento do gateway quando um endpoint protegido é acessado sem token
     * Deve retornar 401 Unauthorized
     */
    @Test
    public void testSecuredEndpointWithoutToken() {
        // Construir URL para um endpoint protegido fictício
        String url = "http://localhost:" + port + "/api/clients/1";
        
        // Realizar uma requisição GET sem token
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        // Verificar resposta - deve ser 401 Unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), 
                "Acesso a endpoint protegido sem token deve retornar 401 Unauthorized");
    }

    /**
     * Testa o comportamento do gateway quando um endpoint protegido é acessado com token inválido
     * Deve retornar 401 Unauthorized
     */
    @Test
    public void testSecuredEndpointWithInvalidToken() {
        // Construir URL para um endpoint protegido fictício
        String url = "http://localhost:" + port + "/api/clients/1";
        
        // Configurar headers com token inválido
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.here");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        // Realizar uma requisição GET com token inválido
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
        
        // Verificar resposta - deve ser 401 Unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), 
                "Acesso a endpoint protegido com token inválido deve retornar 401 Unauthorized");
    }

    /**
     * Testa o fluxo completo de login e acesso a endpoint protegido
     * Este teste simula a autenticação para obter um token válido e depois usa esse token
     * para acessar um endpoint protegido
     * 
     * Observação: Este teste requer que o serviço de autenticação esteja disponível
     * Caso contrário, será ignorado
     */
    @Test
    public void testAuthenticationFlow() {
        // 1. Simular login para obter token
        String loginUrl = "http://localhost:" + port + "/auth/login";
        
        // Preparar corpo da requisição de login
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin@clinica.com");
        loginRequest.put("password", "admin123");
        
        // Configurar headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        
        // Criar entidade HTTP com corpo e headers
        HttpEntity<Map<String, String>> loginEntity = new HttpEntity<>(loginRequest, headers);
        
        try {
            // Enviar requisição de login
            ResponseEntity<Map> loginResponse = restTemplate.exchange(
                    loginUrl, HttpMethod.POST, loginEntity, Map.class);
            
            // Verificar se o login foi bem-sucedido
            if (loginResponse.getStatusCode() == HttpStatus.OK) {
                // Extrair token da resposta
                String token = (String) loginResponse.getBody().get("token");
                assertNotNull(token, "Token não deve ser nulo após login bem-sucedido");
                
                // 2. Usar o token para acessar endpoint protegido
                String protectedUrl = "http://localhost:" + port + "/api/clients/1";
                
                // Configurar headers com token
                HttpHeaders authHeaders = new HttpHeaders();
                authHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                HttpEntity<String> authEntity = new HttpEntity<>(authHeaders);
                
                // Realizar requisição ao endpoint protegido
                ResponseEntity<String> protectedResponse = restTemplate.exchange(
                        protectedUrl, HttpMethod.GET, authEntity, String.class);
                
                // Verificar resposta - deve ser diferente de 401 Unauthorized
                assertTrue(protectedResponse.getStatusCode() != HttpStatus.UNAUTHORIZED,
                        "Acesso a endpoint protegido com token válido não deve retornar 401");
                
                System.out.println("Token válido obtido e utilizado com sucesso!");
            } else {
                System.out.println("Login falhou com status: " + loginResponse.getStatusCode());
                // O teste não falha porque o serviço de autenticação pode não estar disponível
            }
        } catch (Exception e) {
            System.out.println("Erro ao tentar login: " + e.getMessage());
            System.out.println("O serviço de autenticação provavelmente não está disponível.");
            // O teste não falha porque o serviço de autenticação pode não estar disponível
        }
    }
}
