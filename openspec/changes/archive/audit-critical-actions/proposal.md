# Proposal: Auditoria de acoes criticas

## Change ID

`audit-critical-actions`

## Status

Arquivada apos verificacao local e revisao humana final. Validacao formal de controlador e orientacao juridica permanece obrigatoria antes de exposicao publica.

## Problema

Capacidades atuais emitem logs operacionais apos acoes relevantes, mas logs nao constituem trilha de auditoria persistida: podem expirar, nao possuem consulta autorizada, nao definem retencao e nao permitem demonstrar historico operacional por organizacao.

## Objetivo

Definir e implementar trilha de auditoria persistida, minimizada e isolada por organizacao para acoes criticas aprovadas, separada de logs operacionais.

## Escopo candidato

- registrar eventos aprovados apos transacao efetiva;
- persistir identificadores tecnicos minimos, tipo de acao e timestamp UTC;
- garantir isolamento por `organization_id` quando evento pertencer a organizacao;
- proteger contra alteracao e exclusao pela aplicacao;
- permitir consulta somente quando finalidade, autoridade e contrato forem aprovados;
- manter logs operacionais atuais sem dados sensiveis desnecessarios.

## Fora do escopo candidato

- audit trail de tickets, mensagens, anexos, notificacoes ou portal de cliente;
- captura de payload HTTP, e-mail, nome de cliente, senha, hash, cookie, token ou CSRF;
- SIEM, exportacao externa, streaming, webhooks, event sourcing ou outbox;
- politica completa de retencao, exclusao e exportacao LGPD sem decisao especifica.

## Riscos

- persistir identificadores ou metadados alem da finalidade de seguranca;
- permitir que membro de uma organizacao consulte eventos de outra;
- registrar evento antes de commit e criar evidencia de acao nao efetivada;
- usar logs como fonte de auditoria ou misturar ambos os objetivos;
- definir retencao arbitraria e conflitar com politica futura de privacidade.

## Criterio para avancar

Design, delta spec e tarefas devem receber revisao e aprovacao humana antes da implementacao. Validacao formal de controlador e orientacao juridica permanece obrigatoria antes de exposicao publica.
