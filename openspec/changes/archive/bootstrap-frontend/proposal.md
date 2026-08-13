# Proposal: Bootstrap frontend

## Change ID

`bootstrap-frontend`

## Status

Arquivada apos verificacao local e revisao humana final.

## Problema

`frontend/` contem somente `.gitkeep`. Backend possui capacidades autenticadas, mas nao ha aplicacao web, padrao visual, camada HTTP ou integracao segura de cookie e CSRF para torna-las utilizaveis.

## Objetivo

Estabelecer fundacao tecnica, arquitetural e visual minima em Next.js para futuras features autenticadas, sem implementar fluxos completos de negocio.

## Escopo pretendido

- inicializar Next.js, TypeScript strict, Tailwind CSS e shadcn/ui em `frontend/`;
- adicionar Motion como biblioteca padrao para animacoes React orientadas a estado;
- definir estrutura por feature, camada HTTP e tipos de contratos;
- usar proxy/rewrite de desenvolvimento para requisicoes `/api` ao backend;
- implementar cliente CSRF em memoria, separado de autenticacao;
- criar layout publico e shell autenticado estrutural por `organizationId` na URL;
- definir tokens visuais claros, sidebar, header e estados loading, empty e error;
- configurar `npm run check` com typecheck, lint, formatacao e testes acordados;
- documentar execucao local e requisitos de proxy/cookies;
- preparar, sem implementar, auth, organizacoes, membros e clientes.

## Fora do escopo

- login, cadastro, logout ou qualquer autenticacao paralela;
- tela funcional de organizacoes, membros ou clientes;
- tickets, dados falsos, dashboards ou graficos;
- JWT, leitura/manipulacao de cookie de sessao e armazenamento de credenciais;
- Redux, BFF separado, microfrontends e design system complexo;
- GSAP, Three.js ou colecoes React Bits sem necessidade concreta aprovada;
- alteracoes no backend, CORS, cookies, CSRF, migrations ou CI.

## Riscos

- proxy local mascarar comportamento inseguro de cookies em producao;
- CSRF ser tratado como credencial ou persistido indevidamente;
- shell pressupor sessao sem endpoint de identidade atual;
- URL organizacional ser substituida por estado global oculto;
- dependencias de UI e qualidade aumentarem superficie sem beneficio concreto.

## Criterio para avancar

Decisoes em `open-questions.md`, proposal, design, delta spec e tarefas devem receber revisao e aprovacao humana antes da implementacao.
