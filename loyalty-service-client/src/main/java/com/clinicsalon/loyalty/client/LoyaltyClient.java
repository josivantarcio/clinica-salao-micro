package com.clinicsalon.loyalty.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Cliente Feign para comunicação com o serviço de fidelidade
 */
@FeignClient(name = "loyalty-service", path = "/api/v1/loyalty")
public interface LoyaltyClient {

    /**
     * Buscar a conta de fidelidade pelo ID do cliente
     *
     * @param clientId ID do cliente
     * @return Resposta contendo os dados da conta de fidelidade
     */
    @GetMapping("/accounts/{clientId}")
    ResponseEntity<LoyaltyResponse> getLoyaltyAccountByClientId(@PathVariable("clientId") Long clientId);

    /**
     * Adicionar ou remover pontos do saldo de um cliente
     *
     * @param clientId    ID do cliente
     * @param pointsDelta Quantidade de pontos a adicionar (positivo) ou remover (negativo)
     * @return Resposta contendo os dados atualizados da conta de fidelidade
     */
    @PatchMapping("/accounts/{clientId}/points")
    ResponseEntity<LoyaltyResponse> updatePointsBalance(
            @PathVariable("clientId") Long clientId, 
            @RequestParam("pointsDelta") Integer pointsDelta);
}
