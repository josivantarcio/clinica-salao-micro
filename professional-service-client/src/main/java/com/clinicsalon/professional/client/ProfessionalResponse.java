package com.clinicsalon.professional.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalResponse {
    private Long id;
    private String name;
    private String role;
    private String specialization;
    private String email;
    private String phone;
    private String cpf;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
