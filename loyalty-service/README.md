# Serviço de Fidelidade (loyalty-service)

## Descrição
O serviço de fidelidade gerencia o programa de pontos e recompensas para clientes da plataforma ClinicaSalao. Este serviço controla a acumulação de pontos, resgate de recompensas e campanhas promocionais.

## Funcionalidades
- Gerenciamento de pontos de fidelidade
- Criação e gerenciamento de recompensas
- Geração de cupons promocionais
- Rastreamento de pontos por cliente
- Campanhas e programas de fidelidade
- Histórico de resgates e acúmulo de pontos
- Níveis de fidelidade (básico, premium, VIP, etc.)

## Tecnologias
- Spring Boot 3.5.0
- Java 21
- PostgreSQL
- Spring Data JPA
- Maven
- Feign Client (para comunicação com outros serviços)

## Estrutura do Banco de Dados
- Tabela de pontos por cliente
- Tabela de recompensas disponíveis
- Tabela de resgates
- Tabela de campanhas e programas
- Tabela de cupons

## Endpoints Principais
- `GET /loyalty/points/{clientId}` - Obter pontos de um cliente
- `POST /loyalty/points/{clientId}` - Adicionar pontos a um cliente
- `GET /loyalty/rewards` - Listar recompensas disponíveis
- `POST /loyalty/rewards` - Criar nova recompensa
- `PUT /loyalty/rewards/{id}` - Atualizar recompensa
- `DELETE /loyalty/rewards/{id}` - Remover recompensa
- `POST /loyalty/redeem/{clientId}/{rewardId}` - Resgatar recompensa
- `GET /loyalty/history/{clientId}` - Histórico de pontos e resgates
- `POST /loyalty/coupons/generate` - Gerar novos cupons promocionais
- `GET /loyalty/coupons/{code}` - Validar cupom

## Como Executar Localmente
Instruções de execução serão adicionadas à medida que o serviço for implementado.

## Configuração
As configurações do serviço serão fornecidas através de variáveis de ambiente:
- Configuração de banco de dados
- URLs de serviços dependentes

## Dependências
Este serviço depende dos seguintes serviços:
- auth-service: para validação de tokens
- client-service: para informações de clientes
