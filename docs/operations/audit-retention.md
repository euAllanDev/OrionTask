# Retencao de auditoria

Eventos em `audit_events` sao removidos fisicamente todos os dias as 00:00 UTC quando completam 12 meses de `occurred_at`. Nao existe exclusao, alteracao, soft delete ou reativacao individual pela aplicacao.

Backups que contenham `audit_events` devem ter retencao maxima de 90 dias. Apos restauracao e antes de liberar trafego normal, execute a rotina de expurgo para remover eventos ja vencidos.

Antes de exposicao publica, controlador e orientacao juridica devem validar formalmente finalidade, categorias, prazo, base aplicavel, exclusao e tratamento de backups. Este gate nao e substituido por revisao tecnica ou testes.
