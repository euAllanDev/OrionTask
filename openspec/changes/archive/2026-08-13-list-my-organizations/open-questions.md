# Open Questions: Listagem das minhas organizacoes

## Q1 - Campos retornados

Decisao: resposta retorna `id`, `name`, `createdAt`, `updatedAt` e `role` da membership atual. Nao inclui membros, e-mail, metricas, organizacao ativa ou permissao derivada.

## Q2 - Ordenacao fixa

Decisao: ordem fixa `updatedAt DESC`, `id DESC`, com desempate deterministico e sem parametro de ordenacao.

## Q3 - Conta sem memberships

Decisao: conta autenticada sem memberships retorna `200 OK` com lista vazia. Nao e erro nem implica organizacao ativa.

## Q4 - Paginacao

Decisao: sem paginacao, filtros, busca ou ordenacao configuravel. Paginacao entra em change propria quando houver necessidade mensuravel.

## Q5 - Membership e concorrencia

Decisao: resposta reflete leitura da consulta e pode ficar obsoleta imediatamente apos revogacao. Listagem nao concede acesso; endpoints organizacionais posteriores devem reautorizar por membership atual. Nao cria estado ativo ou locking adicional.
