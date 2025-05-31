package com.clinicsalon.gateway.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    // Removido SECRET_KEY não utilizado e substituído pelo uso de Keys.secretKeyFor
    private static final String USERNAME = "testuser";
    private Key key;

    @BeforeEach
    public void setup() {
        // Criar uma chave segura para testes usando o utilitário Keys
        key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        
        // Configurar o jwtUtil com a chave de teste
        ReflectionTestUtils.setField(jwtUtil, "secretKey", encodedKey);
    }

    @Test
    public void testValidateToken_ValidToken() {
        // Arrange - Criar um token válido
        String token = createValidToken();
        
        // Act & Assert
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    public void testValidateToken_InvalidToken() {
        // Arrange - Token mal formado
        String invalidToken = "invalid.token.format";
        
        // Act & Assert
        assertFalse(jwtUtil.validateToken(invalidToken));
    }

    @Test
    public void testExtractUsername() {
        // Arrange
        String token = createValidToken();
        
        // Act
        String username = jwtUtil.extractUsername(token);
        
        // Assert
        assertEquals(USERNAME, username);
    }

    @Test
    public void testExtractRoles() {
        // Arrange - Criar token com roles
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", List.of("ROLE_USER", "ROLE_ADMIN"));
        String token = createTokenWithClaims(claims);
        
        // Act
        String roles = jwtUtil.extractRoles(token);
        
        // Assert
        assertEquals("ROLE_USER,ROLE_ADMIN", roles);
    }

    @Test
    public void testExtractRoles_NoRoles() {
        // Arrange - Criar token sem roles
        String token = createValidToken();
        
        // Act
        String roles = jwtUtil.extractRoles(token);
        
        // Assert
        assertEquals("", roles);
    }

    @Test
    public void testIsTokenExpired() {
        // Arrange - Criar um token que já está expirado (expiração = -1000ms, ou seja, 1 segundo no passado)
        String token = createExpiredToken();
        
        // Act & Assert
        assertThrows(ExpiredJwtException.class, () -> {
            // Tentar extrair informações deve lançar ExpiredJwtException
            jwtUtil.extractUsername(token);
        });
    }

    // Métodos auxiliares para criar tokens
    private String createValidToken() {
        return Jwts.builder()
                .setSubject(USERNAME)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hora
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String createTokenWithClaims(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(USERNAME)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hora
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String createExpiredToken() {
        return Jwts.builder()
                .setSubject(USERNAME)
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000)) // 2 segundos atrás
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // 1 segundo atrás (já expirado)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
