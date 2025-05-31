-- Inserir permissões básicas
INSERT INTO permissions (id, name, description) VALUES 
(gen_random_uuid(), 'READ_USER', 'Permissão para ler informações de usuários'),
(gen_random_uuid(), 'WRITE_USER', 'Permissão para criar/editar usuários'),
(gen_random_uuid(), 'DELETE_USER', 'Permissão para remover usuários'),
(gen_random_uuid(), 'READ_ROLE', 'Permissão para ler papéis'),
(gen_random_uuid(), 'WRITE_ROLE', 'Permissão para criar/editar papéis'),
(gen_random_uuid(), 'DELETE_ROLE', 'Permissão para remover papéis'),
(gen_random_uuid(), 'READ_CLIENT', 'Permissão para ler clientes'),
(gen_random_uuid(), 'WRITE_CLIENT', 'Permissão para criar/editar clientes'),
(gen_random_uuid(), 'DELETE_CLIENT', 'Permissão para remover clientes'),
(gen_random_uuid(), 'READ_PROFESSIONAL', 'Permissão para ler profissionais'),
(gen_random_uuid(), 'WRITE_PROFESSIONAL', 'Permissão para criar/editar profissionais'),
(gen_random_uuid(), 'DELETE_PROFESSIONAL', 'Permissão para remover profissionais'),
(gen_random_uuid(), 'READ_APPOINTMENT', 'Permissão para ler agendamentos'),
(gen_random_uuid(), 'WRITE_APPOINTMENT', 'Permissão para criar/editar agendamentos'),
(gen_random_uuid(), 'DELETE_APPOINTMENT', 'Permissão para remover agendamentos'),
(gen_random_uuid(), 'READ_REPORT', 'Permissão para ler relatórios'),
(gen_random_uuid(), 'GENERATE_REPORT', 'Permissão para gerar relatórios'),
(gen_random_uuid(), 'READ_LOYALTY', 'Permissão para ler dados de fidelidade'),
(gen_random_uuid(), 'WRITE_LOYALTY', 'Permissão para gerenciar programa de fidelidade');

-- Inserir papéis (roles) básicos
INSERT INTO roles (id, name, description) VALUES 
(gen_random_uuid(), 'ROLE_ADMIN', 'Administrador com acesso completo ao sistema'),
(gen_random_uuid(), 'ROLE_USER', 'Usuário padrão com acesso limitado'),
(gen_random_uuid(), 'ROLE_PROFESSIONAL', 'Profissional com acesso a agendamentos e clientes'),
(gen_random_uuid(), 'ROLE_MANAGER', 'Gerente com acesso a relatórios e configurações');

-- Associar todas as permissões ao papel de ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN';

-- Associar permissões básicas ao papel de USER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_USER' 
AND p.name IN ('READ_USER', 'READ_CLIENT', 'READ_PROFESSIONAL', 'READ_APPOINTMENT', 'READ_LOYALTY');

-- Associar permissões relevantes ao papel de PROFESSIONAL
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_PROFESSIONAL' 
AND p.name IN ('READ_USER', 'READ_CLIENT', 'READ_PROFESSIONAL', 'READ_APPOINTMENT', 'WRITE_APPOINTMENT', 'READ_LOYALTY');

-- Associar permissões relevantes ao papel de MANAGER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_MANAGER' 
AND p.name IN ('READ_USER', 'WRITE_USER', 'READ_CLIENT', 'WRITE_CLIENT', 'READ_PROFESSIONAL', 'WRITE_PROFESSIONAL', 
               'READ_APPOINTMENT', 'WRITE_APPOINTMENT', 'READ_REPORT', 'GENERATE_REPORT', 'READ_LOYALTY', 'WRITE_LOYALTY');

-- Criar usuário administrador inicial (senha: admin123)
INSERT INTO users (id, username, password, email, first_name, last_name, is_active, created_at, updated_at) 
VALUES (
    gen_random_uuid(), 
    'admin', 
    '$2a$10$QORooJwcSDnWeByODf5xU.V8ivgRWvH8SsO/ykYLyVHxLzMRZ8cNG', -- senha: admin123 
    'admin@clinicsalon.com', 
    'Admin', 
    'System', 
    true, 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
);

-- Associar usuário admin ao papel de ADMIN
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';
