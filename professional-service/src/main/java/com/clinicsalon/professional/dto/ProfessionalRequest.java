package com.clinicsalon.professional.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.br.CPF;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalRequest {
    
    @NotBlank(message = "O nome é obrigatório")
    private String name;
    
    private String role;
    
    private String specialization;
    
    private String bio;
    
    @Email(message = "O email deve ser válido")
    private String email;
    
    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{5}-\\d{4}", message = "O telefone deve estar no formato (99) 99999-9999")
    private String phone;
    
    @CPF(message = "CPF inválido")
    private String cpf;
}
