package com.clinicsalon.client.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respostas do serviço de clientes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String cpf;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
