# Design: Autenticação e encerramento de sessão

## Decisão principal

O OrionTask usará exclusivamente uma sessão de autenticação server-side própria, persistida no PostgreSQL, identificada por token opaco e associada a uma conta. JWT autocontido não será usado.

A autenticação não utilizará a HttpSession como fonte da identidade. A identidade autenticada será recuperada exclusivamente da sessão persistida do OrionTask. Qualquer sessão técnica usada pelo mecanismo CSRF não autentica a conta nem substitui a sessão de autenticação.

O estado técnico de CSRF tem ciclo de vida separado da sessão de autenticação. Ele pode exigir estado próprio do Spring Security, mas não cria um segundo mecanismo de autenticação e deve ser renovado após login, logout, invalidação ou rejeição do token CSRF.

## Sessão e token de autenticação

- cada login bem-sucedido cria novo token opaco, nova sessão persistida e novo contexto autenticado;
- o login nunca reutiliza um token de autenticação anterior. Qualquer contexto de sessão técnica criado antes do login deve ter seu identificador renovado;
- o token é gerado por fonte criptograficamente segura com pelo menos 256 bits de entropia;
- o token bruto existe somente no cookie do cliente e nunca é persistido, retornado em JSON, registrado em logs, auditoria, métricas, traces ou mensagens de erro;
- o banco armazena somente uma representação criptográfica derivada, única e indexada, que pode ser HMAC com chave do servidor ou hash criptográfico adequado para token aleatório;
- Argon2id permanece exclusivo para senhas e não deve ser usado para token de sessão, que precisa de busca eficiente;
- a decisão exata da derivação pertence ao adapter de segurança e não ao domínio;
- cada sessão registra conta, representação derivada do token, criação, última atividade, expiração absoluta e revogação.

## Cookie de autenticação

O adapter HTTP grava somente o token opaco no cookie `__Host-oriontask-session`, com `Secure`, `HttpOnly`, `SameSite=Lax` e `Path=/`, sem atributo `Domain`. O cookie não é acessível ao JavaScript.

## Expiração e atividade

- a expiração efetiva é o menor valor entre `última atividade + 30 minutos` e `data de criação + 8 horas`;
- a duração absoluta não é prorrogada por atividade;
- sessões expiradas ou revogadas não autenticam, não são renovadas, não voltam a ser válidas e não atualizam a última atividade;
- a última atividade pode ser persistida no máximo uma vez a cada cinco minutos por sessão, para evitar escrita em toda requisição autenticada;
- essa otimização não pode permitir validade após trinta minutos reais sem atividade aceita pelo servidor;
- a estratégia de atualização limitada pertence ao adapter de persistência e deve ser coberta por testes.

## Fluxo HTTP e CSRF

1. O cliente obtém token CSRF em `GET /api/v1/csrf`. O endpoint é público, não autentica, não altera estado de negócio e não retorna token de sessão.
2. `POST /api/v1/sessions` recebe e-mail, senha e token CSRF.
3. O adapter HTTP valida o contrato, valida CSRF, extrai a origem somente de proxies confiáveis e encaminha dados ao caso de uso.
4. A aplicação normaliza o e-mail, consulta os limites de abuso com identificador protegido derivado do e-mail normalizado e executa verificação de hash equivalente quando a conta não existir.
5. Em sucesso, a aplicação cria a sessão server-side, registra auditoria mínima e o adapter grava somente o token opaco no cookie.
6. Após login, o token CSRF usado é invalidado; o cliente deve obter novo token em `GET /api/v1/csrf` antes da próxima operação mutável.
7. Em e-mail inexistente ou senha incorreta, o adapter retorna `401` com `E-mail ou senha inválidos.`, com status, corpo, headers relevantes e formato idênticos, sem criar sessão.
8. `DELETE /api/v1/session` exige token CSRF válido, revoga somente a sessão apresentada quando aplicável, remove sempre o cookie e retorna `204 No Content` sem revelar se havia sessão válida.
9. Após logout, o token CSRF anterior é invalidado; o cliente deve obter novo token em `GET /api/v1/csrf` antes da próxima operação mutável.

O token CSRF é obrigatório para login, logout e demais operações autenticadas que alteram estado. GET, HEAD e demais métodos seguros não alteram estado. `SameSite=Lax` é defesa adicional e não substitui CSRF.

## Integração com Spring Security

O mecanismo que autentica requisições deve:

1. ler o cookie `__Host-oriontask-session`;
2. derivar sua representação segura;
3. localizar a sessão persistida;
4. verificar revogação e expiração efetiva;
5. identificar a conta associada;
6. criar o contexto autenticado para a requisição;
7. nunca confiar em `accountId` enviado pelo cliente.

O contexto autenticado é derivado exclusivamente da sessão persistida e disponibiliza internamente o `accountId` para adapters e casos de uso protegidos. Não há endpoint público `/me` nesta change.

## Logout e revogação

Logout é idempotente: com CSRF válido, retorna `204 No Content` e remove o cookie tanto para sessão ativa quanto para sessão revogada, expirada, inexistente ou cookie ausente. Apenas a sessão apresentada é revogada; outras sessões da mesma conta permanecem válidas. Revogação global permanece fora do escopo.

## Proteção contra abuso e enumeração

- falhas por identificador protegido derivado do e-mail normalizado são limitadas a cinco em janela deslizante de quinze minutos, com atraso progressivo e bloqueio temporário de quinze minutos após a quinta falha;
- autenticação bem-sucedida remove ou reinicia o contador por identificador protegido;
- tentativas por origem são limitadas a vinte em janela deslizante de quinze minutos e recebem `429 Too Many Requests`, com `Retry-After` quando aplicável;
- o identificador protegido usa HMAC ou mecanismo equivalente e não armazena e-mail bruto no controle de abuso;
- a origem é extraída somente de informações confiáveis; `X-Forwarded-For` somente é considerado quando enviado por proxy explicitamente confiável;
- o atraso progressivo não deve prender threads por longos períodos; a implementação deve preferir bloqueio temporal e controle de janela a chamadas longas de `sleep`;
- nenhuma resposta de falha ou limitação revela a existência da conta;
- não se promete tempo perfeitamente constante, mas devem ser evitadas diferenças grosseiras e facilmente observáveis de processamento.

Estados de conta que futuramente impeçam autenticação poderão usar a mesma resposta uniforme, mas esta change não cria estado de conta bloqueada, desativada, indisponível ou pendente.

## Auditoria e logs

Auditoria mínima registra login bem-sucedido, login rejeitado, limitação aplicada, logout, revogação de sessão e, quando necessário para segurança, tentativa com sessão expirada ou revogada. Após login bem-sucedido, pode registrar o UUID da conta. Antes de identificar a conta com segurança, não registra e-mail bruto.

Logs, auditoria, métricas e traces não contêm senha, hash de senha, token de sessão, representação derivada do token quando desnecessária, cookie, token CSRF, e-mail bruto ou payload completo de autenticação.

## Arquitetura e persistência

- `identity.domain` permanece Java puro e não conhece Spring Security, HTTP, cookie, CSRF ou JPA;
- a aplicação define ports para consulta de conta, verificação de senha, criação e consulta de sessão, revogação, controle de abuso, auditoria, geração de token e derivação de token;
- o adapter de segurança implementa Argon2id, tokens e integração com Spring Security;
- o adapter de persistência implementa PostgreSQL/JPA;
- o adapter HTTP valida contrato, cookie, CSRF e origem;
- regras de negócio não ficam em controllers ou filtros.

A migration de sessões deve prever UUID da sessão e da conta, representação derivada única do token, criação, última atividade, informações necessárias ao cálculo da inatividade, expiração absoluta, revogação e índices para token, conta, expiração e limpeza. A exclusão física e retenção de sessões antigas ficam para change operacional futura, mas o modelo deve permitir limpeza eficiente.

## Fora do escopo

- revogação global, listagem de sessões e dispositivos;
- alteração e recuperação de senha;
- confirmação e alteração de e-mail;
- MFA, login social e sem senha;
- organização, membership, papéis e autorização de recursos organizacionais;
- endpoint público de perfil ou sessão.
