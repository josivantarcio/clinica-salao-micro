package com.clinicsalon.professional.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDateTime;

@Entity
@Table(name = "professionals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Professional {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Column(nullable = false)
    private String name;

    @Column
    private String role;
    
    @Column
    private String specialization;
    
    @Column
    private String bio;
    
    @Email(message = "O email deve ser válido")
    @Column(unique = true)
    private String email;

    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{5}-\\d{4}", message = "O telefone deve estar no formato (99) 99999-9999")
    @Column
    private String phone;

    @CPF(message = "CPF inválido")
    @Column(unique = true)
    private String cpf;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
