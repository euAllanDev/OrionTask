# Membership Invitation Specification

## Purpose

Definir convite temporario e autorizado para adicionar conta interna existente a organizacao sem entrega de e-mail nesta capacidade.

## Requirements

### Requirement: Criacao autorizada e papeis limitados

Sistema DEVE aceitar criacao somente de `OWNER` para `ADMIN` ou `TECHNICIAN` e de `ADMIN` para `TECHNICIAN`. Convite NUNCA DEVE conceder `OWNER`. Criador DEVE ser identificado exclusivamente pela sessao server-side persistida e autorizado por `organizationId` e `accountId`. `POST /api/v1/organizations/{organizationId}/invitations` DEVE exigir CSRF e aceitar somente `recipientEmail` e `role`.

UUID valido sem organizacao ou sem membership criadora DEVE retornar `404 Not Found` indistinguivel. Papel alem da autoridade do criador DEVE retornar `403 Forbidden`. UUID, e-mail, payload ou campo adicional invalidos DEVEM retornar `400 Bad Request`, sem convite persistido.

#### Scenario: admin convida tecnico

- DADO `ADMIN` autenticado com membership na organizacao;
- QUANDO criar convite com papel `TECHNICIAN` para conta existente;
- ENTAO sistema DEVE persistir convite pendente e retornar `202 Accepted` com token opaco e expiracao.

#### Scenario: admin tenta convidar admin

- DADO `ADMIN` autenticado com membership na organizacao;
- QUANDO criar convite com papel `ADMIN`;
- ENTAO sistema DEVE retornar `403 Forbidden`;
- E NAO DEVE persistir convite.

### Requirement: Privacidade de destinatario e resposta de cobertura

Convite persistido DEVE referenciar somente `recipientAccountId`; e-mail normalizado DEVE ser usado somente para localizar conta durante criacao. Sistema NAO DEVE persistir e-mail em convite, nem inclui-lo em resposta, logs, metricas, traces ou eventos operacionais.

Para criador autorizado e payload valido, sistema DEVE retornar `202 Accepted` com mesmo formato de token opaco e expiracao quando destinatario nao existe, ja possui membership ou ja possui convite pendente. Somente destinatario existente, sem membership e sem convite pendente DEVE gerar convite persistido. Token de cobertura NAO DEVE ser persistido nem aceito; DEVE usar mesmo gerador criptografico, formato, tamanho e `expiresAt` aparente de `now + 7 dias` do token persistido.

#### Scenario: e-mail sem conta interna

- DADO criador autorizado e e-mail valido sem conta interna;
- QUANDO criar convite;
- ENTAO sistema DEVE retornar `202 Accepted` com formato identico ao de convite persistido;
- E NAO DEVE persistir convite nem confirmar existencia de conta.

### Requirement: Lifecycle, token, expiracao e unicidade

Convite DEVE ter status persistido `PENDING`, `ACCEPTED` ou `EXPIRED`, `acceptedAt` e `expiresAt`. Convite novo DEVE nascer `PENDING`; aceite efetivo DEVE mudar para `ACCEPTED` e definir `acceptedAt`; convite `PENDING` vencido observado durante criacao ou aceite DEVE mudar para `EXPIRED`. Nao ha job de expiracao nesta change.

Token de convite DEVE ter ao menos 256 bits de entropia criptografica, ser exposto somente na resposta de criacao e persistido somente como derivacao unidirecional. Token bruto, derivacao e payload completo NAO DEVEM ser registrados em logs, eventos, metricas ou traces. `PENDING` DEVE expirar sete dias apos criacao. PostgreSQL DEVE impor no maximo um convite `PENDING` por `(organization_id, recipient_account_id)` com indice parcial. Antes de criar convite, sistema DEVE expirar pendente vencido na mesma transacao; convite `EXPIRED` permite novo convite. `ACCEPTED` nao bloqueia por status, mas membership existente recebe resposta de cobertura.

#### Scenario: convite duplicado

- DADO convite pendente para organizacao e destinatario;
- QUANDO criador autorizado enviar novo convite para mesmo destinatario;
- ENTAO sistema DEVE retornar resposta de cobertura `202 Accepted`;
- E NAO DEVE criar segundo convite persistido.

#### Scenario: novo convite apos expiracao

- DADO convite `PENDING` vencido para organizacao e destinatario;
- QUANDO criador autorizado enviar novo convite para mesmo destinatario;
- ENTAO sistema DEVE mudar convite anterior para `EXPIRED`;
- E DEVE persistir novo convite `PENDING`.

### Requirement: Aceite autenticado e atomico

`POST /api/v1/membership-invitations/{token}/accept` DEVE exigir sessao autenticada e CSRF valido. Token NAO DEVE autenticar nem identificar solicitante. Sistema DEVE bloquear convite localizado, aceitar somente token `PENDING`, nao expirado e destinado ao `accountId` da sessao persistida. Aceite DEVE criar membership no papel convidado e mudar convite para `ACCEPTED` na mesma transacao.

Sucesso DEVE retornar `201 Created` com dados minimos da membership. Sistema DEVE verificar membership novamente no aceite. Se ela ja existir, DEVE mudar convite para `ACCEPTED` sem criar duplicata e retornar `404 Not Found` generico. Token invalido, de cobertura, expirado, aceito ou destinado a conta diferente DEVE retornar `404 Not Found` com mesmo corpo e formato e sem criar membership.

#### Scenario: destinatario aceita convite valido

- DADO conta autenticada destinataria de convite pendente nao expirado;
- QUANDO aceitar token com CSRF valido;
- ENTAO sistema DEVE criar membership com papel convidado e consumir convite atomicamente;
- E DEVE retornar `201 Created`.

#### Scenario: outra conta tenta aceitar token

- DADO convite pendente destinado a conta diferente da autenticada;
- QUANDO aceitar token;
- ENTAO sistema DEVE retornar `404 Not Found`;
- E NAO DEVE criar membership nem consumir convite.

#### Scenario: aceite concorrente

- DADO convite `PENDING` valido para conta autenticada;
- QUANDO duas requisicoes tentarem aceita-lo concorrentemente;
- ENTAO no maximo uma DEVE criar membership;
- E convite DEVE ser consumido uma unica vez;
- E outra tentativa DEVE receber resposta indistinguivel de token ja consumido.

#### Scenario: membership criada antes do aceite

- DADO convite `PENDING` para conta destinataria;
- E DADO membership da destinataria criada por outro fluxo antes do aceite;
- QUANDO destinataria aceitar token;
- ENTAO sistema NAO DEVE criar membership duplicada;
- E DEVE mudar convite para `ACCEPTED`;
- E DEVE retornar `404 Not Found` generico.

### Requirement: Evidencia operacional minima

Sistema DEVE emitir `membership_invitation.created` somente depois de commit de convite efetivamente persistido. Resposta de cobertura NAO DEVE produzir evento falso. Depois de aceite que criou membership, sistema DEVE emitir `membership_invitation.accepted` somente com `invitationId`, `organizationId`, `recipientAccountId` e `membershipId`. Eventos NAO DEVEM conter e-mail, token, derivacao, papel ou payload completo; falha de log apos commit NAO DEVE causar rollback.
