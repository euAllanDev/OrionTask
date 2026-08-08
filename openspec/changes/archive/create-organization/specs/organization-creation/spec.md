# Organization Creation Specification

## Purpose

Definir criacao autenticada e atomica de organizacao minima com membership inicial `OWNER`.

## Requirements

### Requirement: Criacao autenticada de organizacao

O sistema DEVE permitir que conta autenticada por sessao server-side persistida crie organizacao. `HttpSession`, `JSESSIONID` e identificadores de conta enviados pelo cliente NAO DEVEM definir a conta criadora. A operacao mutavel DEVE exigir CSRF valido.

#### Scenario: conta autenticada cria organizacao

- DADO conta com sessao persistida ativa e token CSRF valido;
- QUANDO enviar `POST /api/v1/organizations` com nome valido;
- ENTAO sistema DEVE criar organizacao e membership `OWNER` para conta autenticada;
- E DEVE retornar `201 Created`.

#### Scenario: cliente tenta escolher criador

- DADO conta autenticada;
- QUANDO enviar campo `ownerAccountId`, `accountId` ou `createdByAccountId`;
- ENTAO sistema DEVE rejeitar contrato com `400 Bad Request`;
- E NAO DEVE persistir organizacao ou membership.

### Requirement: Dados minimos e validacao

Organizacao DEVE conter somente `name` como dado descritivo nesta capacidade. `name` DEVE ser texto obrigatorio, nao vazio apos remover espacos nas extremidades e ter de 1 a 120 caracteres. Nome NAO DEVE ser identificador tecnico nem unico globalmente. Sistema NAO DEVE coletar slug, identificador fiscal, endereco ou telefone.

`name` DEVE ser mantido enquanto organizacao existir e for necessario para identifica-la e opera-la no OrionTask. Eliminacao, anonimização e retencao apos encerramento NAO sao definidas por esta capacidade e DEVEM ser especificadas em change propria.

#### Scenario: nomes iguais em organizacoes distintas

- DADO duas contas autenticadas;
- QUANDO cada uma criar organizacao com mesmo nome valido;
- ENTAO ambas criacoes DEVEM ser aceitas;
- E organizacoes DEVEM ter UUIDs distintos.

#### Scenario: nome invalido

- DADO conta autenticada com CSRF valido;
- QUANDO enviar nome ausente, nulo, nao textual, vazio ou maior que 120 caracteres;
- ENTAO sistema DEVE retornar `400 Bad Request`;
- E NAO DEVE persistir dados.

### Requirement: Membership inicial e atomicidade

Cada organizacao criada DEVE receber membership inicial para conta criadora com papel fixo `OWNER`. Organizacao e membership DEVEM ser persistidas na mesma transacao. Sistema NAO DEVE persistir organizacao sem membership administrativa inicial nem membership para organizacao inexistente.

Persistencia DEVE impedir memberships duplicadas para mesma combinacao de conta e organizacao com constraint equivalente a `UNIQUE(account_id, organization_id)`. Conta DEVE poder criar multiplas organizacoes.

#### Scenario: falha na criacao da membership

- DADO requisicao valida de criacao;
- QUANDO persistencia da membership falhar;
- ENTAO transacao DEVE sofrer rollback;
- E organizacao NAO DEVE permanecer persistida.

#### Scenario: mesma conta cria multiplas organizacoes

- DADO conta autenticada com CSRF valido;
- QUANDO criar duas organizacoes validas;
- ENTAO cada organizacao DEVE conter membership `OWNER` para mesma conta;
- E operacao NAO DEVE impor unicidade somente por conta.

### Requirement: Identificadores e resposta HTTP

Organizacao DEVE usar UUID nao sequencial como identificador tecnico e timestamps UTC. Em sucesso, `POST /api/v1/organizations` DEVE retornar `201 Created`, header `Location` com UUID da organizacao e corpo contendo somente `id`, `name` e `createdAt` da organizacao criada.

#### Scenario: contrato HTTP estrito

- DADO conta autenticada com CSRF valido;
- QUANDO enviar corpo com campo adicional ou estrutura diferente de `{ "name": "..." }`;
- ENTAO sistema DEVE retornar `400 Bad Request`;
- E NAO DEVE persistir dados.

### Requirement: Evidencia operacional minima

Depois de commit bem-sucedido, sistema DEVE registrar evento operacional estruturado `organization.created` com `accountId` e `organizationId`. Evento NAO DEVE conter nome, e-mail, payload completo, cookie, token ou credencial. Evento NAO DEVE substituir trilha de auditoria persistida.

#### Scenario: criacao bem-sucedida produz evento seguro

- DADO criacao bem-sucedida;
- QUANDO transacao for confirmada;
- ENTAO sistema DEVE registrar evento com UUIDs de conta e organizacao;
- E evento NAO DEVE conter nome ou dados sensiveis.

#### Scenario: falha no registro operacional

- DADO organizacao e membership confirmadas com sucesso;
- QUANDO o registro do evento operacional falhar;
- ENTAO criacao confirmada NAO DEVE sofrer rollback;
- E falha operacional DEVE ser tratada sem expor dados sensiveis.

### Requirement: Isolamento futuro por organizacao

Organizacao e membership DEVEM estabelecer fronteira de tenant por `organization_id`. Esta capacidade NAO DEVE expor consulta, alteracao ou selecao de organizacao. Recursos organizacionais futuros DEVEM filtrar e autorizar acesso por organizacao, sem conceder acesso por identificador conhecido.
