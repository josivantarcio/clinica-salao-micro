package com.clinicsalon.loyalty.controller;

import com.clinicsalon.loyalty.dto.LoyaltyAccountRequest;
import com.clinicsalon.loyalty.dto.LoyaltyAccountResponse;
import com.clinicsalon.loyalty.model.LoyaltyTier;
import com.clinicsalon.loyalty.service.LoyaltyAccountService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class LoyaltyAccountControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private LoyaltyAccountController loyaltyAccountController;
    
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private LoyaltyAccountService loyaltyAccountService;

    private LoyaltyAccountRequest accountRequest;
    private LoyaltyAccountResponse accountResponse;
    private final Long CLIENT_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(loyaltyAccountController).build();
        // Configurar request
        accountRequest = new LoyaltyAccountRequest();
        accountRequest.setClientId(CLIENT_ID);
        accountRequest.setInitialPoints(100);

        // Configurar response
        accountResponse = LoyaltyAccountResponse.builder()
                .id(1L)
                .clientId(CLIENT_ID)
                .clientName("João Silva")
                .pointsBalance(100)
                .lifetimePoints(100)
                .tier(LoyaltyTier.BRONZE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createLoyaltyAccount_Success() throws Exception {
        when(loyaltyAccountService.createLoyaltyAccount(any(LoyaltyAccountRequest.class))).thenReturn(accountResponse);

        mockMvc.perform(post("/api/v1/loyalty/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.clientId", is(CLIENT_ID.intValue())))
                .andExpect(jsonPath("$.clientName", is("João Silva")))
                .andExpect(jsonPath("$.pointsBalance", is(100)))
                .andExpect(jsonPath("$.tier", is("BRONZE")));
    }

    @Test
    void getLoyaltyAccountByClientId_Success() throws Exception {
        when(loyaltyAccountService.getLoyaltyAccountByClientId(CLIENT_ID)).thenReturn(accountResponse);

        mockMvc.perform(get("/api/v1/loyalty/accounts/{clientId}", CLIENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.clientId", is(CLIENT_ID.intValue())))
                .andExpect(jsonPath("$.clientName", is("João Silva")))
                .andExpect(jsonPath("$.pointsBalance", is(100)))
                .andExpect(jsonPath("$.tier", is("BRONZE")));
    }

    @Test
    void getAllLoyaltyAccounts_Success() throws Exception {
        LoyaltyAccountResponse account2 = LoyaltyAccountResponse.builder()
                .id(2L)
                .clientId(2L)
                .clientName("Maria Oliveira")
                .pointsBalance(200)
                .lifetimePoints(200)
                .tier(LoyaltyTier.SILVER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<LoyaltyAccountResponse> accounts = Arrays.asList(accountResponse, account2);

        when(loyaltyAccountService.getAllLoyaltyAccounts()).thenReturn(accounts);

        mockMvc.perform(get("/api/v1/loyalty/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].clientName", is("João Silva")))
                .andExpect(jsonPath("$[1].clientName", is("Maria Oliveira")))
                .andExpect(jsonPath("$[0].tier", is("BRONZE")))
                .andExpect(jsonPath("$[1].tier", is("SILVER")));
    }

    @Test
    void updateTier_Success() throws Exception {
        LoyaltyAccountResponse updatedResponse = LoyaltyAccountResponse.builder()
                .id(1L)
                .clientId(CLIENT_ID)
                .clientName("João Silva")
                .pointsBalance(100)
                .lifetimePoints(100)
                .tier(LoyaltyTier.GOLD)  // Tier atualizado
                .createdAt(accountResponse.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(loyaltyAccountService.updateTier(eq(CLIENT_ID), eq(LoyaltyTier.GOLD))).thenReturn(updatedResponse);

        mockMvc.perform(patch("/api/v1/loyalty/accounts/{clientId}/tier", CLIENT_ID)
                .param("tier", "GOLD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tier", is("GOLD")));
    }

    @Test
    void updatePointsBalance_Success() throws Exception {
        LoyaltyAccountResponse updatedResponse = LoyaltyAccountResponse.builder()
                .id(1L)
                .clientId(CLIENT_ID)
                .clientName("João Silva")
                .pointsBalance(150)  // Saldo atualizado
                .lifetimePoints(150)
                .tier(LoyaltyTier.BRONZE)
                .createdAt(accountResponse.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(loyaltyAccountService.updatePointsBalance(eq(CLIENT_ID), eq(50))).thenReturn(updatedResponse);

        mockMvc.perform(patch("/api/v1/loyalty/accounts/{clientId}/points", CLIENT_ID)
                .param("pointsDelta", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointsBalance", is(150)));
    }
}
