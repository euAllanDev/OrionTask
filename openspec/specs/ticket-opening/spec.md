# Ticket Opening Specification

## Purpose

Definir abertura interna e isolada de ticket organizacional.

## Requirements

### Requirement: Ticket organizacional minimo

Sistema MUST persistir ticket com UUID nao sequencial, `organization_id`, `customer_id`, `creator_account_id`, `assignee_account_id` opcional, titulo, descricao opcional, prioridade, status `OPEN` e timestamps UTC. Ticket NAO DEVE possuir categoria nesta capacidade.

Titulo DEVE ser obrigatorio, removido espacos nas extremidades e ter entre 1 e 120 caracteres. Descricao, quando presente, DEVE ter no maximo 4000 caracteres. Sistema NAO DEVE coletar dados de contato do solicitante ou outros campos nao definidos.

#### Scenario: persiste dados minimos do ticket

- DADO abertura valida de ticket;
- QUANDO criacao for confirmada;
- ENTAO registro DEVE conter somente dados definidos e timestamps UTC.

### Requirement: Abertura autorizada de ticket

`POST /api/v1/organizations/{organizationId}/tickets` MUST exigir sessao server-side persistida e CSRF valido. `OWNER`, `ADMIN` e `TECHNICIAN` DEVEM poder abrir ticket. `creatorAccountId` DEVE ser obtido exclusivamente da sessao persistida; cliente NAO DEVE poder envia-lo ou substitui-lo.

Corpo DEVE aceitar exclusivamente `title`, `description`, `customerId`, `priority` e `assigneeAccountId`. `customerId` e `title` DEVEM ser obrigatorios. `priority` ausente DEVE persistir como `MEDIUM`; sistema DEVE aceitar somente `LOW`, `MEDIUM`, `HIGH` e `URGENT`. `status` DEVE ser definido somente pelo servidor como `OPEN`. Categoria, `organizationId`, `creatorAccountId`, `status`, campos adicionais, corpo ausente, UUID malformado e valores invalidos DEVEM retornar `400 Bad Request` sem persistir ticket.

Em sucesso, sistema DEVE retornar `201 Created`, `Location` para ticket e corpo contendo somente `id`, `customerId`, `creatorAccountId`, `assigneeAccountId`, `title`, `description`, `priority`, `status`, `createdAt` e `updatedAt`.

#### Scenario: rejeita identidade enviada pelo cliente

- DADO membro autenticado e cliente `ACTIVE` da organizacao;
- QUANDO enviar corpo com `creatorAccountId`;
- ENTAO sistema DEVE retornar `400 Bad Request` e nao persistir ticket.

### Requirement: Cliente e atribuicao escopados pela organizacao

Cliente MUST existir, pertencer a `organizationId` informada e ter status `ACTIVE`. Cliente `INACTIVE` NAO DEVE receber ticket novo. Quando `assigneeAccountId` estiver presente, conta DEVE possuir membership atual na mesma organizacao; qualquer papel fixo e elegivel. Cliente e assignee NAO DEVEM ser localizados ou validados somente por UUID.

#### Scenario: abre ticket sem assignee e prioridade explicita

- DADO membro `TECHNICIAN` autenticado na organizacao e cliente `ACTIVE` daquela organizacao;
- QUANDO enviar titulo valido, `customerId` valido e prioridade `HIGH` sem `assigneeAccountId`;
- ENTAO sistema DEVE criar ticket `OPEN` sem assignee e retornar `201 Created`.

#### Scenario: prioridade assume valor padrao

- DADO membro autorizado e cliente `ACTIVE` da organizacao;
- QUANDO enviar abertura valida sem `priority`;
- ENTAO sistema DEVE persistir prioridade `MEDIUM`.

#### Scenario: assignee pertence a organizacao

- DADO membro autorizado, cliente `ACTIVE` e conta com membership na mesma organizacao;
- QUANDO enviar `assigneeAccountId` daquela conta;
- ENTAO sistema DEVE criar ticket associado a essa conta.

### Requirement: Isolamento e nao enumeracao na abertura

Organizacao inexistente, membership ausente do criador, cliente inexistente, cliente de outra organizacao, cliente `INACTIVE` e assignee sem membership na organizacao MUST retornar `404 Not Found` com mesmo status, corpo e sem metadata que revele existencia, status ou vinculo. Tentativa negada NAO DEVE persistir ticket.

#### Scenario: cliente de outra organizacao nao atravessa fronteira

- DADO criador autenticado com membership na organizacao A e cliente `ACTIVE` da organizacao B;
- QUANDO usar UUID desse cliente ao abrir ticket na organizacao A;
- ENTAO sistema DEVE retornar `404 Not Found` generico;
- E NAO DEVE criar ticket em nenhuma organizacao.

#### Scenario: assignee sem membership nao atravessa fronteira

- DADO criador autenticado e cliente `ACTIVE` da organizacao A;
- E conta conhecida sem membership na organizacao A;
- QUANDO usar UUID dessa conta como `assigneeAccountId`;
- ENTAO sistema DEVE retornar `404 Not Found` generico;
- E NAO DEVE criar ticket.

### Requirement: Evidencia verificavel

Testes unitarios MUST cobrir validacao de dados, prioridade padrao, status inicial e autoridade dos tres papeis. Testes de integracao PostgreSQL/Testcontainers DEVEM cobrir migration, criacao atomica, CSRF, isolamento de cliente e assignee, resposta `404` indistinguivel e ausencia de persistencia nas tentativas negadas.

#### Scenario: suite de verificacao executada

- DADO Docker disponivel no ambiente de teste;
- QUANDO `backend/mvnw.cmd verify` for executado;
- ENTAO testes unitarios, integracao PostgreSQL, Flyway e regras arquiteturais DEVEM passar.
