# Design: Convite de membro

## Decisao principal

`OWNER` e `ADMIN` criam convite para conta interna por e-mail normalizado. Convite persistido aponta para `recipientAccountId`, nunca para e-mail. `OWNER` pode convidar `ADMIN` ou `TECHNICIAN`; `ADMIN` somente `TECHNICIAN`; convite nunca concede `OWNER`.

Criador recebe `202 Accepted` e token opaco uma vez. Para impedir enumeracao, resposta tem mesmo status e formato quando destinatario nao existe, ja e membro ou ja possui convite pendente. Nesses casos, token de cobertura e gerado mas nunca persistido. Criador entrega token por canal externo sob propria responsabilidade; envio de e-mail fica fora desta change.

## Modelo, lifecycle e invariantes

Nova entidade de dominio `MembershipInvitation` possui UUID nao sequencial, `organizationId`, `recipientAccountId`, `role`, derivacao de token, `status`, `expiresAt`, `createdAt` e `acceptedAt`. Status persistido e restrito a `PENDING`, `ACCEPTED` e `EXPIRED`; nao ha enum generico de lifecycle. Dados pessoais minimos sao IDs internos; e-mail entra somente na requisicao para localizar conta e nao e persistido neste agregado.

Convite nasce `PENDING`. Aceite valido muda para `ACCEPTED` e define `acceptedAt`. Ao ser observado vencido durante criacao ou aceite, muda para `EXPIRED`; job de expiracao fica fora desta change. `PENDING` deve bloquear novo convite; `EXPIRED` permite novo convite; `ACCEPTED` nao bloqueia por status, mas membership existente recebe resposta de cobertura. Banco deve usar indice parcial PostgreSQL `UNIQUE (organization_id, recipient_account_id) WHERE status = 'PENDING'`. Antes de criar novo convite, aplicacao deve bloquear registro pendente do par, transicionar registro vencido para `EXPIRED` e criar novo `PENDING` na mesma transacao; indice parcial resolve corrida entre criacoes.

Membership continua protegida por `UNIQUE(account_id, organization_id)`. Aceite deve bloquear convite e verificar novamente membership existente. Se membership tiver sido criada por outro fluxo, convite deve mudar para `ACCEPTED` sem criar duplicata e resposta externa deve ser `404` generico. Aceite deve criar membership e mudar convite para `ACCEPTED` na mesma transacao. Concorrencia permite no maximo um aceite efetivo; tentativa perdedora recebe mesmo `404` de token consumido.

Token usa fonte criptograficamente segura com ao menos 256 bits de entropia. Persistencia armazena somente derivacao unidirecional com segredo de servidor quando aplicavel; token bruto nao volta a ser recuperavel. Expira sete dias apos criacao. Token aceito, expirado, inexistente ou de cobertura retorna resultado indistinguivel e nao cria membership.

## API e autorizacao

`POST /api/v1/organizations/{organizationId}/invitations` recebe exclusivamente `{ "recipientEmail": "...", "role": "ADMIN|TECHNICIAN" }`, exige sessao autenticada e CSRF valido. Adapter obtem `accountId` somente da sessao server-side persistida. Aplicacao primeiro autoriza membership criadora pela dupla `organizationId` e `accountId`; UUID valido inexistente ou sem membership retorna `404` indistinguivel.

Para criador autorizado e entrada valida, resposta sempre retorna `202 Accepted` com `{ "token": "...", "expiresAt": "..." }`. Somente destinatario existente, nao membro e sem convite `PENDING` cria registro persistido; os demais resultados recebem token de cobertura identico, gerado pelo mesmo gerador criptografico, com mesmo formato, tamanho e `expiresAt` aparente de `now + 7 dias`, mas nunca persistido. Papel reconhecido mas nao convidavel para criador, incluindo `OWNER`, retorna `403`; papel desconhecido, UUID ou payload malformado, campo adicional, e-mail vazio ou formato invalido retorna `400` sem persistencia.

`POST /api/v1/membership-invitations/{token}/accept` exige sessao autenticada e CSRF valido. Token nao autentica solicitante. Aplicacao localiza convite por derivacao e o bloqueia, verifica que conta autenticada e `recipientAccountId`, confirma `PENDING` e nao expirado, e cria membership no papel convidado atomicamente. Sucesso retorna `201 Created` com dados minimos da membership. Token invalido, expirado, aceito, de cobertura ou destinado a outra conta retorna `404` indistinguivel. Ausencia de sessao segue politica global de autenticacao.

## Arquitetura

- dominio `organization` mantem entidade de convite, estados e regras de transicao em Java puro;
- aplicacao define casos de uso para criar e aceitar, ports para conta destinataria, convite e membership;
- adapter HTTP nao acessa repositorios nem aceita `accountId` do cliente;
- adapter de persistencia aplica escopo organizacional para criacao e transacao atomica para aceite;
- Spring Security somente fornece identidade autenticada da sessao persistida;
- derivacao e geracao de token ficam atras de ports e implementacoes de infraestrutura, reutilizando convencoes seguras existentes quando compativeis.

## Privacidade e operacao

E-mail tem finalidade unica de localizar conta existente e nao entra em tabela de convite, resposta, log, auditoria, metrica ou trace. Token bruto, derivacao, cookies e payload completo nao entram em logs. Depois de commit de convite persistido, registrar evento estruturado minimo `membership_invitation.created`; resposta de cobertura nao gera evento falso. Depois de aceite efetivo, registrar `membership_invitation.accepted` somente com `invitationId`, `organizationId`, `recipientAccountId` e `membershipId`. Eventos nao contem e-mail, token ou papel. Falha de log apos commit nao causa rollback.

## Fora do escopo

- entrega, reenvio ou rastreamento de e-mail e outros canais externos;
- cancelamento, revogacao ou listagem de convites;
- remocao, revogacao ou alteracao de memberships;
- criacao de conta pelo fluxo de convite;
- transferencia de ownership e RBAC generico;
- audit trail persistida completa.
