# Tasks: Convite de membro

## 1. Revisao e aprovacao

- [x] Decidir destinatario, matriz de papeis, token, expiracao, duplicidade e resposta para conta inexistente
- [x] Decidir lifecycle persistido e estrategia de unicidade parcial no PostgreSQL
- [x] Decidir retencao de convites `ACCEPTED` e `EXPIRED`
- [x] Revisar proposal, design, delta spec e riscos de seguranca e privacidade
- [x] Aprovar humanamente a change para implementacao

## 2. Dominio e aplicacao

- [x] Criar modelo Java puro para convite, token derivado, expiracao e consumo
- [x] Definir e implementar lifecycle persistido `PENDING`, `ACCEPTED` e `EXPIRED`
- [x] Criar casos de uso para criar e aceitar convite e ports necessarios
- [x] Aplicar matriz `OWNER`/`ADMIN` e impedir convite `OWNER`
- [x] Implementar token criptografico e derivacao sem expor segredo
- [x] Criar testes unitarios de papeis, expiracao, token e aceite atomico

## 3. Persistencia e migracao

- [x] Criar migration versionada para convites, lifecycle persistido e indice parcial de unicidade somente para `PENDING`
- [x] Implementar persistencia de token derivado e consulta segura para aceite
- [x] Implementar transacao que cria membership e consome convite
- [x] Garantir que convite expirado transiciona para `EXPIRED` e nao bloqueia novo convite
- [x] Criar testes PostgreSQL/Testcontainers para indice parcial, reemissao apos expiracao, concorrencia e isolamento

## 4. API e seguranca

- [x] Criar endpoint autenticado e protegido por CSRF para criar convite
- [x] Criar endpoint autenticado e protegido por CSRF para aceitar convite
- [x] Retornar cobertura `202` indistinguivel para destinatario inexistente, membro existente e convite duplicado
- [x] Cobrir formato, tamanho e expiracao aparente indistinguiveis de token de cobertura
- [x] Retornar `404` indistinguivel para token invalido, expirado, aceito ou de conta diferente
- [x] Cobrir aceite concorrente e membership criada entre emissao e aceite
- [x] Confirmar que somente convite persistido produz `membership_invitation.created`
- [x] Cobrir ausencia de sessao, CSRF, UUID e payload invalidos, autoridade e isolamento entre organizacoes
- [x] Confirmar que logs e respostas nao incluem e-mail, token bruto, derivacao ou payload completo

## 5. Verificacao

- [x] Executar `backend/mvnw.cmd spotless:apply`
- [x] Executar `backend/mvnw.cmd verify`
- [x] Revisar aderencia entre implementacao, design e delta spec
- [x] Realizar revisao humana final antes do archive

## Evidencias

- `backend/mvnw.cmd verify` concluiu em 2026-08-08 com 43 testes aprovados, Flyway V5, PostgreSQL/Testcontainers, ArchUnit, Spotless e Checkstyle.

## Estado da change

- revisao humana final aprovada em 2026-08-08;
- change arquivada em 2026-08-08.
