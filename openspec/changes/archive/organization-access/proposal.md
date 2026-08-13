# Proposal: Acesso organizacional

## Change ID

`organization-access`

## Status

Aprovada para implementacao em 2026-08-08.

## Problema

Criacao de organizacao estabelece membership inicial `OWNER`, mas sistema ainda nao possui modelo de papeis alem de `OWNER`, prova reutilizavel de membership existente nem fronteira de autorizacao para recursos organizacionais futuros. Conhecer UUID de organizacao nao pode conceder acesso.

## Objetivo

Definir e implementar fundacao minima de membership e autorizacao organizacional para que toda capacidade futura prove acesso por sessao autenticada e membership existente da conta na organizacao alvo.

## Escopo pretendido

- evoluir membership inicial para papeis minimos explicitamente aprovados;
- definir matriz fixa de privilegios para `OWNER`, `ADMIN` e `TECHNICIAN`, sem RBAC generico;
- definir como endpoint recebe ou resolve `organizationId` e como backend o autoriza;
- criar port e regras de aplicacao para verificar membership da conta autenticada na organizacao;
- preservar isolamento: UUID conhecido nao concede acesso;
- definir invariante para organizacao nao ficar sem `OWNER`;
- criar testes de autorizacao e isolamento entre contas e organizacoes;
- definir contrato HTTP minimo necessario para demonstrar autorizacao organizacional, se aprovado.

## Fora do escopo

- convites, token de aceite, entrega de e-mail, expiracao ou revogacao de convite;
- ciclo de vida completo de membership, remocao, revogacao ou transferencia de ownership;
- clientes, tickets, categorias, configuracoes, anexos ou notificacoes;
- organizacao ativa persistida na sessao, salvo decisao humana explicita;
- RBAC configuravel, permissao por banco ou grupos arbitrarios;
- Row-Level Security no PostgreSQL;
- auditoria persistida completa.

## Riscos

- aceitar `organizationId` conhecido sem validar membership;
- manter contexto organizacional em estado de sessao e causar confusao entre abas ou requisicoes;
- atribuir privilegios excessivos a papeis iniciais;
- permitir acao futura que deixe organizacao sem `OWNER`;
- antecipar convites ou lifecycle de membership sem contrato de produto;
- retornar respostas que confirmem membership ou organizacao de terceiros.

## Criterio para avancar

As decisoes em `open-questions.md`, proposal e artefatos posteriores devem receber revisao e aprovacao humana antes da implementacao.
