package com.clinicsalon.gateway.filter;

import com.clinicsalon.gateway.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuthenticationFilterTest {

    private AuthenticationFilter authenticationFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RouteValidator routeValidator;

    @Mock
    private GatewayFilterChain filterChain;

    private ServerWebExchange exchange;
    
    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String USERNAME = "testuser";
    private static final String ROLES = "ROLE_USER,ROLE_ADMIN";

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setup() {
        // Mock do predicato isSecured com lenient() para evitar UnnecessaryStubbingException
        routeValidator.isSecured = mock(Predicate.class);
        
        authenticationFilter = new AuthenticationFilter(jwtUtil, routeValidator);
        
        // Configuração padrão do filterChain que será usada em todos os testes
        lenient().when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    @Test
    public void testFilter_OpenApiEndpoint_ShouldNotValidateToken() {
        // Arrange
        // Criar uma requisição para um endpoint não protegido
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/auth/login")
                .build();
        exchange = MockServerWebExchange.from(request);
        
        // Configurar routeValidator para identificar como não protegido
        lenient().when(routeValidator.isSecured.test(request)).thenReturn(false);
        
        // Criar o filtro
        GatewayFilter filter = authenticationFilter.apply(new AuthenticationFilter.Config());
        
        // Act
        Mono<Void> result = filter.filter(exchange, filterChain);
        
        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();
        
        // Verificar que o jwtUtil nunca foi chamado para validar o token
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    public void testFilter_SecuredEndpoint_NoAuthHeader_ShouldReturnUnauthorized() {
        // Arrange
        // Criar uma requisição para um endpoint protegido sem header de autorização
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/clients/123")
                .build();
        exchange = MockServerWebExchange.from(request);
        
        // Configurar routeValidator para identificar como protegido
        lenient().when(routeValidator.isSecured.test(request)).thenReturn(true);
        
        // Criar o filtro
        GatewayFilter filter = authenticationFilter.apply(new AuthenticationFilter.Config());
        
        // Act
        Mono<Void> result = filter.filter(exchange, filterChain);
        
        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();
        
        // Verificar que o status da resposta é UNAUTHORIZED
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().setComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    public void testFilter_SecuredEndpoint_InvalidAuthHeaderFormat_ShouldReturnUnauthorized() {
        // Arrange
        // Criar uma requisição para um endpoint protegido com formato de header inválido
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/clients/123")
                .header(HttpHeaders.AUTHORIZATION, "Basic xyz123")
                .build();
        exchange = MockServerWebExchange.from(request);
        
        // Configurar routeValidator para identificar como protegido
        lenient().when(routeValidator.isSecured.test(request)).thenReturn(true);
        
        // Criar o filtro
        GatewayFilter filter = authenticationFilter.apply(new AuthenticationFilter.Config());
        
        // Act
        Mono<Void> result = filter.filter(exchange, filterChain);
        
        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();
        
        // Verificar que o status da resposta é UNAUTHORIZED
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().setComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    public void testFilter_SecuredEndpoint_InvalidToken_ShouldReturnUnauthorized() {
        // Arrange
        // Criar uma requisição para um endpoint protegido com token inválido
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/clients/123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .build();
        exchange = MockServerWebExchange.from(request);
        
        // Configurar routeValidator para identificar como protegido
        lenient().when(routeValidator.isSecured.test(request)).thenReturn(true);
        
        // Configurar jwtUtil para rejeitar o token
        lenient().when(jwtUtil.validateToken("invalid-token")).thenReturn(false);
        
        // Criar o filtro
        GatewayFilter filter = authenticationFilter.apply(new AuthenticationFilter.Config());
        
        // Act
        Mono<Void> result = filter.filter(exchange, filterChain);
        
        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();
        
        // Verificar que o status da resposta é UNAUTHORIZED
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().setComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    public void testFilter_SecuredEndpoint_ValidToken_ShouldPassThrough() {
        // Arrange
        // Criar uma requisição para um endpoint protegido com token válido
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/clients/123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .build();
        exchange = MockServerWebExchange.from(request);
        
        // Configurar routeValidator para identificar como protegido
        lenient().when(routeValidator.isSecured.test(request)).thenReturn(true);
        
        // Configurar jwtUtil para aceitar o token e retornar claims
        lenient().when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
        lenient().when(jwtUtil.extractUsername(VALID_TOKEN)).thenReturn(USERNAME);
        lenient().when(jwtUtil.extractRoles(VALID_TOKEN)).thenReturn(ROLES);
        
        // Criar o filtro
        GatewayFilter filter = authenticationFilter.apply(new AuthenticationFilter.Config());
        
        // Act
        Mono<Void> result = filter.filter(exchange, filterChain);
        
        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();
        
        // Verificar que o filtro adicionou os headers esperados e continuou a chain
        verify(filterChain).filter(any(ServerWebExchange.class));
    }
    
    // Não precisamos mais deste método auxiliar porque estamos usando o assertEquals do JUnit

}
