# Tasks: Auditoria de acoes criticas

## 1. Revisao e aprovacao

- [x] Decidir eventos cobertos, consulta por papel, tentativas negadas e imutabilidade
- [x] Consolidar dependencia de retencao e exclusao
- [x] Revisar e aprovar proposal, design, delta spec e tarefas

## 2. Modelo e persistencia

- [x] Criar modulo `audit` com dominio Java puro, portas e adaptador PostgreSQL
- [x] Criar migration imutavel de `audit_events` e indice organizacional de consulta
- [x] Garantir escrita atomica com cada mutacao organizacional coberta
- [x] Implementar expurgo diario conforme policy de 12 meses

## 3. Consulta e operacao

- [x] Criar consulta paginada para `OWNER` e `ADMIN`
- [x] Aplicar isolamento por organizacao, anti-enumeracao e menor privilegio
- [x] Documentar configuracao de backups de 90 dias e expurgo apos restauracao
- [x] Registrar gate de validacao formal antes de exposicao publica

## 4. Qualidade e archive

- [x] Cobrir atomicidade, isolamento, autorizacao, anti-enumeracao e expurgo em PostgreSQL/Testcontainers
- [x] Executar `backend/mvnw.cmd verify`
- [x] Revisar aderencia entre implementacao, design e delta spec
- [x] Realizar revisao humana final antes do archive
- [x] Consolidar delta spec, atualizar fonte de verdade e arquivar change
