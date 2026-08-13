# Proposal: Abertura de ticket

## Change ID

`open-ticket`

## Status

Aprovada para implementacao em 2026-08-13.

## Problema

Organizacoes ja possuem membros, clientes e isolamento, mas ainda nao conseguem registrar uma solicitacao de suporte. Equipes dependem de conversas e registros externos, sem historico tecnico inicial associado a organizacao.

## Objetivo

Permitir abertura de ticket interno na organizacao explicita, com dados minimos aprovados, autor obtido exclusivamente da sessao server-side, isolamento rigoroso e resposta anti-enumeracao.

## Escopo pretendido

- abrir ticket para organizacao explicita;
- persistir UUID nao sequencial, `organization_id`, criador, dados minimos aprovados e timestamps UTC;
- persistir titulo obrigatorio, descricao opcional, cliente obrigatorio, prioridade e estado inicial fixo;
- aceitar atribuicao inicial opcional somente para membro da mesma organizacao;
- associar cliente ativo, sempre escopado pela organizacao;
- aplicar matriz de autoridade fixa para `OWNER`, `ADMIN` e `TECHNICIAN`;
- exigir sessao server-side e CSRF valido para mutacao;
- impedir enumeracao entre organizacoes;
- incluir migration, testes unitarios, autorizacao e integracao PostgreSQL/Testcontainers, apos aprovacao.

## Fora do escopo

- consulta ou listagem de tickets;
- filtros, busca, paginacao ou dashboards;
- atribuicao ou reatribuicao posterior;
- alteracao de status;
- fechamento ou reabertura;
- mensagens, historico, anexos, notificacoes ou portal de cliente;
- categorias, inclusive campo textual livre ou taxonomia configuravel;
- alteracao posterior de prioridade ou atribuicao;
- integracoes externas.

## Riscos

- criar ticket sem fronteira organizacional, com cliente de outra organizacao ou com cliente inativo;
- aceitar atribuicao para conta sem membership na organizacao;
- permitir abertura por papel sem autoridade aprovada;
- aceitar identidade ou criador enviados pelo cliente;
- revelar existencia de organizacao ou cliente por UUID conhecido;
- associar cliente inativo sem regra explicita;
- coletar descricao ou dados pessoais alem do necessario sem finalidade definida;
- antecipar lifecycle, atribuicao ou consulta fora desta change.

## Criterio para avancar

As decisoes em `open-questions.md` foram consolidadas. Proposal, design, delta spec e tarefas devem receber revisao e aprovacao humana explicita antes de qualquer codigo.
