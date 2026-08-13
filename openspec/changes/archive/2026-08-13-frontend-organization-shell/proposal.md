# Proposal: Frontend organization shell

## Change ID

`frontend-organization-shell`

## Status

Aprovada para implementacao em 2026-08-13.

## Why

`/app` autenticado ainda e neutro. Backend agora fornece `GET /api/v1/organizations` com memberships atuais, mas frontend nao permite descobrir organizacoes acessiveis nem estabelecer contexto organizacional explicito na URL.

## What Changes

- carregar organizacoes da conta autenticada por `GET /api/v1/organizations`;
- apresentar estados acessiveis de loading, empty, error e ready em `/app`;
- navegar para `/organizations/[organizationId]` por link de organizacao;
- substituir shell neutro por shell autenticado responsivo com sidebar, header, organizacao atual destacada e logout funcional;
- usar `organizationId` da URL como unico contexto de rota;
- apresentar `404` de recurso organizacional como `Recurso indisponível.` sem inferir existencia ou acesso;
- adicionar testes de cliente API, estados, navegacao, contexto, erro e logout.

## Out of Scope

- criar, alterar, selecionar ou persistir organizacao ativa;
- clientes, membros, convites, tickets, dashboard, contadores, dados mockados ou autorizacao por papel no frontend;
- mudancas backend, migrations ou contratos HTTP;
- storage persistente, JWT ou manipulacao de cookies de sessao.

## Criteria To Proceed

Proposal, design, delta spec e tarefas exigem aprovacao humana explicita antes de codigo funcional.
