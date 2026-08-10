# Organization Access Specification

## Purpose

Definir membership e autorizacao minima por organizacao para recursos futuros.

## Requirements

### Requirement: Papeis fixos de membership

Sistema DEVE aceitar somente `OWNER`, `ADMIN` e `TECHNICIAN`. Papel NAO DEVE ser definido ou confiado a partir do cliente. Sistema NAO DEVE criar RBAC generico ou permissoes configuraveis nesta capacidade.

Os tres papeis DEVEM poder ler organizacao autorizada. `OWNER` e `ADMIN` poderao gerenciar memberships em capacidade futura. Apenas `OWNER` podera transferir ownership em capacidade futura. Toda organizacao existente DEVE possuir ao menos uma membership `OWNER`; nenhuma operacao futura DEVE violar esta invariante.

Migration DEVE preservar memberships `OWNER` existentes, aceitar os tres papeis aprovados e rejeitar outros valores.

### Requirement: Contexto explicito e autorizacao por membership

Todo endpoint organizacional DEVE receber `organizationId` explicito no path. Sistema NAO DEVE manter organizacao ativa em sessao, cookie ou `HttpSession`. Backend DEVE obter `accountId` exclusivamente da sessao server-side persistida e verificar membership existente por ambos `organizationId` e `accountId`.

Sistema NAO DEVE carregar organizacao apenas por `organizationId` e usar leitura isolada como decisao de autorizacao. UUID conhecido NAO DEVE conceder acesso.

### Requirement: Leitura organizacional autorizada

`GET /api/v1/organizations/{organizationId}` DEVE retornar `200 OK` e somente `id`, `name`, `createdAt` e `updatedAt` para membership existente. UUID malformado DEVE retornar `400 Bad Request`.

UUID valido sem organizacao e organizacao existente sem membership DEVEM retornar `404 Not Found` com mesmo status, corpo, formato e sem metadata que revele existencia ou vinculo. Cliente sem sessao autenticada NAO DEVE receber dados e DEVE receber resposta de autenticacao global.

GET NAO DEVE exigir CSRF, nem atualizar organizacao, membership ou timestamp funcional. Atualizacao tecnica de atividade da sessao permanece responsabilidade da autenticacao. Operacoes mutaveis futuras DEVEM exigir CSRF valido.

### Requirement: Isolamento organizacional

Consultas de autorizacao DEVEM incluir `organization_id` e `account_id`. Controller NAO DEVE acessar repositorio diretamente. Erros, logs e respostas NAO DEVEM expor dados de organizacao nao autorizada.

### Requirement: Revogacao autorizada de membership

`DELETE /api/v1/organizations/{organizationId}/members/{accountId}` DEVE exigir sessao server-side persistida e CSRF valido. Sistema DEVE obter identidade do ator exclusivamente da sessao e NAO DEVE aceitar identidade do ator enviada pelo cliente.

Endpoint DEVE aceitar somente UUIDs validos no path e nenhum corpo. UUID malformado ou corpo enviado DEVE retornar `400 Bad Request` sem persistir dados. Em revogacao efetiva, DEVE retornar `204 No Content`.

`OWNER` DEVE poder revogar somente `ADMIN` e `TECHNICIAN`. `ADMIN` DEVE poder revogar somente `TECHNICIAN`. `TECHNICIAN` NAO DEVE revogar memberships. Nenhum papel DEVE revogar membership `OWNER` nesta capacidade. Sistema NAO DEVE implementar RBAC generico, transferencia de ownership ou mudanca de papel.

#### Scenario: owner revoga tecnico

- DADO `OWNER` autenticado e membership `TECHNICIAN` diferente na mesma organizacao;
- QUANDO requisitar revogacao valida;
- ENTAO sistema DEVE excluir membership alvo e retornar `204 No Content`;
- E acesso posterior do alvo a recursos daquela organizacao DEVE ser negado.

#### Scenario: administrador nao revoga administrador

- DADO `ADMIN` autenticado e alvo `ADMIN` na mesma organizacao;
- QUANDO requisitar revogacao valida;
- ENTAO sistema DEVE retornar `403 Forbidden`;
- E membership alvo DEVE permanecer existente.

### Requirement: Isolamento e nao enumeracao na revogacao

Sistema DEVE localizar e excluir membership somente pela combinacao de `organization_id` e `account_id`. Organizacao inexistente, ator sem membership e alvo sem membership DEVEM retornar `404 Not Found` com mesmo status, corpo e sem metadata que revele existencia, vinculo ou papel. Controller NAO DEVE acessar repository diretamente.

#### Scenario: ator tenta revogar membro de outra organizacao

- DADO ator autenticado sem membership na organizacao indicada;
- QUANDO usar UUID conhecido de organizacao e conta;
- ENTAO sistema DEVE retornar `404 Not Found` generico;
- E NAO DEVE remover membership de nenhuma organizacao.

### Requirement: Escopo de sessao e integridade operacional

Revogar membership DEVE excluir somente membership alvo. Sistema NAO DEVE revogar ou alterar sessoes da conta alvo, memberships de outras organizacoes, convites ou contas. Operacoes organizacionais subsequentes DEVEM autorizar usando membership atual.

Depois de commit de revogacao efetiva, sistema DEVE registrar evento operacional estruturado `organization.membership_revoked` com somente `organizationId`, `revokedAccountId` e `revokedByAccountId`. Evento NAO DEVE conter e-mail, papel, corpo, cookie, token ou credencial. Falha de registro apos commit NAO DEVE causar rollback.

#### Scenario: revogacao preserva outras organizacoes

- DADO conta alvo com memberships em duas organizacoes;
- QUANDO membership for revogada em uma delas;
- ENTAO somente aquela membership DEVE ser excluida;
- E sessoes e membership da outra organizacao DEVEM permanecer inalteradas.

### Requirement: Evidencia automatizada de isolamento organizacional

Suite de integracao com PostgreSQL DEVE demonstrar que conta autenticada sem membership na organizacao alvo nao consegue ler organizacao, listar clientes, ler cliente, alterar cliente, desativar cliente ou revogar membership daquela organizacao, mesmo conhecendo UUIDs validos. Onde especificacao define `404 Not Found` indistinguivel, teste DEVE comparar status e corpo com recurso inexistente equivalente.

Testes de mutacao negada DEVEM usar CSRF valido da conta nao autorizada e confirmar que clientes e memberships de ambas organizacoes permanecem inalterados apos a resposta. Suite DEVE cobrir duas organizacoes distintas e ao menos uma conta autenticada por tenant.
