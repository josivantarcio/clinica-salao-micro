package com.clinicsalon.appointment.model;

public enum AppointmentStatus {
    PENDING,      // Aguardando confirmação
    SCHEDULED,    // Agendado
    CONFIRMED,    // Confirmado
    IN_PROGRESS,  // Em andamento
    COMPLETED,    // Concluído
    CANCELLED,    // Cancelado
    NO_SHOW       // Cliente não compareceu
}
