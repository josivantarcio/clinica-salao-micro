package com.clinicsalon.loyalty.service;

import com.clinicsalon.loyalty.dto.LoyaltyAccountRequest;
import com.clinicsalon.loyalty.dto.LoyaltyAccountResponse;
import com.clinicsalon.loyalty.exception.ResourceNotFoundException;
import com.clinicsalon.loyalty.model.LoyaltyAccount;
import com.clinicsalon.loyalty.model.LoyaltyTier;
import com.clinicsalon.loyalty.repository.LoyaltyAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoyaltyAccountServiceTest {

    @Mock
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Mock
    private ClientLookupService clientLookupService;

    @InjectMocks
    private LoyaltyAccountService loyaltyAccountService;

    private LoyaltyAccount testAccount;
    private LoyaltyAccountRequest testRequest;
    private final Long CLIENT_ID = 1L;
    private final String CLIENT_NAME = "João Silva";

    @BeforeEach
    void setUp() {
        // Configurar conta de teste
        testAccount = LoyaltyAccount.builder()
                .id(1L)
                .clientId(CLIENT_ID)
                .pointsBalance(100)
                .lifetimePoints(100)
                .tier(LoyaltyTier.BRONZE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Configurar request de teste
        testRequest = new LoyaltyAccountRequest();
        testRequest.setClientId(CLIENT_ID);
        testRequest.setInitialPoints(100);
    }

    @Test
    void createLoyaltyAccount_Success() {
        // Arrange
        when(clientLookupService.getClientName(CLIENT_ID)).thenReturn(CLIENT_NAME);
        when(loyaltyAccountRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.empty());
        when(loyaltyAccountRepository.save(any(LoyaltyAccount.class))).thenReturn(testAccount);

        // Act
        LoyaltyAccountResponse response = loyaltyAccountService.createLoyaltyAccount(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(CLIENT_ID, response.getClientId());
        assertEquals(CLIENT_NAME, response.getClientName());
        assertEquals(100, response.getPointsBalance());
        assertEquals(LoyaltyTier.BRONZE, response.getTier());
        
        verify(loyaltyAccountRepository).findByClientId(CLIENT_ID);
        verify(loyaltyAccountRepository).save(any(LoyaltyAccount.class));
        verify(clientLookupService).getClientName(CLIENT_ID);
    }

    @Test
    void createLoyaltyAccount_AccountAlreadyExists() {
        // Arrange
        when(clientLookupService.getClientName(CLIENT_ID)).thenReturn(CLIENT_NAME);
        when(loyaltyAccountRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.of(testAccount));

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> 
            loyaltyAccountService.createLoyaltyAccount(testRequest)
        );
        
        assertTrue(exception.getMessage().contains("Loyalty account already exists"));
        verify(loyaltyAccountRepository).findByClientId(CLIENT_ID);
        verify(loyaltyAccountRepository, never()).save(any(LoyaltyAccount.class));
    }

    @Test
    void getLoyaltyAccountByClientId_Success() {
        // Arrange
        when(loyaltyAccountRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.of(testAccount));
        when(clientLookupService.getClientName(CLIENT_ID)).thenReturn(CLIENT_NAME);

        // Act
        LoyaltyAccountResponse response = loyaltyAccountService.getLoyaltyAccountByClientId(CLIENT_ID);

        // Assert
        assertNotNull(response);
        assertEquals(CLIENT_ID, response.getClientId());
        assertEquals(CLIENT_NAME, response.getClientName());
        assertEquals(100, response.getPointsBalance());
        
        verify(loyaltyAccountRepository).findByClientId(CLIENT_ID);
        verify(clientLookupService).getClientName(CLIENT_ID);
    }

    @Test
    void getLoyaltyAccountByClientId_NotFound() {
        // Arrange
        when(loyaltyAccountRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> 
            loyaltyAccountService.getLoyaltyAccountByClientId(CLIENT_ID)
        );
        
        assertTrue(exception.getMessage().contains("Loyalty account not found"));
        verify(loyaltyAccountRepository).findByClientId(CLIENT_ID);
        verify(clientLookupService, never()).getClientName(any());
    }

    @Test
    void getAllLoyaltyAccounts_Success() {
        // Arrange
        LoyaltyAccount account2 = LoyaltyAccount.builder()
                .id(2L)
                .clientId(2L)
                .pointsBalance(200)
                .lifetimePoints(200)
                .tier(LoyaltyTier.SILVER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        List<LoyaltyAccount> accounts = Arrays.asList(testAccount, account2);
        
        when(loyaltyAccountRepository.findAll()).thenReturn(accounts);
        when(clientLookupService.getClientName(1L)).thenReturn("João Silva");
        when(clientLookupService.getClientName(2L)).thenReturn("Maria Oliveira");

        // Act
        List<LoyaltyAccountResponse> responses = loyaltyAccountService.getAllLoyaltyAccounts();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("João Silva", responses.get(0).getClientName());
        assertEquals("Maria Oliveira", responses.get(1).getClientName());
        
        verify(loyaltyAccountRepository).findAll();
        verify(clientLookupService, times(2)).getClientName(any());
    }

    @Test
    void updateTier_Success() {
        // Arrange
        when(loyaltyAccountRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.of(testAccount));
        when(loyaltyAccountRepository.save(any(LoyaltyAccount.class))).thenReturn(testAccount);
        when(clientLookupService.getClientName(CLIENT_ID)).thenReturn(CLIENT_NAME);

        // Act
        LoyaltyAccountResponse response = loyaltyAccountService.updateTier(CLIENT_ID, LoyaltyTier.GOLD);

        // Assert
        assertNotNull(response);
        assertEquals(LoyaltyTier.GOLD, response.getTier());
        
        verify(loyaltyAccountRepository).findByClientId(CLIENT_ID);
        verify(loyaltyAccountRepository).save(any(LoyaltyAccount.class));
        verify(clientLookupService).getClientName(CLIENT_ID);
    }

    @Test
    void updatePointsBalance_AddPoints_Success() {
        // Arrange
        when(loyaltyAccountRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.of(testAccount));
        when(loyaltyAccountRepository.save(any(LoyaltyAccount.class))).thenReturn(testAccount);
        when(clientLookupService.getClientName(CLIENT_ID)).thenReturn(CLIENT_NAME);

        // Act
        LoyaltyAccountResponse response = loyaltyAccountService.updatePointsBalance(CLIENT_ID, 50);

        // Assert
        assertNotNull(response);
        assertEquals(150, response.getPointsBalance());
        
        verify(loyaltyAccountRepository).findByClientId(CLIENT_ID);
        verify(loyaltyAccountRepository).save(any(LoyaltyAccount.class));
        verify(clientLookupService).getClientName(CLIENT_ID);
    }

    @Test
    void updatePointsBalance_SubtractPoints_Success() {
        // Arrange
        when(loyaltyAccountRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.of(testAccount));
        when(loyaltyAccountRepository.save(any(LoyaltyAccount.class))).thenReturn(testAccount);
        when(clientLookupService.getClientName(CLIENT_ID)).thenReturn(CLIENT_NAME);

        // Act
        LoyaltyAccountResponse response = loyaltyAccountService.updatePointsBalance(CLIENT_ID, -50);

        // Assert
        assertNotNull(response);
        assertEquals(50, response.getPointsBalance());
        
        verify(loyaltyAccountRepository).findByClientId(CLIENT_ID);
        verify(loyaltyAccountRepository).save(any(LoyaltyAccount.class));
        verify(clientLookupService).getClientName(CLIENT_ID);
    }

    @Test
    void updatePointsBalance_TierUpgrade_Success() {
        // Arrange
        testAccount.setPointsBalance(1900);
        testAccount.setLifetimePoints(1900);
        testAccount.setTier(LoyaltyTier.BRONZE);
        
        when(loyaltyAccountRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.of(testAccount));
        when(loyaltyAccountRepository.save(any(LoyaltyAccount.class))).thenAnswer(invocation -> {
            LoyaltyAccount savedAccount = invocation.getArgument(0);
            // Verificar se o tier foi atualizado conforme esperado
            assertEquals(LoyaltyTier.SILVER, savedAccount.getTier());
            return savedAccount;
        });
        when(clientLookupService.getClientName(CLIENT_ID)).thenReturn(CLIENT_NAME);

        // Act
        LoyaltyAccountResponse response = loyaltyAccountService.updatePointsBalance(CLIENT_ID, 200);

        // Assert
        assertNotNull(response);
        assertEquals(2100, response.getPointsBalance());
        assertEquals(2100, response.getLifetimePoints());
        assertEquals(LoyaltyTier.SILVER, response.getTier());
        
        verify(loyaltyAccountRepository).findByClientId(CLIENT_ID);
        verify(loyaltyAccountRepository).save(any(LoyaltyAccount.class));
    }
}
