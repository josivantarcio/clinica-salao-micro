package com.clinicsalon.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RouteValidatorTest {

    // Não usamos @InjectMocks para evitar erros de compilação relacionados a variáveis final
    private RouteValidator routeValidator;

    @BeforeEach
    public void setup() {
        // Criamos manualmente a instância
        routeValidator = new RouteValidator();
    }

    @Test
    public void testIsSecured_SecuredEndpoint() {
        // Arrange - Criar uma request para um endpoint protegido
        ServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/clients/123")
                .build();
        
        // Act
        boolean isSecured = routeValidator.isSecured.test(request);
        
        // Assert
        assertTrue(isSecured, "O endpoint /clients/123 deve ser protegido");
    }
    
    @Test
    public void testIsSecured_LoginEndpoint() {
        // Arrange - Criar uma request para o endpoint de login (não protegido)
        ServerHttpRequest request = MockServerHttpRequest
                .post("http://localhost:8080/auth/login")
                .build();
        
        // Act
        boolean isSecured = routeValidator.isSecured.test(request);
        
        // Assert
        assertFalse(isSecured, "O endpoint /auth/login não deve ser protegido");
    }
    
    @Test
    public void testIsSecured_RegisterEndpoint() {
        // Arrange - Criar uma request para o endpoint de registro (não protegido)
        ServerHttpRequest request = MockServerHttpRequest
                .post("http://localhost:8080/auth/register")
                .build();
        
        // Act
        boolean isSecured = routeValidator.isSecured.test(request);
        
        // Assert
        assertFalse(isSecured, "O endpoint /auth/register não deve ser protegido");
    }
    
    @Test
    public void testIsSecured_ActuatorEndpoint() {
        // Arrange - Criar uma request para um endpoint do Actuator (não protegido)
        ServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/actuator/health")
                .build();
        
        // Act
        boolean isSecured = routeValidator.isSecured.test(request);
        
        // Assert
        assertFalse(isSecured, "O endpoint /actuator/health não deve ser protegido");
    }
    
    @Test
    public void testIsSecured_EurekaEndpoint() {
        // Arrange - Criar uma request para um endpoint do Eureka (não protegido)
        ServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/eureka/apps")
                .build();
        
        // Act
        boolean isSecured = routeValidator.isSecured.test(request);
        
        // Assert
        assertFalse(isSecured, "O endpoint /eureka/apps não deve ser protegido");
    }
}
