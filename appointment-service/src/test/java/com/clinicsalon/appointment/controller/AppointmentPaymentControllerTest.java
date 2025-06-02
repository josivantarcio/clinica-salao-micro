package com.clinicsalon.appointment.controller;

import com.clinicsalon.appointment.service.AppointmentPaymentService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AppointmentPaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AppointmentPaymentService paymentService;

    @InjectMocks
    private AppointmentPaymentController paymentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
    }

    @Test
    void createPaymentLink_Success() throws Exception {
        // Arrange
        Map<String, String> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("paymentLink", "https://payment.example.com/123456");

        when(paymentService.createPaymentLink(anyLong())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/appointments/payments/1/payment-link")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.paymentLink").value("https://payment.example.com/123456"));
    }

    @Test
    void getPaymentStatus_Success() throws Exception {
        // Arrange
        Map<String, Object> response = new HashMap<>();
        response.put("status", "PAID");
        response.put("paidAt", "2025-06-01T10:15:30");
        response.put("amount", "100.00");

        when(paymentService.getPaymentStatus(anyLong())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/appointments/payments/1/status")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.amount").value("100.00"));
    }

    @Test
    void processRefund_Success() throws Exception {
        // Arrange
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("refundId", "REF123456");
        response.put("amount", "100.00");

        when(paymentService.processRefund(anyLong())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/appointments/payments/1/refund")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.refundId").value("REF123456"));
    }
}
