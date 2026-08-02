# Retomada da Proxima Sessao

## Ponto de partida

Retomar na change ativa `bootstrap-project-foundation`. A aprovação humana do delta spec foi confirmada e o build atual passou com `mvnw.cmd verify` em 2 de agosto de 2026, com Docker e Testcontainers, mas a change continua em implementação.

Java 21 é definido por `maven.compiler.release` e validado pelo Maven Enforcer. Maven Toolchains não será usado nesta fundação para evitar a configuração local adicional de `toolchains.xml`. O próximo passo é validar o build em clone limpo.

## Estado atual

- nome adotado na documentacao: OrionTask;
- Java 21 LTS, Maven e Maven Wrapper;
- monorepo com `backend/` e `frontend/`;
- GitHub Actions em pull requests;
- Spotless, Checkstyle, ArchUnit, Flyway e Testcontainers;
- codigo em ingles e documentacao em pt-BR;
- `main` protegida, branches curtas e Conventional Commits;
- uma aprovacao humana e CI verde para integrar em `main`;
- repositorio proprietario, sem titular de direitos autorais definido;
- portal do cliente permanece na Fase 4; o MVP interno cadastra e vincula clientes, sem acesso externo.
- repositorio Git proprio na raiz do OrionTask, com `backend/mvnw` rastreado como executavel;
- health check restrito a `/actuator/health`, sem detalhes, componentes ou grupos;
- `mvnw.cmd verify` passou em 2 de agosto de 2026 com PostgreSQL 17 no Testcontainers, Flyway, ArchUnit, Spotless e Checkstyle.
- alterações da fundação estão no staging area, sem commit criado.

## Artefatos para ler primeiro

1. `AGENTS.md`
2. `docs/constitution/mission.md`
3. `docs/constitution/principles.md`
4. `docs/constitution/tech-stack.md`
5. `docs/constitution/roadmap.md`
6. `openspec/specs/project-governance/spec.md`
7. todos os arquivos em `openspec/changes/bootstrap-project-foundation/`, incluindo este.

## Próximos passos

1. Executar verificação em clone limpo com o Maven Wrapper.
2. Realizar revisão humana da implementação contra `proposal.md`, `design.md` e o delta spec.
3. Criar o commit da fundação após confirmação humana do diff staged.
4. Confirmar que a fundação expõe exclusivamente `/actuator/health`; OpenAPI foi adiada para a primeira capacidade HTTP de negócio.
5. Abrir pull request e aguardar CI verde.
6. Após a integração, arquivar a change e incorporar o delta em `openspec/specs/`.

Nao criar funcionalidades de identidade, organizacoes, clientes, tickets, frontend, anexos ou notificacoes nesta change.

## Pendencias nao bloqueantes

- validar dominio, redes sociais e marcas semelhantes de OrionTask antes de referencias publicas definitivas;
- identificar o titular dos direitos autorais antes de adicionar um arquivo de licenca proprietario definitivo.
