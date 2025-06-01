package com.clinicsalon.finance.service;

import java.util.Map;
import java.util.UUID;

/**
 * Serviço responsável por integração com outros microsserviços
 */
public interface IntegrationService {

    /**
     * Busca informações do cliente pelo ID
     * @param clientId ID do cliente
     * @return Mapa com informações do cliente
     */
    Map<String, Object> getClientInfo(UUID clientId);
    
    /**
     * Busca informações do agendamento pelo ID
     * @param appointmentId ID do agendamento
     * @return Mapa com informações do agendamento
     */
    Map<String, Object> getAppointmentInfo(UUID appointmentId);
    
    /**
     * Busca detalhes do serviço para um agendamento
     * @param appointmentId ID do agendamento
     * @return Mapa com detalhes do serviço
     */
    Map<String, Object> getServiceDetailsForAppointment(UUID appointmentId);
}
