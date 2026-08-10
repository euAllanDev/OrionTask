# Tasks: Gestao de clientes

## 1. Revisao e aprovacao

- [x] Decidir dados minimos, matriz de autoridade, lifecycle, unicidade e listagem
- [x] Revisar e aprovar proposal, design e delta spec

## 2. Dominio e aplicacao

- [x] Criar modelo Java puro e estados fixos de cliente
- [x] Criar casos de uso, comandos, resultados e portas necessarias
- [x] Aplicar matriz `OWNER`/`ADMIN`/`TECHNICIAN`
- [x] Cobrir validacao, lifecycle e autorizacao em testes unitarios

## 3. Persistencia e migracao

- [x] Criar migration versionada de clientes e indice organizacional
- [x] Implementar persistencia escopada por organizacao e status
- [x] Criar testes PostgreSQL/Testcontainers para migration, isolamento e desativacao

## 4. API, seguranca e operacao

- [x] Criar rotas HTTP e validar contratos estritos
- [x] Exigir CSRF em mutacoes e nao em leituras
- [x] Retornar `404` indistinguivel para recursos nao autorizados ou ausentes
- [x] Registrar eventos operacionais minimos apos commit
- [x] Cobrir HTTP, autorizacao, CSRF e isolamento

## 5. Verificacao e archive

- [x] Executar `backend/mvnw.cmd spotless:apply`
- [x] Executar `backend/mvnw.cmd verify`
- [x] Revisar aderencia entre implementacao, design e delta spec
- [x] Realizar revisao humana final antes do archive
- [x] Consolidar delta spec, atualizar roadmap e README, arquivar change

## Estado da change

- revisao humana final aprovada em 2026-08-09;
- change arquivada em 2026-08-09.

## Evidencias

- `backend/mvnw.cmd verify` concluiu em 2026-08-09 com 54 testes aprovados, PostgreSQL/Testcontainers, Flyway V6, ArchUnit, Spotless e Checkstyle.
