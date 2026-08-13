# Session Authentication Specification Delta

## ADDED Requirements

### Requirement: Identidade minima da sessao atual

`GET /api/v1/session` MUST exigir sessao server-side persistida valida e NAO DEVE exigir CSRF. Sistema DEVE obter `accountId` exclusivamente do principal estabelecido pela sessao persistida; corpo, query, header, `JSESSIONID` ou identificador enviado pelo cliente NAO DEVEM definir a identidade.

Quando autenticado, sistema DEVE retornar `200 OK` com somente `accountId` UUID e `email` normalizado persistido da conta. Resposta NAO DEVE conter token, cookie, derivacao, timestamps de sessao, organizacao, membership ou papel.

#### Scenario: sessao valida retorna identidade minima

- DADO sessao server-side valida associada a uma conta;
- QUANDO requisitar `GET /api/v1/session`;
- ENTAO sistema DEVE retornar `200 OK` com `accountId` da sessao e e-mail normalizado da conta;
- E NAO DEVE retornar dados de sessao ou organizacao.

### Requirement: Ausencia de sessao atual nao e confirmada

Sessao ausente, expirada, revogada ou associada a conta nao localizada MUST retornar `401 Unauthorized` generico. Sistema NAO DEVE diferenciar causa, confirmar estado previo de cookie ou retornar identificador parcial.

#### Scenario: sessao expirada nao retorna identidade

- DADO cookie associado a sessao expirada;
- QUANDO requisitar `GET /api/v1/session`;
- ENTAO sistema DEVE retornar `401 Unauthorized` generico;
- E NAO DEVE retornar `accountId` ou e-mail.

### Requirement: Privacidade da identidade atual

E-mail retornado por `GET /api/v1/session` MUST ter finalidade exclusiva de identificar conta autenticada na interface. Sistema NAO DEVE registrar e-mail, payload, cookie, token ou derivacao em logs, auditoria, metricas, traces ou respostas de erro nesta capacidade. Leitura NAO DEVE criar evento de auditoria.

#### Scenario: leitura nao cria auditoria

- DADO sessao valida;
- QUANDO consultar identidade atual;
- ENTAO sistema DEVE retornar identidade minima;
- E NAO DEVE persistir evento de auditoria para leitura.

### Requirement: Evidencia verificavel

Testes unitarios MUST cobrir obtencao de identidade somente pelo principal autenticado e conta ausente. Testes HTTP com PostgreSQL/Testcontainers DEVEM cobrir sessao valida, sessao ausente, expirada ou revogada, ausencia de CSRF e campos exatos de resposta.

#### Scenario: suite de verificacao executada

- DADO Docker disponivel no ambiente de teste;
- QUANDO `backend/mvnw.cmd verify` for executado;
- ENTAO testes unitarios, integracao PostgreSQL, Flyway e regras arquiteturais DEVEM passar.
