-- Inserir serviços iniciais
INSERT INTO services (name, description, price, duration_minutes, active, created_at, updated_at)
VALUES 
    ('Corte de Cabelo Feminino', 'Corte modelador feminino com finalização', 80.00, 60, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Corte de Cabelo Masculino', 'Corte moderno masculino com finalização', 50.00, 30, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Coloração', 'Aplicação de tintura com produtos de qualidade', 150.00, 120, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Mechas/Luzes', 'Mechas com técnica personalizada', 180.00, 180, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Hidratação', 'Hidratação profunda para cabelos danificados', 70.00, 45, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Escova', 'Escova modeladora com produtos de qualidade', 60.00, 40, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Penteado', 'Penteado para eventos especiais', 100.00, 60, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Manicure', 'Tratamento de unhas das mãos com esmaltação', 40.00, 30, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Pedicure', 'Tratamento de unhas dos pés com esmaltação', 50.00, 40, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Manicure e Pedicure', 'Combo de tratamento para unhas das mãos e pés', 80.00, 60, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Design de Sobrancelhas', 'Modelagem e limpeza de sobrancelhas', 30.00, 20, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Depilação Facial', 'Depilação de áreas do rosto', 40.00, 30, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Depilação Pernas Completa', 'Depilação de pernas inteiras', 80.00, 60, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Depilação Axilas', 'Depilação das axilas', 30.00, 20, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Massagem Relaxante', 'Massagem para aliviar tensões', 120.00, 60, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Limpeza de Pele', 'Limpeza facial profunda', 90.00, 60, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Tratamento Anti-idade', 'Tratamento facial rejuvenescedor', 150.00, 90, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Drenagem Linfática', 'Massagem para eliminar toxinas e reduzir inchaço', 110.00, 60, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
