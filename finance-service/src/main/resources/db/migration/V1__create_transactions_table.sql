CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    appointment_id UUID NOT NULL,
    client_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(50),
    payment_gateway_id VARCHAR(100),
    invoice_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    description VARCHAR(255)
);

CREATE INDEX idx_transactions_client_id ON transactions (client_id);
CREATE INDEX idx_transactions_appointment_id ON transactions (appointment_id);
CREATE INDEX idx_transactions_status ON transactions (status);
CREATE INDEX idx_transactions_created_at ON transactions (created_at);
