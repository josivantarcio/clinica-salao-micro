# Serviço de Autenticação (auth-service)

## Descrição
O serviço de autenticação é responsável por gerenciar a autenticação e autorização de usuários na plataforma ClinicaSalao. Este serviço implementa JWT para geração e validação de tokens.

## Funcionalidades
- Autenticação de usuários (login/logout)
- Gerenciamento de usuários (CRUD)
- Gerenciamento de permissões e funções
- Emissão e validação de tokens JWT
- Integração com outros serviços para validação de identidade

## Tecnologias
- Spring Boot 3.5.0
- Spring Security
- JWT
- Java 21
- PostgreSQL (banco de dados dedicado para usuários)
- Maven

## Estrutura do Banco de Dados
- Tabela de usuários
- Tabela de perfis/funções
- Tabela de permissões

## Endpoints Principais
- `POST /auth/login` - Autenticação de usuário
- `POST /auth/refresh` - Renovar token
- `POST /auth/logout` - Encerrar sessão
- `GET /users` - Listar usuários
- `POST /users` - Criar usuário
- `GET /users/{id}` - Obter usuário por ID
- `PUT /users/{id}` - Atualizar usuário
- `DELETE /users/{id}` - Remover usuário

## Como Executar Localmente
Instruções de execução serão adicionadas à medida que o serviço for implementado.

## Configuração
As configurações do serviço serão fornecidas através de variáveis de ambiente:
- `JWT_SECRET` - Segredo para assinatura de tokens JWT
- `JWT_EXPIRATION` - Tempo de expiração do token em milissegundos

## Dependências
Este serviço é independente e não requer outros serviços para funcionar, mas é utilizado por todos os outros para validação de tokens.
