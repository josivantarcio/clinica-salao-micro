package com.clinicsalon.report.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO que representa um profissional no sistema.
 * Usado para comunicação com o serviço de profissionais via Feign.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalDto {
    private Long id;
    private String name;
    private String role;
    private String specialization;
    private String bio;
    private String email;
    private String phone;
    private String cpf;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Campo adicional usado apenas para relatórios
    private List<String> services;
}
