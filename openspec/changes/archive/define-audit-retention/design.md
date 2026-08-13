# Design: Retencao de auditoria

## Politica canonica

Eventos de auditoria organizacional serao retidos por 12 meses a partir de `occurredAt` em UTC e excluidos fisicamente ao fim do prazo. Evento futuro contera somente `organizationId`, `actorAccountId`, identificador tecnico do recurso afetado quando aplicavel, tipo de evento e timestamp UTC. Esses identificadores sao pseudonimizados e vinculaveis a pessoa; nao sao anonimos.

Finalidade: seguranca, responsabilidade operacional e investigacao de incidentes da organizacao controladora no OrionTask. A policy nao se aplica a contas, sessoes, clientes, tickets, mensagens, anexos ou logs operacionais fora da categoria de auditoria.

## Expurgo e backups

Quando houver persistencia de auditoria, rotina de expurgo devera excluir fisicamente eventos cujo `occurredAt` tenha completado 12 meses. Nao havera soft delete, reativacao ou anonimização nesta policy. Excecao de conservacao exige fundamento legal ou regulatorio especifico documentado em change propria; nao existe mecanismo generico de excecao.

Backups terao retencao maxima de 90 dias. Restauracao nao reinicia prazo de evento: antes de exposicao normal do sistema restaurado, rotina de expurgo devera executar e remover eventos vencidos. A janela residual maxima em backup e de 90 dias.

## Exclusao de organizacao

Quando capacidade futura excluir organizacao, eventos de auditoria ja existentes mantem prazo calculado a partir de `occurredAt`. Eles nao serao excluidos imediatamente nem retidos por novo prazo. Ao completar 12 meses, serao excluidos fisicamente, salvo fundamento especifico de conservacao.

## Gate de exposicao publica

Antes de exposicao publica do OrionTask, controlador e orientacao juridica devem validar formalmente finalidade, categorias registradas, prazo, base aplicavel, exclusao e tratamento de backups. Esta validacao e requisito de release, nao pode ser substituida por aprovacao tecnica ou de codigo.

## Limites

Esta change define policy e requisitos para capacidades futuras. Nao cria tabela, endpoint, job, migration, auditoria persistida ou rotina de backup. `audit-critical-actions` devera implementar seus mecanismos em conformidade com esta policy apos aprovacao propria.
