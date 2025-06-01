package com.clinicsalon.client.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente Feign para comunicação com o serviço de clientes
 */
@FeignClient(name = "client-service", path = "/api/v1/clients")
public interface ClientClient {

    /**
     * Buscar cliente pelo ID
     *
     * @param id ID do cliente
     * @return Resposta contendo os dados do cliente
     */
    @GetMapping("/{id}")
    ResponseEntity<ClientResponse> getClientById(@PathVariable("id") Long id);
    
    /**
     * Verificar se um cliente existe pelo ID
     *
     * @param id ID do cliente
     * @return Resposta True se existe, False caso contrário
     */
    @GetMapping("/{id}/exists")
    ResponseEntity<Boolean> clientExists(@PathVariable("id") Long id);
}
