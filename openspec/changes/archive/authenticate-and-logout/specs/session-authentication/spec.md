# Delta Specification: Session Authentication

## ADDED Requirements

### Requirement: Sessão de autenticação própria

O sistema DEVE autenticar contas exclusivamente por sessão server-side própria, persistida no PostgreSQL e associada a uma única conta. A identidade autenticada DEVE ser recuperada exclusivamente dessa sessão persistida. O sistema NÃO DEVE usar JWT autocontido nem `HttpSession` como fonte da identidade autenticada.

### Requirement: Token opaco e cookie seguro

O sistema DEVE gerar token opaco com fonte criptograficamente segura e pelo menos 256 bits de entropia. O token bruto DEVE existir somente no cookie `__Host-oriontask-session`, configurado com `Secure`, `HttpOnly`, `SameSite=Lax` e `Path=/`, sem atributo `Domain`. O sistema NÃO DEVE persistir, retornar em JSON, registrar ou expor o token em logs, auditoria, métricas, traces ou mensagens de erro.

O banco DEVE armazenar somente representação criptográfica derivada, única e indexada do token. Argon2id DEVE ser usado exclusivamente para senhas e NÃO DEVE ser usado para tokens de sessão.

#### Scenario: credenciais válidas

- DADO uma conta com credenciais válidas;
- QUANDO a pessoa enviar login com token CSRF válido;
- ENTÃO o sistema DEVE criar novo token opaco, nova sessão server-side e novo contexto autenticado;
- E DEVE enviar somente o token no cookie `__Host-oriontask-session` seguro;
- E DEVE persistir somente a representação derivada única do token, associada à conta;
- E NÃO DEVE reutilizar token de autenticação anterior.

### Requirement: Contexto técnico do CSRF separado

Qualquer sessão técnica usada pelo mecanismo CSRF NÃO DEVE autenticar a conta, substituir a sessão persistida do OrionTask nem ser considerada fonte da identidade. Caso exista contexto técnico antes do login, seu identificador DEVE ser renovado conforme a proteção contra session fixation do Spring Security.

### Requirement: Proteção CSRF

Login, logout e operações autenticadas que alteram estado DEVEM exigir token CSRF válido. `GET /api/v1/csrf` DEVE poder ser acessado sem autenticação, NÃO DEVE autenticar a pessoa, NÃO DEVE alterar estado de negócio e NÃO DEVE retornar token de sessão. Logout NÃO DEVE ocorrer por `GET`, e métodos seguros NÃO DEVEM alterar estado.

#### Scenario: obtenção inicial de token CSRF

- DADO um cliente sem sessão autenticada;
- QUANDO solicitar `GET /api/v1/csrf`;
- ENTÃO o sistema DEVE emitir e retornar o token CSRF necessário para operações mutáveis;
- E NÃO DEVE autenticar o cliente nem retornar token de sessão.

#### Scenario: renovação do CSRF após login

- DADO um token CSRF usado em autenticação bem-sucedida;
- QUANDO uma nova sessão autenticada for criada;
- ENTÃO o token CSRF anterior DEVE ser invalidado;
- E o cliente DEVE conseguir obter novo token CSRF para operações posteriores em `GET /api/v1/csrf`.

#### Scenario: renovação do CSRF após logout

- DADO uma pessoa que solicitou logout com token CSRF válido;
- QUANDO o logout for concluído;
- ENTÃO o token CSRF anterior DEVE ser invalidado;
- E o cliente DEVE conseguir obter novo token CSRF em `GET /api/v1/csrf` antes da próxima operação mutável.

### Requirement: Expiração e atualização de atividade

Uma sessão DEVE expirar pelo primeiro limite atingido entre `última atividade + 30 minutos` e `data de criação + 8 horas`. A duração absoluta NÃO DEVE ser prorrogada pela atividade. Sessão expirada ou revogada NÃO DEVE autenticar, ser renovada, voltar a ser válida ou atualizar a última atividade.

O sistema PODE persistir a atualização de última atividade no máximo uma vez a cada cinco minutos por sessão, mas essa otimização NÃO DEVE permitir validade após trinta minutos reais sem atividade aceita pelo servidor.

#### Scenario: inatividade atingida

- DADO uma sessão sem atividade aceita pelo servidor por trinta minutos;
- QUANDO a sessão tentar autenticar requisição;
- ENTÃO a sessão NÃO DEVE autenticar;
- E NÃO DEVE atualizar sua última atividade.

#### Scenario: duração absoluta atingida

- DADO uma sessão com atividade recente;
- QUANDO completar oito horas desde sua criação;
- ENTÃO a sessão NÃO DEVE autenticar;
- E NÃO DEVE ter sua duração absoluta prorrogada pela atividade.

### Requirement: Logout idempotente e revogação individual

`DELETE /api/v1/session` DEVE exigir token CSRF válido, ser idempotente e retornar `204 No Content` quando a sessão estiver ativa, revogada, expirada, inexistente ou quando o cookie estiver ausente. O servidor DEVE sempre enviar a remoção do cookie `__Host-oriontask-session` e NÃO DEVE revelar se havia sessão válida.

Logout DEVE revogar somente a sessão apresentada. Revogação global NÃO DEVE ser exposta nesta change.

#### Scenario: logout de sessão ativa

- DADO uma sessão autenticada;
- QUANDO a pessoa solicitar logout com token CSRF válido;
- ENTÃO o sistema DEVE revogar a sessão apresentada;
- E DEVE retornar `204 No Content` e remover o cookie;
- E a sessão NÃO DEVE autenticar requisições futuras.

#### Scenario: logout sem sessão válida

- DADO cookie ausente, sessão expirada, revogada ou inexistente;
- QUANDO a pessoa solicitar logout com token CSRF válido;
- ENTÃO o sistema DEVE retornar `204 No Content`;
- E DEVE enviar a remoção do cookie;
- E NÃO DEVE revelar se existia sessão válida.

### Requirement: Resposta anti-enumeração

E-mail inexistente e senha incorreta DEVEM retornar exatamente `401` com a mensagem `E-mail ou senha inválidos.`. Status, corpo, headers relevantes e formato da resposta DEVEM ser idênticos. Quando a conta não existir, o sistema DEVE executar verificação equivalente de hash de senha usando hash seguro preparado para esse objetivo. O sistema NÃO DEVE criar sessão em nenhum fluxo de credencial inválida.

O sistema NÃO DEVE prometer tempo perfeitamente constante, mas DEVE evitar diferenças grosseiras e facilmente observáveis de processamento.

#### Scenario: e-mail inexistente

- DADO um e-mail sem conta associada;
- QUANDO uma pessoa solicitar login;
- ENTÃO o sistema DEVE executar trabalho de hash equivalente;
- E DEVE retornar a mesma resposta de senha incorreta;
- E NÃO DEVE criar sessão.

### Requirement: Proteção contra abuso

Falhas de login DEVEM ser controladas por identificador protegido derivado do e-mail normalizado, usando HMAC ou mecanismo equivalente sem armazenar e-mail bruto. O sistema DEVE limitar a cinco falhas em janela deslizante de quinze minutos, aplicar atraso progressivo e bloqueio temporário de quinze minutos após a quinta falha. Autenticação bem-sucedida DEVE remover ou reiniciar esse contador. Nenhuma resposta DEVE revelar a existência da conta.

Tentativas por origem DEVEM ser limitadas a vinte em janela deslizante de quinze minutos. Ao exceder, o sistema DEVE retornar `429 Too Many Requests` e DEVE incluir `Retry-After` quando aplicável. A origem DEVE ser extraída somente de informações confiáveis; headers como `X-Forwarded-For` NÃO DEVEM ser aceitos diretamente de clientes e somente PODEM ser considerados quando enviados por proxies explicitamente confiáveis.

#### Scenario: limite por identificador protegido

- DADO cinco falhas de login para o mesmo identificador protegido derivado do e-mail normalizado na janela vigente;
- QUANDO ocorrer nova tentativa antes do fim do bloqueio;
- ENTÃO o sistema DEVE aplicar bloqueio temporário sem revelar a existência da conta;
- E NÃO DEVE criar sessão.

#### Scenario: limite por origem

- DADO vinte tentativas de login da mesma origem confiável na janela vigente;
- QUANDO ocorrer nova tentativa;
- ENTÃO o sistema DEVE retornar `429 Too Many Requests`;
- E DEVE incluir `Retry-After` quando aplicável;
- E NÃO DEVE criar sessão.

### Requirement: Identidade da sessão e isolamento

Uma sessão válida DEVE disponibilizar internamente o `accountId` autenticado para adapters e casos de uso protegidos. A identidade DEVE vir exclusivamente da sessão e NÃO DEVE aceitar identificador de conta fornecido pelo cliente como substituto.

Cada sessão DEVE pertencer a uma única conta. Revogar ou encerrar uma sessão NÃO DEVE revogar outras sessões da mesma conta nesta change. Sessões de contas diferentes DEVEM permanecer isoladas.

#### Scenario: identidade da sessão

- DADO uma sessão associada à conta A;
- QUANDO a sessão autenticar uma requisição;
- ENTÃO o contexto autenticado DEVE identificar exclusivamente a conta A;
- E NÃO DEVE aceitar um identificador de conta fornecido pelo cliente como substituto.

#### Scenario: logout em uma de duas sessões da mesma conta

- DADO duas sessões válidas da mesma conta;
- QUANDO uma delas for encerrada;
- ENTÃO somente essa sessão DEVE deixar de autenticar;
- E a outra sessão DEVE permanecer válida até expirar ou ser revogada.

#### Scenario: sessões de contas diferentes

- DADO uma sessão válida da conta A e uma sessão válida da conta B;
- QUANDO a sessão da conta A autenticar uma requisição;
- ENTÃO ela NÃO DEVE autenticar como conta B;
- E a revogação da sessão da conta A NÃO DEVE afetar a sessão da conta B.

### Requirement: Dados e auditoria mínimos

Sessões DEVEM persistir somente dados necessários à autenticação e revogação. Auditoria mínima DEVE registrar login bem-sucedido, login rejeitado, limitação aplicada, logout, revogação de sessão e, quando necessário para segurança, tentativa com sessão expirada ou revogada.

Logs, auditoria, métricas e traces NÃO DEVEM conter senha, hash de senha, token de sessão, representação derivada do token quando desnecessária, cookie, token CSRF, e-mail bruto ou payload completo de autenticação.
