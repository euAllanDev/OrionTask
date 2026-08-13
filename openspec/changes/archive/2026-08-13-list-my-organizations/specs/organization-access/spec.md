# Organization Access Specification Delta

## ADDED Requirements

### Requirement: Listagem autenticada das organizacoes da conta

`GET /api/v1/organizations` MUST exigir sessao server-side persistida valida e NAO DEVE exigir CSRF. Sistema DEVE obter `accountId` exclusivamente da sessao persistida; corpo, query, header, `JSESSIONID` ou identificador de conta enviado pelo cliente NAO DEVEM definir identidade.

Sistema DEVE retornar somente organizacoes com membership atual para `accountId`. Cada item DEVE conter somente `id`, `name`, `createdAt`, `updatedAt` e `role` da membership atual. `role` DEVE aceitar somente `OWNER`, `ADMIN` ou `TECHNICIAN`. Sistema NAO DEVE retornar membros, convites, e-mail, permissao derivada, organizacao ativa, token ou cookie.

#### Scenario: conta lista somente suas organizacoes

- DADO conta autenticada com memberships em duas organizacoes;
- E outra organizacao sem membership para essa conta;
- QUANDO requisitar `GET /api/v1/organizations`;
- ENTAO sistema DEVE retornar somente as duas organizacoes da conta;
- E cada item DEVE conter papel atual da membership correspondente.

### Requirement: Ordem e lista vazia deterministicas

Sistema MUST ordenar organizacoes por `updatedAt` decrescente e `id` decrescente. Endpoint NAO DEVE aceitar paginação, filtros, busca ou ordenacao configuravel nesta capacidade. Conta autenticada sem memberships DEVE receber `200 OK` com array vazio.

Corpo ou parametro query enviado ao endpoint DEVE retornar `400 Bad Request` sem alterar dados.

#### Scenario: conta sem organization retorna lista vazia

- DADO conta autenticada sem membership;
- QUANDO requisitar listagem sem corpo ou query;
- ENTAO sistema DEVE retornar `200 OK` com `[]`.

### Requirement: Listagem nao concede acesso persistente

Organizacao sem membership atual MUST nao aparecer na listagem. Sessao ausente, expirada ou revogada DEVE seguir resposta global de autenticacao generica. Resposta de lista pode ficar obsoleta apos revogacao concorrente e NAO DEVE conceder acesso a endpoint organizacional subsequente; cada endpoint com `organizationId` DEVE revalidar membership atual.

#### Scenario: membership revogada nao autoriza rota posterior

- DADO conta recebeu lista contendo organizacao autorizada;
- E membership for revogada depois da resposta;
- QUANDO conta requisitar recurso da organizacao com UUID previamente listado;
- ENTAO endpoint posterior DEVE negar acesso conforme seu contrato de autorizacao atual.

### Requirement: Evidencia verificavel

Testes unitarios MUST cobrir mapeamento de item, papel e ordenacao. Testes HTTP com PostgreSQL/Testcontainers DEVEM cobrir sessao valida, os tres papeis, lista vazia, isolamento por membership, ordem deterministica, ausencia de CSRF, `401` sem sessao e `400` para corpo ou query.

#### Scenario: suite de verificacao executada

- DADO Docker disponivel no ambiente de teste;
- QUANDO `backend/mvnw.cmd verify` for executado;
- ENTAO testes unitarios, integracao PostgreSQL, Flyway e regras arquiteturais DEVEM passar.
