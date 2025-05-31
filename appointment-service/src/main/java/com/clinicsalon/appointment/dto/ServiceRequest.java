package com.clinicsalon.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {
    
    @NotBlank(message = "O nome do serviço é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    private String name;
    
    @NotBlank(message = "A descrição do serviço é obrigatória")
    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
    private String description;
    
    @NotNull(message = "O preço do serviço é obrigatório")
    @Positive(message = "O preço do serviço deve ser positivo")
    private BigDecimal price;
    
    @NotNull(message = "A duração do serviço é obrigatória")
    @Positive(message = "A duração do serviço deve ser positiva")
    private Integer durationMinutes;
    
    private Boolean active;
}
