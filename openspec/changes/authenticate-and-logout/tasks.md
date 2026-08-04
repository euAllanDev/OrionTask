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
- [x] Criar testes de autenticação válida e credenciais inválidas
- [x] Criar testes de expiração por inatividade e duração absoluta
- [x] Criar testes de revogação da sessão
- [x] Criar teste dedicado para atualização limitada da última atividade
- [x] Criar testes de isolamento entre contas diferentes

## 3. Persistência e segurança

- [x] Criar migration de sessões com UUIDs, conta, representação derivada única do token, criação, atividade, expiração absoluta, revogação e índices de busca, conta e limpeza
- [x] Gerar token opaco com ao menos 256 bits de entropia criptograficamente segura
- [x] Persistir somente HMAC-SHA-256 do token de sessão; não usar Argon2id para token de sessão
- [x] Configurar cookie `__Host-oriontask-session` com `HttpOnly`, `Secure`, `SameSite=Lax`, `Path=/` e sem `Domain`
- [x] Garantir que token, cookie e representações derivadas não apareçam em respostas JSON, logs, auditoria, métricas, traces ou erros
- [x] Integrar a sessão persistida ao Spring Security sem criar mecanismo concorrente de autenticação
- [x] Não utilizar `HttpSession` ou `JSESSIONID` como fonte da identidade autenticada
- [x] Persistir e recuperar o contexto autenticado exclusivamente pela sessão própria do OrionTask
- [x] Configurar proteção CSRF para login, logout e rotas mutáveis
- [x] Emitir token CSRF em `GET /api/v1/csrf` sem autenticar ou expor token de sessão
- [x] Configurar `JSESSIONID` técnico com `HttpOnly`, `Secure`, `SameSite=Lax`, `Path=/` e sem `Domain` em produção
- [x] Configurar timeout de trinta minutos para a sessão técnica de CSRF
- [x] Invalidar o token CSRF anterior após login bem-sucedido
- [x] Invalidar o token CSRF anterior após logout
- [x] Permitir obtenção de novo token por `GET /api/v1/csrf`
- [x] Implementar limite de cinco falhas por identificador protegido em quinze minutos
- [x] Implementar bloqueio temporário de quinze minutos
- [x] Implementar atraso progressivo sem bloquear threads por longos períodos
- [x] Reiniciar ou remover contador após autenticação bem-sucedida
- [x] Implementar limite de vinte tentativas por origem em quinze minutos
- [x] Retornar `429` e `Retry-After` quando aplicável
- [x] Ignorar headers de origem encaminhados quando não houver proxy confiável configurado
- [x] Preparar configuração explícita para proxies confiáveis
- [x] Registrar auditoria mínima sem dados sensíveis
- [x] Criar testes de integração com PostgreSQL e Testcontainers

## 4. API e verificação

- [x] Criar endpoint de login com token CSRF obrigatório
- [x] Criar `DELETE /api/v1/session` com token CSRF obrigatório, resultado idempotente `204 No Content` e remoção incondicional do cookie
- [x] Confirmar que login e logout não ocorrem por `GET`
- [x] Cobrir emissão de CSRF antes do login
- [x] Cobrir obtenção de novo CSRF após login
- [x] Cobrir obtenção de novo CSRF após logout
- [x] Cobrir acesso anônimo ao endpoint de CSRF
- [x] Cobrir criação de sessão válida
- [x] Cobrir credencial inválida
- [x] Cobrir expiração por inatividade
- [x] Cobrir duração absoluta
- [x] Cobrir revogação
- [x] Cobrir logout com sessão ativa
- [x] Cobrir logout repetido
- [x] Cobrir logout sem cookie
- [x] Cobrir logout com token ou sessão inexistente
- [x] Cobrir logout com sessão expirada
- [x] Cobrir logout com sessão previamente revogada
- [x] Cobrir isolamento entre duas sessões da mesma conta
- [x] Cobrir isolamento entre sessões de contas diferentes
- [x] Confirmar que uma sessão não aceita `accountId` fornecido pelo cliente como substituto da identidade autenticada
- [x] Cobrir rejeição de requisição com token CSRF ausente
- [x] Cobrir rejeição de requisição com token CSRF inválido
- [x] Cobrir rejeição de token CSRF antigo após login ou logout
- [x] Cobrir rotação do `JSESSIONID` técnico e invalidação do CSRF após login
- [x] Cobrir rate limiting por identificador protegido
- [x] Cobrir rate limiting por origem confiável
- [x] Cobrir atraso progressivo entre falhas
- [x] Cobrir bloqueio temporário de quinze minutos
- [x] Cobrir reinício do contador após autenticação bem-sucedida
- [x] Executar `backend/mvnw.cmd spotless:apply`
- [x] Executar `backend/mvnw.cmd verify`
- [x] Revisar o diff e confirmar ausência de dados sensíveis
- [x] Revisar aderência entre implementação e delta spec
- [x] Atualizar evidências e criar commit seguindo Conventional Commits
- [x] Realizar revisão humana final antes do archive

## Evidências

- `backend/mvnw.cmd verify` concluído em 2026-08-04 com 26 testes aprovados, incluindo PostgreSQL/Testcontainers, Flyway, Spotless, Checkstyle e ArchUnit.
- Foram validados: criação de sessão, credencial inválida, expiração por inatividade, duração absoluta, revogação, atualização limitada da atividade, rate limiting por identificador e origem, atraso progressivo, bloqueio temporário, reinício do contador após login bem-sucedido, resposta `429` com `Retry-After`, logout ativo, repetido, sem cookie, com token inexistente e com sessão expirada, isolamento entre sessões da mesma conta e de contas diferentes, e rejeição de CSRF ausente, inválido e antigo.
- O contexto técnico de CSRF usa `HttpSession` apenas para o token CSRF; a identidade autenticada continua derivada exclusivamente da sessão persistida do OrionTask.
- Foi validado que o `JSESSIONID` técnico emitido para CSRF tem `HttpOnly`, `Secure`, `SameSite=Lax` e `Path=/`; login rotaciona esse identificador, rejeita o token anterior e aceita novo token na sessão técnica rotacionada.

## Estado da change

- implementação principal concluída;
- build verde;
- cobertura de critérios implementada e verificada;
- revisão humana final aprovada em 2026-08-04;
- change pronta para archive.
