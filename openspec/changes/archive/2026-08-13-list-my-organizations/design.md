# Design: Listagem das minhas organizacoes

## Decisao principal

Adicionar `GET /api/v1/organizations` como leitura autenticada da lista de organizacoes onde conta atual possui membership. Endpoint nao define organizacao ativa e nao concede acesso futuro: toda rota com `organizationId` continua autorizando por membership atual.

## API e contrato

| Operacao | Rota | Autenticacao | CSRF |
| --- | --- | --- | --- |
| Listar minhas organizacoes | `GET /api/v1/organizations` | sessao server-side persistida | nao exige |

Resposta `200 OK` e array JSON sem paginação. Cada item retorna somente:

```json
{
  "id": "uuid",
  "name": "Organizacao",
  "createdAt": "2026-01-01T00:00:00Z",
  "updatedAt": "2026-01-01T00:00:00Z",
  "role": "OWNER"
}
```

`role` e papel fixo da membership atual: `OWNER`, `ADMIN` ou `TECHNICIAN`. Lista vazia retorna `200 OK` com `[]`. Endpoint nao aceita corpo, query, `accountId` ou parametros de ordenacao. Entrada adicional ou invalida deve retornar `400 Bad Request` sem alterar dados.

Ordem fixa: `updatedAt DESC`, `id DESC`.

## Isolamento e concorrencia

Backend obtem `accountId` somente da sessao server-side persistida e lista por membership cujo `account_id` coincide. Organizacoes sem membership atual nunca aparecem. Sessao ausente, expirada ou revogada segue resposta global de autenticacao `401` generica.

Resultado representa instante da consulta. Membership pode ser revogada depois da resposta; lista nao conserva direito e nenhum endpoint subsequente pode confiar nela como autorizacao. Toda operacao com `organizationId` revalida membership atual. Nao ha organizacao ativa em sessao, cookie, banco ou estado backend.

## Arquitetura e persistencia

- dominio `organization` pode usar resultado de leitura que combina organizacao e papel de membership sem introduzir entidade nova;
- aplicacao define porta de entrada, resultado e porta de saida de listagem;
- adapter web injeta principal autenticado e nao acessa repository;
- adapter de persistencia consulta `organizations` e `organization_memberships` com `account_id`, ordenacao fixa e projection minima;
- nenhuma migration, cache, auditoria ou evento novo;
- dominio continua Java puro e controller nao acessa repository.

## Privacidade e fora do escopo

Nome tem finalidade de permitir pessoa identificar organizacao acessivel. Resposta nao inclui e-mail, membros, convite, conta, permissao derivada, token, cookie ou payload de sessao. Nome e resposta completa nao devem entrar em logs, metricas, traces ou erros.

Fora do escopo: criar, editar, excluir, selecionar ou ativar organizacao; paginação, filtros, busca, ordenacao configuravel; memberships, convites, frontend, clientes, tickets, autorizacao frontend e auditoria nova.
