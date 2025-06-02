-- Inserir serviços iniciais para teste
INSERT INTO services (name, description, duration_minutes, price, is_active, created_at, updated_at)
VALUES 
    ('Corte de Cabelo Feminino', 'Corte de cabelo para mulheres, inclui lavagem e finalização', 60, 85.00, true, NOW(), NOW()),
    ('Corte de Cabelo Masculino', 'Corte de cabelo para homens, inclui lavagem', 30, 50.00, true, NOW(), NOW()),
    ('Coloração', 'Aplicação de tinta, inclui lavagem e finalização', 120, 150.00, true, NOW(), NOW()),
    ('Hidratação Profunda', 'Tratamento de hidratação para cabelos danificados', 45, 75.00, true, NOW(), NOW()),
    ('Manicure', 'Cuidados com as unhas das mãos, inclui esmalte', 30, 40.00, true, NOW(), NOW()),
    ('Pedicure', 'Cuidados com as unhas dos pés, inclui esmalte', 45, 50.00, true, NOW(), NOW()),
    ('Combo Manicure e Pedicure', 'Serviço completo para mãos e pés', 75, 80.00, true, NOW(), NOW()),
    ('Escova', 'Lavagem e escova para todos os tipos de cabelo', 45, 65.00, true, NOW(), NOW()),
    ('Maquiagem', 'Maquiagem social ou para eventos', 60, 120.00, true, NOW(), NOW()),
    ('Design de Sobrancelhas', 'Modelagem de sobrancelhas com pinça ou linha', 20, 35.00, true, NOW(), NOW());
