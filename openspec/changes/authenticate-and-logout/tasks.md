# Tasks: Autenticação e encerramento de sessão

## 1. Revisão e aprovação

- [x] Revisar proposal, design, delta spec e decisões de segurança
- [x] Resolver todas as decisões bloqueantes
- [x] Aprovar a change para implementação

## 2. Domínio e aplicação

- [x] Criar modelo de sessão própria e ports para consulta de conta, verificação de senha, sessão, revogação, abuso, auditoria, geração e derivação de token
- [x] Implementar login e criação de sessão server-side persistida sem usar `HttpSession` como fonte de identidade
- [x] Implementar contexto autenticado interno com `accountId` derivado exclusivamente da sessão persistida
- [x] Implementar expiração pelo menor valor entre trinta minutos de inatividade e oito horas de duração absoluta
- [x] Garantir que sessão expirada ou revogada não autentique, não seja renovada, não seja reutilizada e não atualize última atividade
- [x] Implementar atualização limitada da última atividade, no máximo uma vez a cada cinco minutos por sessão, sem estender a inatividade real
- [x] Tornar o logout idempotente e restrito à sessão atual
- [x] Retornar resposta uniforme para e-mail inexistente e senha incorreta
- [x] Executar verificação de hash equivalente quando a conta não existir
- [ ] Criar testes unitários de autenticação, expiração, atividade limitada, revogação e isolamento

## 3. Persistência e segurança

- [x] Criar migration de sessões com UUIDs, conta, representação derivada única do token, criação, atividade, expiração absoluta, revogação e índices de busca, conta e limpeza
- [x] Gerar token opaco com ao menos 256 bits de entropia criptograficamente segura
- [x] Persistir somente representação derivada do token, usando HMAC ou hash adequado; não usar Argon2id para token de sessão
- [x] Configurar cookie `__Host-oriontask-session` com `HttpOnly`, `Secure`, `SameSite=Lax`, `Path=/` e sem `Domain`
- [x] Garantir que token, cookie e representações derivadas não apareçam em respostas JSON, logs, auditoria, métricas, traces ou erros
- [x] Integrar a sessão persistida ao Spring Security sem criar mecanismo concorrente de autenticação
- [x] Configurar proteção CSRF para login, logout e rotas mutáveis
- [x] Emitir token CSRF em `GET /api/v1/csrf` sem autenticar ou expor token de sessão
- [x] Renovar e invalidar token CSRF após login, logout, invalidação ou rejeição
- [x] Manter o Spring Security stateless, sem criar contexto técnico antes do login que exija renovação contra session fixation
- [x] Implementar limite de cinco falhas por identificador protegido derivado do e-mail normalizado em quinze minutos, com atraso progressivo e bloqueio de quinze minutos
- [x] Reiniciar ou remover contador por identificador protegido após autenticação bem-sucedida
- [x] Implementar limite de vinte tentativas por origem confiável em quinze minutos com `429` e `Retry-After` quando aplicável
- [x] Configurar suporte a proxies explicitamente confiáveis e nunca confiar diretamente em headers enviados pelo cliente
- [x] Registrar auditoria mínima sem dados sensíveis
- [x] Criar testes de integração com PostgreSQL e Testcontainers

## 4. API e verificação

- [x] Criar endpoint de login com token CSRF obrigatório
- [x] Criar `DELETE /api/v1/session` com token CSRF obrigatório, resultado idempotente `204 No Content` e remoção incondicional do cookie
- [x] Confirmar que login e logout não ocorrem por `GET`
- [x] Cobrir emissão e renovação de CSRF antes do login, após login e após logout
- [ ] Cobrir acesso anônimo, sessão válida, credencial inválida, expiração por inatividade, duração absoluta e revogação
- [ ] Cobrir logout com sessão ativa, ausente, inexistente, expirada e revogada
- [ ] Cobrir isolamento entre duas sessões da mesma conta e entre sessões de contas diferentes
- [ ] Confirmar que uma sessão não autentica outra conta nem aceita `accountId` do cliente como substituto
- [ ] Cobrir rate limiting por identificador protegido e por origem confiável
- [x] Executar `backend/mvnw.cmd spotless:apply`
- [x] Executar `backend/mvnw.cmd verify`
- [x] Revisar o diff e confirmar ausência de dados sensíveis
- [x] Revisar aderência entre implementação e delta spec
- [ ] Atualizar evidências e criar commit seguindo Conventional Commits
- [ ] Realizar revisão humana antes do archive

## Evidências

- `backend/mvnw.cmd verify` concluído em 2026-08-04: 11 testes aprovados, incluindo PostgreSQL/Testcontainers, Flyway, Spotless, Checkstyle e ArchUnit.
- Permanecem pendentes os cenários dedicados de expiração, logout sem sessão válida, isolamento entre sessões e rate limiting.
