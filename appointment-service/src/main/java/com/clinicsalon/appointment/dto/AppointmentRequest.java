package com.clinicsalon.appointment.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequest {
    
    @NotNull(message = "O ID do cliente é obrigatório")
    @Positive(message = "O ID do cliente deve ser positivo")
    private Long clientId;
    
    @NotNull(message = "O ID do profissional é obrigatório")
    @Positive(message = "O ID do profissional deve ser positivo")
    private Long professionalId;
    
    @NotNull(message = "A data/hora de início é obrigatória")
    private LocalDateTime startTime;
    
    @NotNull(message = "A data/hora de fim é obrigatória")
    private LocalDateTime endTime;
    
    @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres")
    private String notes;
    
    @NotEmpty(message = "É necessário informar pelo menos um serviço")
    private List<AppointmentServiceRequest> services;
    
    // Getters explícitos para garantir compatibilidade
    public Long getClientId() {
        return clientId;
    }
    
    public Long getProfessionalId() {
        return professionalId;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public List<AppointmentServiceRequest> getServices() {
        return services;
    }
    
    public String getNotes() {
        return notes;
    }
}
