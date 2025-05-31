-- Criação da tabela de serviços
CREATE TABLE services (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    duration_minutes INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Criação da tabela de agendamentos
CREATE TABLE appointments (
    id SERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    professional_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Criação da tabela de relacionamento entre agendamentos e serviços
CREATE TABLE appointment_services (
    id SERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    notes VARCHAR(255),
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(id)
);

-- Índices para melhorar a performance das consultas
CREATE INDEX idx_appointments_client_id ON appointments(client_id);
CREATE INDEX idx_appointments_professional_id ON appointments(professional_id);
CREATE INDEX idx_appointments_start_time ON appointments(start_time);
CREATE INDEX idx_appointments_status ON appointments(status);
CREATE INDEX idx_appointment_services_appointment_id ON appointment_services(appointment_id);
CREATE INDEX idx_appointment_services_service_id ON appointment_services(service_id);
CREATE INDEX idx_services_name ON services(name);
CREATE INDEX idx_services_active ON services(active);
