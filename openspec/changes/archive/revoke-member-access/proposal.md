# Proposal: Revogacao de acesso de membro

## Change ID

`revoke-member-access`

## Status

Arquivada em 2026-08-09 apos revisao humana final.

## Problema

Organizacoes podem convidar membros, mas nao podem remover um acesso quando pessoa deixa equipe, muda de funcao ou acesso foi concedido indevidamente. Membership ativa sem mecanismo de revogacao viola menor privilegio e impede controle operacional basico.

## Objetivo

Permitir que ator autorizado revogue membership de outra conta em organizacao explicita, sem revelar recursos de outras organizacoes e sem violar invariante de ao menos um `OWNER` por organizacao.

## Escopo pretendido

- revogar membership existente por organizacao e membro;
- exigir sessao server-side persistida e CSRF valido;
- autorizar revogacao conforme matriz de papeis aprovada;
- impedir remocao que deixe organizacao sem `OWNER`;
- retornar respostas que nao confirmem organizacao ou membership sem autorizacao;
- registrar auditoria minima sem e-mail, token, cookie ou payload completo;
- testar autorizacao, concorrencia, isolamento organizacional e ultima membership `OWNER`.

## Fora do escopo

- transferencia de ownership;
- alteracao de papel de membership;
- revogacao global de sessoes;
- cancelamento ou revogacao de convites pendentes;
- exclusao de conta ou organizacao;
- clientes, tickets e demais recursos organizacionais;
- RBAC configuravel.

## Riscos

- membro sem privilegio remover pessoa de maior privilegio;
- ultimo `OWNER` ser removido e deixar organizacao sem administracao;
- identificador conhecido revelar membership ou organizacao de terceiros;
- requisicoes concorrentes violarem invariante de ownership;
- sessao existente manter acesso apos membership revogada;
- auditoria ou logs reterem dados pessoais desnecessarios.

## Criterio para avancar

As decisoes em `open-questions.md`, proposal, design, delta spec e tarefas devem receber revisao e aprovacao humana antes da implementacao.
