# Proposal: Criacao de organizacao

## Change ID

`create-organization`

## Status

Aprovada para implementacao em 2026-08-08.

## Problema

Uma conta autenticada ainda nao possui um limite organizacional para operar o OrionTask. Sem uma organizacao, recursos futuros como clientes e tickets nao podem receber `organization_id`, nem aplicar isolamento multiempresa e autorizacao por recurso.

## Objetivo

Permitir que uma conta autenticada crie uma organizacao minima e estabelecer sua relacao inicial de propriedade, sem antecipar o sistema completo de memberships, papeis ou convites.

## Escopo pretendido

- exigir uma sessao de autenticacao valida para criar organizacao;
- coletar somente `name` como dado organizacional descritivo obrigatorio;
- persistir uma organizacao com UUID nao sequencial e timestamps UTC, sem slug ou identificador publico adicional;
- estabelecer membership inicial da conta criadora com papel fixo `OWNER`;
- permitir que uma conta crie multiplas organizacoes, impedindo vinculo duplicado para mesma conta e organizacao;
- criar organizacao e membership `OWNER` atomicamente;
- definir finalidade e classificacao inicial dos dados organizacionais coletados;
- impedir que identificadores enviados pelo cliente definam conta criadora ou autoridade;
- definir o contrato HTTP, erros seguros, auditoria e protecao contra abuso aplicaveis;
- incluir testes de dominio, aplicacao, integracao PostgreSQL, autoridade do criador e isolamento da criacao entre contas.

## Fora do escopo

- membership completo e gerenciamento de membros;
- papeis alem da autoridade inicial do criador;
- convites, aceitacao, remocao ou revogacao de membros;
- edicao, desativacao ou exclusao de organizacao;
- cadastro de clientes, tickets, anexos ou notificacoes;
- selecao de organizacao ativa na sessao;
- faturamento, planos e limites comerciais;
- portal do cliente;
- Row-Level Security no PostgreSQL.

## Riscos

- criar vinculo entre conta e organizacao que limite ou contradiga membership futuro;
- permitir que uma conta aja em organizacao alheia por identificador conhecido;
- reter nome apos termino de sua finalidade sem politica aprovada;
- expor existencia ou dados de organizacoes por respostas, logs ou auditoria;
- implementar autorizacao apenas no frontend ou fora do contexto autenticado;
- introduzir modelo de papeis prematuro nesta change.

## Criterio para implementar

Proposal, design, delta spec e tasks devem receber revisao e aprovacao humana explicita antes da implementacao.
