# Organization Access Specification Delta

## ADDED Requirements

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
