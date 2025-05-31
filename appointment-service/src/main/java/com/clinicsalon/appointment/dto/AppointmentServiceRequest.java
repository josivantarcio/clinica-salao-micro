package com.clinicsalon.appointment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentServiceRequest {
    
    @NotNull(message = "O ID do serviço é obrigatório")
    @Positive(message = "O ID do serviço deve ser positivo")
    private Long serviceId;
    
    @Size(max = 255, message = "As observações do serviço devem ter no máximo 255 caracteres")
    private String notes;
    
    // Getter explícito
    public Long getServiceId() {
        return serviceId;
    }
}
