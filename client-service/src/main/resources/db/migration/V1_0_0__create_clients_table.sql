-- Create clients table
CREATE TABLE IF NOT EXISTS clients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    birth_date DATE,
    cpf VARCHAR(14) UNIQUE,
    address TEXT,
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_clients_email ON clients (email);
CREATE INDEX IF NOT EXISTS idx_clients_cpf ON clients (cpf);
CREATE INDEX IF NOT EXISTS idx_clients_phone ON clients (phone);
CREATE INDEX IF NOT EXISTS idx_clients_active ON clients (active);

-- Add comments to the table and columns
COMMENT ON TABLE clients IS 'Stores client information for the clinic';
COMMENT ON COLUMN clients.id IS 'Primary key';
COMMENT ON COLUMN clients.name IS 'Full name of the client';
COMMENT ON COLUMN clients.email IS 'Email address of the client (must be unique)';
COMMENT ON COLUMN clients.phone IS 'Phone number of the client';
COMMENT ON COLUMN clients.birth_date IS 'Date of birth of the client';
COMMENT ON COLUMN clients.cpf IS 'CPF (Brazilian individual taxpayer registry number) of the client';
COMMENT ON COLUMN clients.address IS 'Full address of the client';
COMMENT ON COLUMN clients.notes IS 'Additional notes about the client';
COMMENT ON COLUMN clients.active IS 'Indicates if the client is active (true) or not (false)';
COMMENT ON COLUMN clients.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN clients.updated_at IS 'Timestamp when the record was last updated';
