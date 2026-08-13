# Proposal: Frontend authentication

## Change ID

`frontend-auth`

## Status

Aprovada para implementacao em 2026-08-13.

## Problema

Backend oferece cadastro, login por sessao server-side, logout e CSRF, mas frontend ainda nao expõe fluxos autenticados utilizaveis. Fundacao atual tambem possui retry automatico para qualquer `403`, sem distinguir rejeicao CSRF de autorizacao negada.

## Objetivo

Especificar telas e camada frontend para cadastro, login, logout, descoberta de sessao atual, protecao basica de rotas e estados autenticados, preservando sessao no backend/browser, CSRF em memoria e anti-enumeracao.

## Escopo pretendido

- rotas `/login`, `/register` e area autenticada estrutural minima;
- formulario de cadastro com somente e-mail e senha aceitos pelo backend;
- login, logout e mensagens coerentes com contratos reais;
- estado autenticado somente em memoria, derivado de endpoint backend aprovado;
- protecao de rotas baseada no estado reconstruido apos recarga;
- integracao CSRF sem tokens persistidos ou retry automatico generico para `403`;
- estados acessiveis de loading, erro e sessao expirada;
- testes com Vitest e React Testing Library.

## Fora do escopo

- organizacoes, clientes, memberships, convites, tickets, dashboard, graficos e dados mockados;
- selecao ou listagem de organizacao;
- autorizacao por papel no frontend;
- JWT, storage persistente de credenciais ou manipulacao manual de cookies;
- confirmacao de e-mail, MFA, recuperacao de senha, E2E Playwright e novas bibliotecas de formulario sem decisao aprovada;
- alteracoes backend, migrations, cookies, autenticacao, CSRF ou endpoints nesta change.

## Decisoes consolidadas

- `GET /api/v1/session` esta disponivel e retorna identidade minima da sessao server-side;
- correcao do retry automatico generico de `403` pertence a esta change;
- area autenticada inicial sera shell neutro em `/app`, sem dados de organizacao.

## Criterio para avancar

Proposal, design, delta spec e tarefas devem receber revisao e aprovacao humana explicita antes de qualquer codigo.
