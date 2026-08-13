# Proposal: Definir retencao de auditoria

## Change ID

`define-audit-retention`

## Status

Arquivada apos revisao humana. Validacao formal de controlador e orientacao juridica permanece gate obrigatorio antes de exposicao publica.

## Problema

Auditoria de acoes criticas requer registros persistidos, mas OrionTask nao possui politica de retencao e exclusao aplicavel a essa categoria. Reter indefinidamente conflita com minimizacao e impede definir ciclo de vida verificavel.

## Objetivo

Definir finalidade, dados minimos, prazo, gatilho de exclusao, tratamento de backups e responsabilidades para futuros eventos de auditoria organizacional. A policy resultante desbloqueia especificacao e implementacao de `audit-critical-actions`.

## Escopo candidato

- eventos futuros de auditoria das mutacoes organizacionais aprovadas;
- identificadores tecnicos de organizacao, conta autora e recurso afetado, tipo de evento e timestamp UTC;
- retencao no banco primario e eliminacao ou anonimização ao fim do prazo;
- comportamento quando organizacao for excluida no futuro;
- prazo de expurgo em backups e restauracao;
- finalidade de seguranca, responsabilidade operacional e investigacao de incidentes.

## Fora do escopo candidato

- politica geral de contas, sessoes, clientes, tickets, mensagens, anexos e notificacoes;
- direitos de titulares, exportacao, correcao, exclusao de organizacao ou Central de Confianca;
- definicao juridica definitiva de controlador, operador, suboperadores ou transferencias internacionais;
- implementacao de auditoria, endpoint de consulta, migration ou job de expurgo.

## Riscos

- prazo sem base de negocio ou juridica adequada;
- exclusao impedir investigacao de incidente ou demonstracao de responsabilidade;
- backups restaurarem eventos vencidos sem novo expurgo;
- ator ser tratado como dado anonimo apesar de ser vinculavel a conta;
- policy de auditoria ser indevidamente aplicada a outras categorias de dados.

## Criterio para avancar

Antes de exposicao publica do OrionTask, controlador e orientacao juridica devem validar formalmente finalidade, categorias, prazo, base aplicavel, exclusao e tratamento de backups. Depois de aprovar design, delta spec e tarefas, esta change podera definir a politica tecnica; qualquer implementacao de auditoria devera preservar esse gate de exposicao.
