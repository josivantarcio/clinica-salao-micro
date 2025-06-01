package com.clinicsalon.loyalty.service;

import com.clinicsalon.client.client.ClientClient;
import com.clinicsalon.client.client.ClientResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientLookupServiceTest {

    @Mock
    private ClientClient clientClient;

    @InjectMocks
    private ClientLookupService clientLookupService;

    private final Long CLIENT_ID = 1L;
    private ClientResponse clientResponse;

    @BeforeEach
    void setUp() {
        clientResponse = ClientResponse.builder()
                .id(CLIENT_ID)
                .name("João Silva")
                .email("joao.silva@email.com")
                .phone("(11) 98765-4321")
                .build();
    }

    @Test
    void getClientName_Success() {
        // Arrange
        when(clientClient.getClientById(CLIENT_ID)).thenReturn(ResponseEntity.ok(clientResponse));

        // Act
        String clientName = clientLookupService.getClientName(CLIENT_ID);

        // Assert
        assertEquals("João Silva", clientName);
        verify(clientClient).getClientById(CLIENT_ID);
    }

    @Test
    void getClientName_ClientServiceUnavailable_ReturnsFallback() {
        // Arrange
        when(clientClient.getClientById(CLIENT_ID)).thenThrow(new RuntimeException("Service unavailable"));

        // Act
        String clientName = clientLookupService.getClientName(CLIENT_ID);

        // Assert
        assertEquals("Cliente #" + CLIENT_ID, clientName);
        verify(clientClient).getClientById(CLIENT_ID);
    }
}
