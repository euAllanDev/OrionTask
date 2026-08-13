# Proposal: Listagem das minhas organizacoes

## Change ID

`list-my-organizations`

## Status

Aprovada para implementacao em 2026-08-13.

## Problema

Conta autenticada pode criar organizacao e consultar organizacao por UUID conhecido, mas nao consegue descobrir organizacoes onde possui membership. Frontend nao pode navegar para contexto organizacional explicito sem inventar UUID, estado ativo oculto ou dados mockados.

## Objetivo

Permitir que conta autenticada liste somente organizacoes onde possui membership atual, para frontend escolher URL explicita de organizacao sem transformar sessao em fonte de organizacao ativa.

## Escopo pretendido

- `GET /api/v1/organizations`;
- sessao server-side persistida obrigatoria;
- leitura sem CSRF;
- identidade obtida exclusivamente da sessao;
- retorno minimo de organizacoes com membership da conta;
- isolamento por membership atual;
- ordenacao deterministica aprovada;
- lista vazia para conta sem memberships;
- testes unitarios e PostgreSQL/Testcontainers, apos aprovacao.

## Fora do escopo

- organizacao ativa em sessao, cookie ou servidor;
- criar, editar, excluir ou selecionar organizacao;
- memberships, convites, papeis configuraveis e autorizacao frontend;
- paginação, busca, filtros, ordenacao configuravel, dashboard e frontend;
- clientes, tickets, auditoria nova, eventos, migration, JWT ou `HttpSession` como identidade.

## Riscos

- retornar organizacao sem membership atual;
- confiar em `accountId` enviado pelo cliente;
- expor membership, e-mail, dados de organizacao alem do necessario ou organizacoes de outra conta;
- criar estado oculto de organizacao ativa;
- usar ordem instavel e dificultar interface futura;
- antecipar paginação ou filtros sem necessidade real.

## Criterio para avancar

Decisoes em `open-questions.md` foram consolidadas. Proposal, design, delta spec e tarefas devem receber revisao e aprovacao humana explicita antes de qualquer codigo.
