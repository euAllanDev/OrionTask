# Ticket Reading Specification

## Purpose

Definir consulta individual e listagem paginada, autorizada e isolada de tickets organizacionais.

## Requirements

### Requirement: Consulta individual autorizada de ticket

`GET /api/v1/organizations/{organizationId}/tickets/{ticketId}` MUST exigir sessao server-side persistida e NAO DEVE exigir CSRF. `OWNER`, `ADMIN` e `TECHNICIAN` DEVEM consultar ticket somente da propria organizacao. Backend DEVE obter `accountId` exclusivamente da sessao persistida e verificar membership pela combinacao de `organizationId` e `accountId`.

Resposta `200 OK` DEVE conter somente `id`, `title`, `description`, `status`, `priority`, `customerId`, `creatorAccountId`, `assigneeAccountId`, `createdAt` e `updatedAt`. Cliente inativo associado a ticket existente NAO DEVE ocultar ticket de membro autorizado.

`organizationId` ou `ticketId` malformado DEVE retornar `400 Bad Request`. Organizacao inexistente, membership ausente, ticket inexistente e ticket de outra organizacao DEVEM retornar `404 Not Found` com mesmo corpo e sem metadata que revele existencia ou vinculo.

#### Scenario: ticket de outro tenant nao e confirmado

- DADO membro autenticado na organizacao A e ticket existente na organizacao B;
- QUANDO consultar ticket B pela rota da organizacao A;
- ENTAO sistema DEVE retornar `404 Not Found` generico;
- E corpo DEVE ser identico ao de ticket inexistente na organizacao A.

### Requirement: Listagem paginada autorizada de tickets

`GET /api/v1/organizations/{organizationId}/tickets` MUST exigir sessao server-side persistida e NAO DEVE exigir CSRF. `OWNER`, `ADMIN` e `TECHNICIAN` DEVEM listar somente tickets da propria organizacao. Organizacao inexistente ou membership ausente DEVE retornar `404 Not Found` generico.

Listagem aceita somente `page`, `size`, `status`, `priority`, `customerId` e `assigneeAccountId`, no maximo uma vez cada. `page` inicia em zero; `size` padrao e 50 e maximo e 100. `status` aceita somente `OPEN`; `priority` aceita somente `LOW`, `MEDIUM`, `HIGH` e `URGENT`; `customerId` e `assigneeAccountId` aceitam UUID exato. Parametro desconhecido, repetido, malformado ou fora do intervalo DEVE retornar `400 Bad Request`.

Resposta `200 OK` DEVE conter somente `items`, `page`, `size`, `totalElements` e `totalPages`. Cada item DEVE conter somente `id`, `title`, `status`, `priority`, `customerId`, `assigneeAccountId`, `createdAt` e `updatedAt`. Sistema DEVE ordenar por `createdAt` decrescente e `id` decrescente.

#### Scenario: pagina padrao ordenada

- DADO membro autorizado e tickets da organizacao com mesmo `createdAt`;
- QUANDO listar sem parametros de pagina ou filtro;
- ENTAO resposta DEVE usar `page` zero e `size` 50;
- E itens DEVEM estar ordenados por `createdAt DESC`, `id DESC`.

### Requirement: Filtros organizacionais sem enumeracao

Filtros `status`, `priority`, `customerId` e `assigneeAccountId` MUST ser combinados por intersecao e sempre aplicar `organization_id`. Filtro ausente NAO DEVE restringir resultado. Ticket associado a cliente inativo DEVE continuar elegivel para listagem, inclusive quando filtrado por `customerId`.

`customerId` ou `assigneeAccountId` inexistente ou pertencente a outra organizacao DEVE retornar `200 OK` com `items` vazio, `totalElements` zero e `totalPages` zero. Sistema NAO DEVE usar esse resultado para confirmar existencia, membership ou vinculo de UUID.

#### Scenario: filtro estrangeiro retorna vazio

- DADO membro autorizado na organizacao A e cliente ou conta existente somente na organizacao B;
- QUANDO listar tickets da organizacao A usando UUID desse recurso como filtro;
- ENTAO sistema DEVE retornar `200 OK` com lista vazia;
- E NAO DEVE retornar `404` ou revelar vinculacao do UUID.

### Requirement: Evidencia verificavel

Testes unitarios MUST cobrir validacao de pagina, filtros, ordem e mapeamento de respostas. Testes de integracao PostgreSQL/Testcontainers DEVEM cobrir leitura individual, paginacao, ordem deterministica, todos os papeis, filtros, cliente inativo, isolamento e respostas `404` indistinguiveis. Testes DEVEM confirmar que UUID estrangeiro ou inexistente usado como filtro retorna lista vazia.

#### Scenario: suite de verificacao executada

- DADO Docker disponivel no ambiente de teste;
- QUANDO `backend/mvnw.cmd verify` for executado;
- ENTAO testes unitarios, integracao PostgreSQL, Flyway e regras arquiteturais DEVEM passar.
