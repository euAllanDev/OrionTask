# Proposal: Identidade da sessao atual

## Change ID

`current-session-identity`

## Status

Aprovada para implementacao em 2026-08-13.

## Problema

Frontend nao pode reconstruir estado autenticado apos recarga sem consultar backend. Cookie de sessao e `HttpOnly` e nao pode ser usado pelo frontend como fonte de identidade. Nao existe endpoint autenticado que exponha identidade minima da sessao atual.

## Objetivo

Expor identidade minima de sessao server-side atual para frontend reconstruir estado derivado, sem expor token, cookie, dados de organizacao ou outros dados de conta.

## Escopo pretendido

- `GET /api/v1/session` autenticado por sessao server-side persistida;
- resposta `200 OK` contendo somente `accountId` e e-mail normalizado;
- `401 Unauthorized` generico para sessao ausente, expirada ou revogada;
- nenhum CSRF para leitura;
- testes unitarios e HTTP/PostgreSQL/Testcontainers, apos aprovacao.

## Fora do escopo

- login, logout, criacao de conta, cookies, CSRF ou lifecycle de sessao;
- organizacoes, memberships, papeis, permissoes ou organizacao ativa;
- alteracao de conta, perfil, e-mail ou senha;
- JWT, `HttpSession` como identidade, auditoria nova ou frontend;
- listagem, exportacao ou descoberta de contas.

## Riscos

- expor e-mail alem da finalidade de identidade de interface;
- aceitar `accountId` enviado pelo cliente em vez de sessao persistida;
- confirmar estado de sessao sem autenticar pelo token opaco existente;
- vazar token, cookie, derivacao ou metadados de sessao;
- transformar endpoint em fonte de organizacao ativa ou autorizacao por papel.

## Criterio para avancar

Decisoes em `open-questions.md` devem ser fechadas. Depois, proposal, design, delta spec e tarefas devem receber revisao e aprovacao humana explicita antes de qualquer codigo.
