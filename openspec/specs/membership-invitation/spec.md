# Membership Invitation Specification

## Purpose

Definir convite temporario e autorizado para adicionar conta interna existente a organizacao sem entrega de e-mail nesta capacidade.

## Requirements

### Requirement: Criacao autorizada e papeis limitados

`POST /api/v1/organizations/{organizationId}/invitations` DEVE exigir sessao server-side persistida e CSRF valido. Sistema DEVE aceitar somente `recipientEmail` e `role`. `OWNER` pode convidar `ADMIN` ou `TECHNICIAN`; `ADMIN` pode convidar somente `TECHNICIAN`; `TECHNICIAN` nao pode convidar; convite NUNCA DEVE conceder `OWNER`.

UUID valido sem organizacao ou sem membership criadora DEVE retornar `404 Not Found` indistinguivel. Papel conhecido, mas nao permitido para criador, incluindo `OWNER`, DEVE retornar `403 Forbidden`. Papel desconhecido, UUID, e-mail, payload ou campo adicional invalidos DEVEM retornar `400 Bad Request` sem persistencia.

### Requirement: Privacidade de destinatario e resposta de cobertura

E-mail normalizado DEVE ser usado somente para localizar conta interna durante criacao. Convite persistido DEVE referenciar somente `recipientAccountId`; e-mail NAO DEVE ser persistido, retornado, registrado em logs, metricas, traces ou eventos operacionais.

Para criador autorizado e entrada valida, sistema DEVE retornar `202 Accepted` com `token` opaco e `expiresAt` quando destinatario nao existe, ja possui membership ou ja possui convite pendente. Somente destinatario existente, sem membership e sem convite pendente DEVE gerar convite persistido. Token de cobertura DEVE usar mesmo gerador criptografico, formato, tamanho e expiracao aparente de `now + 7 dias`, mas NAO DEVE ser persistido nem aceito.

### Requirement: Lifecycle, token, expiracao e unicidade

Convite DEVE ter status persistido `PENDING`, `ACCEPTED` ou `EXPIRED`, `acceptedAt` e `expiresAt`. Convite novo nasce `PENDING`; aceite efetivo muda para `ACCEPTED` e define `acceptedAt`; convite `PENDING` vencido observado durante criacao ou aceite muda para `EXPIRED`. Nao ha job de expiracao nesta capacidade.

Token DEVE ter pelo menos 256 bits de entropia criptografica, ser exposto somente na resposta de criacao e persistido somente como derivacao unidirecional. Token bruto, derivacao e payload completo NAO DEVEM entrar em logs, eventos, metricas ou traces.

PostgreSQL DEVE impor no maximo um convite `PENDING` por `(organization_id, recipient_account_id)` com indice parcial. Criacao deve observar convite pendente vencido, muda-lo para `EXPIRED` e criar novo `PENDING` na mesma transacao. `EXPIRED` permite novo convite. `ACCEPTED` nao bloqueia por status, mas membership existente recebe resposta de cobertura.

### Requirement: Aceite autenticado e atomico

`POST /api/v1/membership-invitations/{token}/accept` DEVE exigir sessao autenticada e CSRF valido. Token NAO DEVE autenticar solicitante. Sistema DEVE bloquear convite localizado e aceitar somente token `PENDING`, nao expirado e destinado ao `accountId` da sessao persistida.

Aceite deve criar membership no papel convidado e mudar convite para `ACCEPTED` na mesma transacao. Sistema DEVE verificar novamente membership: se ela ja existir por outro fluxo, DEVE inutilizar convite sem criar duplicata e retornar `404 Not Found` generico. Token invalido, de cobertura, expirado, aceito ou destinado a outra conta DEVE retornar `404 Not Found` indistinguivel.

Aceites concorrentes DEVEM produzir no maximo uma membership e um consumo efetivo; tentativa perdedora retorna mesmo `404` de token consumido.

### Requirement: Evidencia operacional minima e retencao

Sistema DEVE emitir `membership_invitation.created` somente depois de commit de convite persistido e `membership_invitation.accepted` somente depois de aceite que criou membership. Eventos contem somente `invitationId`, `organizationId`, `recipientAccountId` e, no aceite, `membershipId`; NAO DEVEM conter e-mail, token, derivacao, papel ou payload completo. Falha de log apos commit NAO DEVE causar rollback.

Esta capacidade nao implementa limpeza fisica. Convites `ACCEPTED` e `EXPIRED` permanecem armazenados enquanto necessarios para operacao e prevencao de reutilizacao. Politica definitiva de retencao, exclusao e audit trail deve ser definida em change propria antes de exposicao publica.
