# Changelog

## Versão 1.1.0 (31/05/2025)

### Correções
- **API Gateway**
  - Corrigido problema com testes unitários do JWT que apresentavam WeakKeyException
  - Implementação adequada do RouteValidator para evitar erros de compilação com variáveis finais em lambdas
  - Corrigido NullPointerException nos testes do AuthenticationFilter com configuração correta dos mocks
  - Substituídos todos os `when()` por `lenient().when()` para evitar UnnecessaryStubbingException
  - Adicionada anotação @SuppressWarnings para resolver avisos de type safety no mock do Predicate
  - Removidos imports não utilizados em diversos arquivos
  - Adicionadas dependências explícitas do Mockito para resolver problemas com MockitoExtension

- **Auth Service**
  - Atualizado DaoAuthenticationProvider para não usar métodos deprecated
  - Corrigidos avisos no application.yml com o escape adequado de caracteres especiais
  - Removidos imports não utilizados nos testes

### Melhorias
- Adicionada documentação no código para explicar as decisões de implementação
- Melhorada a organização e legibilidade dos testes

### Testes
- Corrigidos e estabilizados todos os testes no api-gateway e auth-service
- Implementada configuração correta para o assert de status HTTP nos testes reativos
