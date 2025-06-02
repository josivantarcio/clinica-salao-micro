package com.clinicsalon.appointment.controller;

import com.clinicsalon.appointment.service.LoyaltyIntegrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LoyaltyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LoyaltyIntegrationService loyaltyService;

    @InjectMocks
    private LoyaltyController loyaltyController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(loyaltyController).build();
    }

    @Test
    void getPointsBalance_Success() throws Exception {
        // Arrange
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("points", 250);
        response.put("clientId", 1L);

        when(loyaltyService.getPointsBalance(anyLong())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/appointments/loyalty/points/balance/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.points").value(250));
    }

    @Test
    void getLoyaltyAccount_Success() throws Exception {
        // Arrange
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("accountId", "ACC123");
        response.put("clientId", 1L);
        response.put("createdAt", "2025-01-01T10:00:00");

        when(loyaltyService.ensureLoyaltyAccount(anyLong())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/appointments/loyalty/account/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.accountId").value("ACC123"));
    }
}
