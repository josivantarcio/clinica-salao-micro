package com.clinicsalon.auth.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private UserDetails userDetails;
    private String testSecretKey = "testsecretkeythatshouldbelongerthanthisinproduction";
    private long testExpiration = 3600000; // 1 hour

    @BeforeEach
    public void setup() {
        // Set up the JWT service with test values
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", testExpiration);
        jwtService.init(); // Call the init method to set up the key

        // Create a test user
        userDetails = new User(
            "test@example.com",
            "password",
            Collections.emptyList()
        );
    }

    @Test
    public void testGenerateToken() {
        // Generate a token
        String token = jwtService.generateToken(userDetails);

        // Verify token is not null or empty
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void testGenerateTokenWithExtraClaims() {
        // Create extra claims
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");

        // Generate a token with extra claims
        String token = jwtService.generateToken(extraClaims, userDetails);

        // Verify token is not null or empty
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Verify the claim is in the token
        assertEquals("ADMIN", jwtService.extractClaim(token, claims -> claims.get("role")));
    }

    @Test
    public void testExtractUsername() {
        // Generate a token
        String token = jwtService.generateToken(userDetails);

        // Extract the username
        String username = jwtService.extractUsername(token);

        // Verify the username matches
        assertEquals(userDetails.getUsername(), username);
    }

    @Test
    public void testIsTokenValid() {
        // Generate a token
        String token = jwtService.generateToken(userDetails);

        // Verify the token is valid
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    public void testIsTokenValidWithWrongUser() {
        // Generate a token
        String token = jwtService.generateToken(userDetails);

        // Create a different user
        UserDetails wrongUser = new User(
            "wrong@example.com",
            "password",
            Collections.emptyList()
        );

        // Verify the token is not valid for the wrong user
        assertFalse(jwtService.isTokenValid(token, wrongUser));
    }

    @Test
    public void testIsTokenExpired() {
        // Cria um token que já está expirado (expiração = -1000ms, ou seja, 1 segundo no passado)
        String token = jwtService.generateTokenWithCustomExpiration(userDetails, -1000);
        
        // Verifica que o token já está expirado
        assertThrows(ExpiredJwtException.class, () -> {
            // Ao tentar extrair informações de um token expirado, deve lançar ExpiredJwtException
            jwtService.extractUsername(token);
        });
    }
}
