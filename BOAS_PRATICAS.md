# Guia de Boas Práticas - ClinicaSalao Microservices

## Introdução

Este guia contém as melhores práticas para o desenvolvimento e manutenção do projeto ClinicaSalao, baseado nas lições aprendidas durante a implementação e correção dos microsserviços existentes.

## Configuração de Projeto

### Versionamento

- **Consistência de versões**: Garantir que todos os serviços referenciem a mesma versão do POM pai (atualmente 1.0.0)
- **Atualização sincronizada**: Ao atualizar a versão de um componente, atualizar em todos os serviços simultaneamente

### Dependências

- **Spring Boot**: Usar a versão 3.5.0 em todos os serviços
- **Java**: Usar Java 21 para todos os microsserviços
- **Documentação**: Incluir SpringDoc OpenAPI (versão 2.4.0) em todos os serviços
- **Bibliotecas comuns**: Utilizar o mesmo conjunto de bibliotecas em todos os microsserviços:
  - Spring Cloud (com versão compatível com Spring Boot 3.5.0)
  - Resilience4j para tolerância a falhas
  - Micrometer para métricas
  - Caffeine para cache

## Padrões de Código

### Nomenclatura

- **Packages**: Utilizar `com.clinicsalon.[service-name].[component]`
- **Classes**: Seguir convenções Java para nomes de classes (PascalCase)
- **Métodos**: Seguir convenções Java para nomes de métodos (camelCase)
- **Propriedades YAML**: Usar kebab-case para nomes de propriedades customizadas

### Monitoramento

- **Anotações**: Usar `@MonitorPerformance` em métodos críticos do negócio
- **Thresholds**: Definir thresholds realistas baseados em testes de performance
- **Logs**: Utilizar níveis de log apropriados (INFO para eventos normais, WARN para potenciais problemas, ERROR para falhas)
- **Formatos YAML**: Seguir o formato correto para chaves com caracteres especiais:
  ```yaml
  logging:
    level:
      '[com.clinicsalon]': INFO
  ```
- **Métricas**: Padronizar nomes de métricas para facilitar a criação de dashboards

### Testes

- **Testes unitários**: Cobertura mínima de 80% para código de negócio
- **Testes de integração**: Implementar para fluxos críticos
- **Testes de performance**: Utilizar para APIs de alto volume
- **Mocks**: Garantir que os mocks correspondam à assinatura atual das interfaces

## Arquitetura

### Comunicação entre Serviços

- **Feign Clients**: Utilizar para chamadas entre serviços
- **Circuit Breakers**: Configurar em todas as chamadas externas
- **Fallbacks**: Implementar estratégias de fallback para todos os clientes

### Bancos de Dados

- **Migrações**: Usar Flyway para todas as migrações de banco de dados
- **Nomenclatura**: Padronizar nomes de banco seguindo `clinicsalon_[service]`
- **Transações**: Usar `@Transactional` em métodos que modificam dados

### Cache

- **Configuração**: Seguir o padrão estabelecido com Caffeine
- **Nomes de cache**: Padronizar nomes para facilitar monitoramento
- **Expiração**: Definir tempos de expiração apropriados para cada tipo de dado

## YAML e Configurações

### Padrões de Configuração

- **Perfis**: Sempre incluir perfis `default` e `monitoring`
- **Propriedades comuns**: Extrair para arquivos compartilhados
- **Valores sensíveis**: Nunca incluir diretamente em arquivos de configuração (usar variáveis de ambiente ou config server)

### Monitoramento Spring Boot 3

- Usar estrutura correta para métricas e monitoramento:
  ```yaml
  management:
    prometheus:
      metrics:
        export:
          enabled: true
    metrics:
      distribution:
        percentiles-histogram:
          '[http.server.requests]': true
        percentiles:
          '[http.server.requests]': 0.5, 0.9, 0.95, 0.99
  ```

## CI/CD

### Pipeline

- **Verificação de lint**: Incluir na etapa de build
- **Testes**: Executar testes unitários e de integração automaticamente
- **Análise estática**: Configurar SonarQube para todos os serviços
- **Dependências**: Verificar vulnerabilidades com OWASP Dependency Check

### Ambientes

- **Desenvolvimento**: Configurar para logs detalhados e monitoramento completo
- **Testes**: Configurar para simular ambiente de produção
- **Produção**: Otimizar para performance e segurança

## Lições Aprendidas

### Migração Spring Boot 3

- Usar a documentação oficial para verificar mudanças entre versões
- Atualizar propriedades de configuração conforme as diretrizes
- Verificar compatibilidade de dependências
- Prestar atenção especial às mudanças em anotações e auto-configurações

### Configurações YAML

- Usar colchetes simples para chaves com caracteres especiais: `'[nome.com.pontos]'`
- Usar formatação consistente em todos os arquivos
- Separar configurações por ambiente/perfil

### Monitoramento Eficaz

- Focar em métricas que realmente importam para o negócio
- Configurar alertas apenas para problemas significativos
- Manter dashboards atualizados com novas funcionalidades

## Referências

- [Spring Boot 3 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Resilience4j Documentation](https://resilience4j.readme.io/docs)
