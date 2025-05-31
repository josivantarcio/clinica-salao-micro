# Serviço de Clientes (client-service)

## Descrição
O serviço de clientes gerencia todas as informações relacionadas aos clientes da plataforma ClinicaSalao. Este serviço mantém um registro completo dos dados pessoais, histórico e preferências dos clientes.

## Funcionalidades
- Cadastro, consulta, atualização e remoção de clientes
- Gerenciamento de perfis de cliente
- Histórico de atendimentos
- Preferências de tratamentos e profissionais
- Acompanhamento de progresso de tratamentos
- Gestão de contatos e informações pessoais

## Tecnologias
- Spring Boot 3.5.0
- Java 21
- PostgreSQL
- Spring Data JPA
- Maven
- Feign Client (para comunicação com outros serviços)

## Estrutura do Banco de Dados
- Tabela de clientes
- Tabela de contatos
- Tabela de preferências
- Tabela de histórico médico/estético

## Endpoints Principais
- `GET /clients` - Listar clientes
- `POST /clients` - Cadastrar cliente
- `GET /clients/{id}` - Obter cliente por ID
- `PUT /clients/{id}` - Atualizar cliente
- `DELETE /clients/{id}` - Remover cliente
- `GET /clients/search` - Buscar clientes por critérios
- `GET /clients/{id}/history` - Obter histórico de atendimentos
- `GET /clients/{id}/preferences` - Obter preferências do cliente

## Como Executar Localmente
Instruções de execução serão adicionadas à medida que o serviço for implementado.

## Configuração
As configurações do serviço serão fornecidas através de variáveis de ambiente:
- Configuração de banco de dados
- URLs de serviços dependentes

## Dependências
Este serviço depende dos seguintes serviços:
- auth-service: para validação de tokens
