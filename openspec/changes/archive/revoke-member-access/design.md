# Design: Revogacao de acesso de membro

## Decisao principal

`DELETE /api/v1/organizations/{organizationId}/members/{accountId}` remove fisicamente uma membership existente. `accountId` do alvo identifica recurso a revogar, mas `accountId` do ator vem exclusivamente da sessao server-side persistida. Nenhuma sessao e revogada: toda rota organizacional continua verificando membership atual por `organizationId` e `accountId`.

## Autorizacao e invariantes

| Ator | Pode revogar |
| --- | --- |
| `OWNER` | `ADMIN`, `TECHNICIAN` |
| `ADMIN` | `TECHNICIAN` |
| `TECHNICIAN` | Nenhum papel |

`OWNER` nunca e alvo nesta change. Portanto, toda organizacao preserva ao menos uma membership `OWNER` sem introduzir transferencia de ownership ou remocao concorrente de owners.

Aplicacao localiza membership do ator pela dupla `organizationId` e `actorAccountId`; ausencia retorna `404` generico. Em seguida, localiza membership alvo dentro da mesma organizacao. Alvo sem membership, organizacao inexistente ou ator sem membership retornam `404` sem diferenciar causa. Papel do ator sem permissao retorna `403` somente depois de acesso organizacional autorizado. UUID malformado retorna `400` sem persistencia.

## HTTP

Endpoint exige sessao autenticada e CSRF valido. Corpo e campos adicionais nao sao aceitos. Sucesso retorna `204 No Content`. Controller obtem ator com `@AuthenticationPrincipal`, chama porta de entrada e nunca acessa repositorio.

Tentativa posterior da conta removida em qualquer endpoint organizacional ja existente recebe `404` generico, pois consulta de autorizacao inclui membership atual. Login e acesso a outras organizacoes nao sao afetados.

## Arquitetura e persistencia

- aplicacao cria caso de uso e porta para revogar membership;
- dominio mantem papeis fixos e regra de matriz, sem Spring ou JPA;
- adapter JPA executa busca e exclusao por `organization_id` e `account_id` na mesma transacao;
- adaptador bloqueia organizacao durante revogacao para serializar mudancas de membership desta capacidade;
- constraint `UNIQUE(account_id, organization_id)` continua impedindo memberships duplicadas;
- nenhuma migration e necessaria: tabela atual suporta exclusao fisica.

## Privacidade e evidencia operacional

Depois do commit, registrar evento estruturado `organization.membership_revoked` somente com `organizationId`, `revokedAccountId` e `revokedByAccountId`. Evento nao inclui e-mail, papel, payload, cookie, token ou dados de outra organizacao. Falha de log apos commit nao causa rollback. Audit trail persistida, retencao e exclusao de conta ficam fora desta change.

## Fora do escopo

- remocao de `OWNER` e transferencia de ownership;
- mudanca de papel;
- revogacao de sessoes;
- cancelamento de convites;
- exclusao de conta ou organizacao;
- RBAC configuravel e audit trail persistida.
