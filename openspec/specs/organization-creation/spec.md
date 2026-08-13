# Organization Creation Specification

## Purpose

Definir criacao autenticada e atomica de organizacao minima com membership inicial `OWNER`.

## Requirements

### Requirement: Criacao autenticada de organizacao

O sistema DEVE permitir que conta autenticada por sessao server-side persistida crie organizacao. `HttpSession`, `JSESSIONID` e identificadores de conta enviados pelo cliente NAO DEVEM definir a conta criadora. Operacao DEVE exigir CSRF valido.

`POST /api/v1/organizations` DEVE receber exclusivamente `{ "name": "..." }`. Campos adicionais, incluindo `ownerAccountId`, `accountId` e `createdByAccountId`, DEVEM retornar `400 Bad Request` sem persistir dados. Em sucesso, DEVE retornar `201 Created`, `Location` com UUID e corpo contendo somente `id`, `name` e `createdAt`.

### Requirement: Dados minimos e privacidade

Organizacao DEVE conter somente `name` como dado descritivo nesta capacidade. `name` DEVE ser texto obrigatorio, nao vazio apos remover espacos nas extremidades e ter de 1 a 120 caracteres. Nome NAO DEVE ser identificador tecnico nem unico globalmente. Sistema NAO DEVE coletar slug, identificador fiscal, endereco ou telefone.

`name` DEVE ser mantido enquanto organizacao existir e for necessario para identifica-la e opera-la no OrionTask. Eliminacao, anonimização e retencao apos encerramento NAO sao definidas por esta capacidade e DEVEM ser especificadas em change propria.

### Requirement: Membership inicial e atomicidade

Cada organizacao criada DEVE receber membership inicial para conta criadora com papel fixo `OWNER`. Organizacao e membership DEVEM ser persistidas na mesma transacao. Sistema NAO DEVE persistir organizacao sem membership administrativa inicial nem membership para organizacao inexistente.

Persistencia DEVE impedir memberships duplicadas para mesma combinacao de conta e organizacao com constraint equivalente a `UNIQUE(account_id, organization_id)`. Conta DEVE poder criar multiplas organizacoes.

### Requirement: Identificadores e isolamento

Organizacao DEVE usar UUID nao sequencial como identificador tecnico e timestamps UTC. Organizacao e membership DEVEM estabelecer fronteira de tenant por `organization_id`. Esta capacidade NAO DEVE expor consulta, alteracao ou selecao de organizacao. Recursos organizacionais futuros DEVEM filtrar e autorizar acesso por organizacao, sem conceder acesso por identificador conhecido.

### Requirement: Evidencia operacional minima

Depois de commit bem-sucedido, sistema DEVE registrar evento operacional estruturado `organization.created` com `accountId` e `organizationId`. Evento NAO DEVE conter nome, e-mail, payload completo, cookie, token ou credencial. Evento NAO DEVE substituir trilha de auditoria persistida.

Se registro operacional falhar apos organizacao e membership confirmadas, criacao NAO DEVE sofrer rollback e falha DEVE ser tratada sem expor dados sensiveis.
