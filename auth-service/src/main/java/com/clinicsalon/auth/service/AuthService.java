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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthenticationException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthenticationException("Email already in use");
        }
        
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new AuthenticationException("Default role not found"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .active(true)
                .roles(new HashSet<>(Collections.singletonList(userRole)))
                .createdAt(LocalDateTime.now())
                .build();
        
        User savedUser = userRepository.save(user);
        
        String jwtToken = jwtService.generateToken(user);
        
        return buildAuthResponse(savedUser, jwtToken, null);
    }

    public AuthResponse authenticate(AuthRequest request) {
        log.info("Authenticating user: {}", request.getUsername());
        
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("User not found"));
        
        String jwtToken = jwtService.generateToken(user);
        
        return buildAuthResponse(user, jwtToken, null);
    }
    
    private AuthResponse buildAuthResponse(User user, String token, String refreshToken) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .build();
    }
}
