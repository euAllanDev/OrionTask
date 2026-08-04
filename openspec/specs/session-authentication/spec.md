# Session Authentication Specification

## Purpose

Definir a autenticacao propria e o encerramento seguro de sessoes das contas internas do OrionTask.

## Requirements

### Requirement: Sessao de autenticacao propria

O sistema DEVE autenticar contas exclusivamente por sessao server-side propria, persistida no PostgreSQL e associada a uma unica conta. A identidade autenticada DEVE ser recuperada exclusivamente dessa sessao persistida. O sistema NAO DEVE usar JWT autocontido nem `HttpSession` como fonte da identidade autenticada.

O token opaco DEVE ter ao menos 256 bits de entropia de fonte criptograficamente segura e existir somente no cookie `__Host-oriontask-session`, configurado com `Secure`, `HttpOnly`, `SameSite=Lax` e `Path=/`, sem `Domain`. O banco DEVE armazenar somente uma representacao criptografica derivada, unica e indexada. Argon2id e exclusivo para senhas. O token, o cookie e sua representacao derivada nao DEVEM aparecer em respostas JSON, logs, auditoria, metricas, traces ou erros.

### Requirement: Contexto tecnico de CSRF separado

O contexto tecnico de CSRF NAO DEVE autenticar a conta, substituir a sessao persistida do OrionTask nem ser considerado fonte da identidade. O `JSESSIONID` tecnico deve usar `HttpOnly`, `Secure`, `SameSite=Lax` e `Path=/`, sem `Domain`, e expirar apos trinta minutos. O perfil local pode desabilitar `Secure` somente para desenvolvimento HTTP sem TLS. O identificador desse contexto DEVE ser renovado apos login e invalidado apos logout.

Login, logout e operacoes autenticadas mutaveis DEVEM exigir token CSRF valido. `GET /api/v1/csrf` DEVE ser publico, nao autenticar a pessoa, nao alterar estado de negocio e nao retornar token de sessao.

### Requirement: Expiracao e atividade

Uma sessao DEVE expirar pelo primeiro limite atingido entre `ultima atividade + 30 minutos` e `data de criacao + 8 horas`. A duracao absoluta NAO DEVE ser prorrogada pela atividade. Sessoes expiradas ou revogadas NAO DEVEM autenticar, ser renovadas, voltar a ser validas ou atualizar a ultima atividade.

A ultima atividade PODE ser persistida no maximo uma vez a cada cinco minutos por sessao, sem permitir validade apos trinta minutos reais sem atividade aceita pelo servidor.

### Requirement: Logout idempotente e revogacao individual

`DELETE /api/v1/session` DEVE exigir token CSRF valido, ser idempotente e retornar `204 No Content` quando a sessao estiver ativa, revogada, expirada, inexistente ou quando o cookie estiver ausente. O servidor DEVE sempre remover o cookie `__Host-oriontask-session` sem revelar se havia sessao valida.

Logout DEVE revogar somente a sessao apresentada. Revogacao global NAO DEVE ser exposta nesta capacidade.

### Requirement: Resposta anti-enumeracao e protecao contra abuso

E-mail inexistente e senha incorreta DEVEM retornar exatamente `401` com a mensagem `E-mail ou senha inválidos.` e sem criar sessao. Quando a conta nao existir, o sistema DEVE executar verificacao de hash equivalente.

Falhas de login DEVEM ser controladas por identificador protegido derivado do e-mail normalizado, sem armazenar e-mail bruto: cinco falhas em quinze minutos, atraso progressivo e bloqueio temporario de quinze minutos apos a quinta falha. Autenticacao bem-sucedida DEVE reiniciar esse contador.

Tentativas por origem DEVEM ser limitadas a vinte em quinze minutos. Ao exceder, o sistema DEVE retornar `429 Too Many Requests` com `Retry-After` quando aplicavel. Headers encaminhados somente PODEM ser considerados de proxies explicitamente confiaveis.

### Requirement: Identidade da sessao e isolamento

Uma sessao valida DEVE disponibilizar internamente o `accountId` autenticado exclusivamente a partir da sessao persistida. O sistema NAO DEVE aceitar identificador de conta enviado pelo cliente como substituto.

Cada sessao DEVE pertencer a uma unica conta. Revogar uma sessao NAO DEVE afetar outras sessoes da mesma conta ou de contas diferentes.

### Requirement: Dados e auditoria minimos

Sessoes DEVEM persistir somente dados necessarios a autenticacao e revogacao: UUID da sessao e da conta, representacao derivada unica do token, criacao, ultima atividade, expiracao absoluta e revogacao. Auditoria minima DEVE registrar login bem-sucedido, login rejeitado, limitacao aplicada, logout e revogacao de sessao, sem dados sensiveis.

Logs, auditoria, metricas e traces NAO DEVEM conter senha, hash de senha, token de sessao, representacao derivada do token quando desnecessaria, cookie, token CSRF, e-mail bruto ou payload completo de autenticacao.
