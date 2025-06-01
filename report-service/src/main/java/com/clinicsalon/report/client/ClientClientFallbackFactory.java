package com.clinicsalon.report.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ClientClientFallbackFactory implements FallbackFactory<ClientClient> {

    @Override
    public ClientClient create(Throwable cause) {
        return new ClientClientFallback(cause);
    }

    @Slf4j
    static class ClientClientFallback implements ClientClient {
        private final Throwable cause;

        ClientClientFallback(Throwable cause) {
            this.cause = cause;
        }

        @Override
        public Optional<ClientDto> getClientById(Long clientId) {
            log.error("Fallback para getClientById. ClientId: {}, erro: {}", clientId, cause.getMessage());
            return Optional.empty();
        }

        @Override
        public List<ClientDto> getAllClients() {
            log.error("Fallback para getAllClients. Erro: {}", cause.getMessage());
            return Collections.emptyList();
        }

        @Override
        public String getClientName(Long clientId) {
            log.error("Fallback para getClientName. ClientId: {}, erro: {}", clientId, cause.getMessage());
            return "Cliente não encontrado";
        }
    }
}
