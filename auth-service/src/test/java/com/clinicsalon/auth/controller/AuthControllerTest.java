package com.clinicsalon.auth.controller;

import com.clinicsalon.auth.dto.AuthRequest;
import com.clinicsalon.auth.dto.AuthResponse;
import com.clinicsalon.auth.dto.RegisterRequest;
import com.clinicsalon.auth.exception.AuthenticationException;
import com.clinicsalon.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private final UUID TEST_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new RestExceptionHandler()) // Adiciona um manipulador de exceções global
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        // Prepare test data
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");

        AuthResponse expectedResponse = AuthResponse.builder()
                .token("jwt-token")
                .userId(TEST_UUID)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .roles(Collections.singletonList("ROLE_USER"))
                .build();

        // Mock service behavior
        when(authService.register(any(RegisterRequest.class))).thenReturn(expectedResponse);

        // Perform the test
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.userId").value(TEST_UUID.toString()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    public void testLoginSuccess() throws Exception {
        // Prepare test data
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password123");

        AuthResponse expectedResponse = AuthResponse.builder()
                .token("jwt-token")
                .userId(TEST_UUID)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .roles(Collections.singletonList("ROLE_USER"))
                .build();

        // Mock service behavior
        when(authService.authenticate(any(AuthRequest.class))).thenReturn(expectedResponse);

        // Perform the test
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.userId").value(TEST_UUID.toString()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    public void testLoginFailure() throws Exception {
        // Prepare test data
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("wrongpassword");

        // Mock service behavior
        when(authService.authenticate(any(AuthRequest.class)))
                .thenThrow(new AuthenticationException("Invalid credentials"));

        // Perform the test
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
    }
}
