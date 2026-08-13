# Decisoes do convite de membro

Esta change nao pode avancar para design, delta spec ou implementacao enquanto decisoes que alterem seguranca, privacidade, modelo de dados ou comportamento de negocio permanecerem abertas.

## Destinatario do convite

Quem pode receber convite nesta primeira versao?

- Opcao A: somente conta interna existente, identificada por e-mail normalizado. Menor coleta, aceite pode exigir sessao da mesma conta e nao cria estado para pessoa sem conta.
- Opcao B: qualquer e-mail. Permite convite antes do cadastro, mas exige retencao do e-mail, fluxo de registro vinculado, tratamento de conta criada depois e maior superficie de enumeracao.

Decisao aprovada: opcao A. Pessoa cria conta interna antes de receber convite. Convite persistido referencia somente `recipientAccountId`; e-mail normalizado e usado na requisicao apenas para localizar conta e nao integra resposta, logs ou convite persistido.

## Autoridade e papel convidado

Quais memberships podem criar convite e quais papeis podem atribuir?

- Opcao A: `OWNER` pode convidar `ADMIN` ou `TECHNICIAN`; `ADMIN` pode convidar somente `TECHNICIAN`; nenhum convite cria `OWNER`.
- Opcao B: `OWNER` e `ADMIN` podem convidar qualquer papel, incluindo `OWNER`.

Decisao aprovada: opcao A. Preserva ownership e evita elevacao de privilegio por `ADMIN`.

## Entrega do token sem e-mail

Como token chega ao destinatario sem servico de e-mail nesta change?

- Opcao A: resposta de criacao retorna token opaco ou URL de aceite uma unica vez ao criador autorizado. Criador e responsavel por entrega externa; token nao e recuperavel depois.
- Opcao B: token nunca e retornado. Sem canal externo, convite nao pode ser aceito nesta capacidade.

Decisao aprovada: opcao A. Token deve ter ao menos 256 bits de entropia, ser transmitido somente sobre HTTPS, persistido somente em forma derivada e proibido em logs, auditoria, metricas e traces.

## Expiracao e aceite

Qual ciclo minimo de convite entra nesta change?

- Opcao A: convite `PENDING` expira apos sete dias; aceite autenticado pela conta destinataria cria membership uma unica vez. Convite expirado ou aceito nao pode ser reutilizado.
- Opcao B: convite nao expira ate ciclo de revogacao futuro.

Decisao aprovada: opcao A. Token de credencial temporaria precisa de expiracao. Revogacao manual permanece change posterior.

## Convite duplicado

Como tratar convite pendente para mesma organizacao e destinatario?

- Opcao A: permitir somente um convite pendente por par organizacao-destinatario; nova tentativa retorna resultado seguro sem criar token adicional.
- Opcao B: permitir varios convites pendentes; qualquer token pode ser aceito.

Decisao aprovada: opcao A. Reduz superficie de token, ambiguidade e entrega externa duplicada.

## Destinatario inexistente

Qual resposta recebe criador autorizado quando e-mail nao corresponde a conta interna?

- Opcao A: `404 Not Found`. Simples, mas permite enumeracao de contas.
- Opcao B: `202 Accepted` generico sem confirmacao de existencia. Nao revela falha ao criador, mas exige resposta indistinguivel para convite elegivel, duplicado ou destinatario inexistente.
- Opcao C: persistir convite antes de cadastro. Amplia escopo, retencao e lifecycle de identidade.

Decisao aprovada: opcao B. Sistema responde `202 Accepted` com token opaco de formato identico em todos os resultados autorizados. Somente convite elegivel e inedito e persistido; destinatario inexistente, ja membro ou com convite pendente recebe token de cobertura que nunca e persistido. Token de cobertura usa mesmo gerador, formato, tamanho e expiracao aparente de token persistido. Assim, resposta nao confirma se houve convite valido nem se conta existe.

## Retencao de convite

Convites guardam relacao entre organizacao, conta destinataria, papel pretendido e timestamps. Por quanto tempo convites `ACCEPTED` e `EXPIRED` permanecem armazenados?

Decisao aprovada: nesta change nao havera limpeza fisica. Convites `ACCEPTED` e `EXPIRED` permanecem armazenados enquanto necessarios para operacao e prevencao de reutilizacao. Politica definitiva de retencao, exclusao e audit trail deve ser definida em change propria antes de exposicao publica.

## Limites confirmados

- identidade do criador e aceitante vem exclusivamente da sessao server-side persistida;
- `HttpSession` e `JSESSIONID` nao sao fontes de identidade;
- token de convite nao e autenticacao e nao substitui sessao;
- envio de e-mail e outros canais externos permanecem fora do escopo;
- implementacao e revisao humana final foram aprovadas em 2026-08-08.
