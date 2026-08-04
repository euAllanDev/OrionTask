# Retomada da Próxima Sessão

## Estado atual

A change `authenticate-and-logout` foi aprovada para implementação em 2026-08-04. A implementação de autenticação, sessão persistida, CSRF, logout e migration foi concluída e validada por `backend/mvnw.cmd verify` com PostgreSQL/Testcontainers.

## Decisões aprovadas

- sessões server-side com token opaco;
- trinta minutos de inatividade e oito horas de duração absoluta;
- cookie `__Host-oriontask-session` com `Secure`, `HttpOnly`, `SameSite=Lax`, `Path=/` e sem `Domain`;
- CSRF em login, logout e operações mutáveis, com bootstrap em `GET /api/v1/csrf`;
- resposta uniforme `401` para e-mail inexistente ou senha incorreta;
- cinco falhas por identificador protegido e vinte tentativas por origem, ambos em quinze minutos;
- logout idempotente da sessão atual;
- revogação global permanece fora do escopo.

## Conclusão

A revisão humana final foi aprovada em 2026-08-04. A change foi concluída sem incluir organização, membership, papéis, alteração ou recuperação de senha, alteração de e-mail, MFA ou revogação global.
