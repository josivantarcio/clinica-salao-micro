# Client Service

## Description
The Client Service is responsible for managing all client-related information in the ClinicaSalao system. This service handles the complete lifecycle of client data, including personal information, contact details, and client status management.

## Features
- Create, read, update, and delete client records
- Client activation and deactivation
- Search and filter clients
- Input validation and error handling
- Comprehensive API documentation with Swagger/OpenAPI
- Database migrations with Flyway

## Technologies
- Java 17
- Spring Boot 3.5.0
- Spring Data JPA
- MapStruct (for DTO-Entity mapping)
- Lombok (for reducing boilerplate code)
- PostgreSQL (database)
- Flyway (database migration)
- SpringDoc OpenAPI (API documentation)
- Maven (build tool)
- JUnit 5 (testing)
- Mockito (mocking for tests)

## API Documentation

Once the application is running, you can access the API documentation at:
- Swagger UI: http://localhost:8082/swagger-ui.html
- OpenAPI JSON: http://localhost:8082/v3/api-docs

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL 12 or higher

### Installation

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/clinica-salao-microservices.git
   cd clinica-salao-microservices/client-service
   ```

2. Configure the database in `src/main/resources/application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/clinicasalon_client
       username: your_username
       password: your_password
   ```

3. Build the application:
   ```
   mvn clean install
   ```

4. Run the application:
   ```
   mvn spring-boot:run
   ```

## API Endpoints

### Clients

- `GET /api/clients` - Get all clients (paginated)
- `GET /api/clients/{id}` - Get a client by ID
- `POST /api/clients` - Create a new client
- `PUT /api/clients/{id}` - Update a client
- `DELETE /api/clients/{id}` - Delete a client
- `PATCH /api/clients/{id}/activate` - Activate a client
- `PATCH /api/clients/{id}/deactivate` - Deactivate a client

## Database Schema

The database schema is managed by Flyway migrations located in `src/main/resources/db/migration/`.

## Testing

To run the tests:

```
mvn test
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Contact

Your Name - your.email@example.com

Project Link: [https://github.com/yourusername/clinica-salao-microservices](https://github.com/yourusername/clinica-salao-microservices)

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
