# Open Questions: Identidade da sessao atual

## Q1 - Dados retornados

Decisao: resposta autenticada de `GET /api/v1/session` retorna exclusivamente:

```json
{
  "accountId": "uuid",
  "email": "user@example.com"
}
```

`email` e o valor normalizado armazenado: trim e lowercase. Backend nao preserva forma original. Resposta nao inclui token, cookie, derivacao, timestamps de sessao, organizacao, membership ou papel.

## Q2 - Sessao ausente ou invalida

Decisao: sessao ausente, expirada ou revogada retorna `401 Unauthorized` generico. Resposta nao diferencia causa nem confirma se cookie apresentado existiu ou foi valido anteriormente.

## Q3 - CSRF e autenticacao

Decisao: `GET /api/v1/session` nao exige CSRF por ser leitura sem mutacao. Identidade vem exclusivamente da sessao server-side persistida, pelo filtro de autenticacao existente; `JSESSIONID`, `HttpSession`, `accountId` em query ou header nao sao fontes de identidade.

## Q4 - Privacidade e finalidade

Decisao: e-mail normalizado tem finalidade exclusiva de identificar conta autenticada na interface. Nao deve entrar em logs, auditoria, metricas, traces, mensagens de erro ou respostas de recursos organizacionais por esta change.
