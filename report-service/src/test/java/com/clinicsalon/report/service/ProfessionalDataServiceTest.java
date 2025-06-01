package com.clinicsalon.report.service;

import com.clinicsalon.report.client.ProfessionalClient;
import com.clinicsalon.report.client.ProfessionalDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfessionalDataServiceTest {

    @Mock
    private ProfessionalClient professionalClient;

    @InjectMocks
    private ProfessionalDataService professionalDataService;

    private ProfessionalDto professionalDto;
    private final Long professionalId = 1L;

    @BeforeEach
    void setUp() {
        professionalDto = ProfessionalDto.builder()
                .id(professionalId)
                .name("João Silva")
                .email("joao.silva@example.com")
                .phone("11987654321")
                .specialization("Cabeleireiro")
                .active(true)
                .createdAt(LocalDateTime.now())
                .services(Arrays.asList("Corte Masculino", "Barba"))
                .build();
    }

    @Test
    void getProfessionalById_Success() {
        // Arrange
        when(professionalClient.getProfessionalById(professionalId)).thenReturn(Optional.of(professionalDto));

        // Act
        ProfessionalDto result = professionalDataService.getProfessionalById(professionalId);

        // Assert
        assertNotNull(result);
        assertEquals(professionalDto.getId(), result.getId());
        assertEquals(professionalDto.getName(), result.getName());
        assertEquals(professionalDto.getSpecialization(), result.getSpecialization());
        verify(professionalClient, times(1)).getProfessionalById(professionalId);
    }

    @Test
    void getProfessionalById_NotFound_ShouldThrowException() {
        // Arrange
        when(professionalClient.getProfessionalById(professionalId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            professionalDataService.getProfessionalById(professionalId);
        });

        assertTrue(exception.getMessage().contains("Profissional não encontrado"));
        verify(professionalClient, times(1)).getProfessionalById(professionalId);
    }

    @Test
    void getAllProfessionals_Success() {
        // Arrange
        ProfessionalDto secondProfessional = ProfessionalDto.builder()
                .id(2L)
                .name("Maria Oliveira")
                .specialization("Manicure")
                .active(true)
                .build();
        List<ProfessionalDto> professionals = Arrays.asList(professionalDto, secondProfessional);
        
        when(professionalClient.getAllProfessionals()).thenReturn(professionals);

        // Act
        List<ProfessionalDto> result = professionalDataService.getAllProfessionals();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("João Silva", result.get(0).getName());
        assertEquals("Maria Oliveira", result.get(1).getName());
        verify(professionalClient, times(1)).getAllProfessionals();
    }

    @Test
    void getAllProfessionals_EmptyList() {
        // Arrange
        when(professionalClient.getAllProfessionals()).thenReturn(Collections.emptyList());

        // Act
        List<ProfessionalDto> result = professionalDataService.getAllProfessionals();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(professionalClient, times(1)).getAllProfessionals();
    }

    @Test
    void getProfessionalName_Success() {
        // Arrange
        String expectedName = "João Silva";
        when(professionalClient.getProfessionalName(professionalId)).thenReturn(expectedName);

        // Act
        String result = professionalDataService.getProfessionalName(professionalId);

        // Assert
        assertEquals(expectedName, result);
        verify(professionalClient, times(1)).getProfessionalName(professionalId);
    }

    @Test
    void getProfessionalsBySpecialization_Success() {
        // Arrange
        String specialization = "Cabeleireiro";
        List<ProfessionalDto> professionals = Collections.singletonList(professionalDto);
        
        when(professionalClient.getProfessionalsBySpecialization(specialization)).thenReturn(professionals);

        // Act
        List<ProfessionalDto> result = professionalDataService.getProfessionalsBySpecialization(specialization);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(specialization, result.get(0).getSpecialization());
        verify(professionalClient, times(1)).getProfessionalsBySpecialization(specialization);
    }

    @Test
    void testFallbackMethods() {
        // Arrange
        Exception exception = new RuntimeException("Service unavailable");

        // Act & Assert for getProfessionalByIdFallback
        ProfessionalDto fallbackResult = professionalDataService.getProfessionalByIdFallback(professionalId, exception);
        assertNotNull(fallbackResult);
        assertEquals(professionalId, fallbackResult.getId());
        assertTrue(fallbackResult.getName().contains("Indisponível"));

        // Act & Assert for getAllProfessionalsFallback
        List<ProfessionalDto> allFallbackResult = professionalDataService.getAllProfessionalsFallback(exception);
        assertNotNull(allFallbackResult);
        assertTrue(allFallbackResult.isEmpty());

        // Act & Assert for getProfessionalNameFallback
        String nameFallbackResult = professionalDataService.getProfessionalNameFallback(professionalId, exception);
        assertNotNull(nameFallbackResult);
        assertTrue(nameFallbackResult.contains("Profissional #" + professionalId));

        // Act & Assert for getProfessionalsBySpecializationFallback
        List<ProfessionalDto> specializationFallbackResult = 
            professionalDataService.getProfessionalsBySpecializationFallback("Cabeleireiro", exception);
        assertNotNull(specializationFallbackResult);
        assertTrue(specializationFallbackResult.isEmpty());
    }
}
