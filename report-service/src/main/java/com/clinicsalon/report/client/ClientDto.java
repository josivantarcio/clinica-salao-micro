package com.clinicsalon.report.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String cpf;
    private LocalDate birthDate;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
