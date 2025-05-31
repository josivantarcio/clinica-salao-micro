package com.clinicsalon.auth.service;

import com.clinicsalon.auth.dto.AuthRequest;
import com.clinicsalon.auth.dto.AuthResponse;
import com.clinicsalon.auth.dto.RegisterRequest;
import com.clinicsalon.auth.exception.AuthenticationException;
import com.clinicsalon.auth.model.Role;
import com.clinicsalon.auth.model.User;
import com.clinicsalon.auth.repository.RoleRepository;
import com.clinicsalon.auth.repository.UserRepository;
import com.clinicsalon.auth.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private AuthRequest authRequest;
    private User user;
    private Role role;
    private final UUID TEST_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @BeforeEach
    public void setUp() {
        // Set up test data
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");

        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password123");

        role = new Role();
        role.setId(UUID.fromString("223e4567-e89b-12d3-a456-426614174000"));
        role.setName("ROLE_USER");

        user = User.builder()
                .id(TEST_UUID)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .active(true)
                .roles(new HashSet<>(Collections.singletonList(role)))
                .build();
    }

    @Test
    public void testRegisterSuccess() {
        // Mock repository behaviors
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // Call the service method
        AuthResponse response = authService.register(registerRequest);

        // Verify the response
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(TEST_UUID, response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test", response.getFirstName());
        assertEquals("User", response.getLastName());
        assertEquals(Collections.singletonList("ROLE_USER"), response.getRoles());

        // Verify that the repositories were called correctly
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(roleRepository, times(1)).findByName("ROLE_USER");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtService, times(1)).generateToken(any(User.class));
    }

    @Test
    public void testRegisterUsernameExists() {
        // Mock repository behavior
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Verify that the service throws an exception
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Username already exists", exception.getMessage());

        // Verify that only the necessary repositories were called
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(roleRepository, never()).findByName(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testRegisterEmailExists() {
        // Mock repository behaviors
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Verify that the service throws an exception
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email already in use", exception.getMessage());

        // Verify that only the necessary repositories were called
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(roleRepository, never()).findByName(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testAuthenticateSuccess() {
        // Mock repository and service behaviors
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        // Call the service method
        AuthResponse response = authService.authenticate(authRequest);

        // Verify the response
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(TEST_UUID, response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test", response.getFirstName());
        assertEquals("User", response.getLastName());
        assertEquals(Collections.singletonList("ROLE_USER"), response.getRoles());

        // Verify that the repositories and services were called correctly
        verify(authenticationManager, times(1)).authenticate(
                new UsernamePasswordAuthenticationToken("testuser", "password123"));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(jwtService, times(1)).generateToken(user);
    }

    @Test
    public void testAuthenticateUserNotFound() {
        // Mock repository behavior
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        
        // Verify that the service throws an exception
        assertThrows(AuthenticationException.class, () -> {
            // Mock authentication manager to not throw exception when called
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
            authService.authenticate(authRequest);
        });
    }
}
