# Open Questions

As decisões que bloqueavam o apply desta change foram resolvidas, e a change foi aprovada para implementação.

## Decisões resolvidas

- Ferramenta de build: Maven.
- Versão do Java: Java 21 LTS.
- Organização dos repositórios: monorepo com `backend/` e `frontend/`.
- Nome adotado na documentação: OrionTask.
- Repositório Git: repositório próprio na raiz do OrionTask.
- CI: GitHub Actions.
- Spring Boot: linha estável suportada mais recente compatível com Java 21, com versão fixada no `pom.xml` durante a implementação.
- Commits: Conventional Commits.
- Branches: `main` protegida e branches curtas por change.
- Integração: uma aprovação humana e CI verde.
- Idioma: código em inglês e documentação em pt-BR.
- Endpoint técnico: somente `/actuator/health`, sem detalhes internos.
- Build: Maven Wrapper versionado.
- Qualidade: Spotless e Checkstyle, sem meta percentual inicial de cobertura.
- CI: GitHub Actions executado somente em pull requests.
- Licença: repositório proprietário; o titular dos direitos autorais ainda precisa ser identificado para o arquivo legal definitivo.

## Pendência não bloqueante

- OpenAPI fica fora da bootstrap change. A fundação expõe exclusivamente `/actuator/health`; a documentação de API será introduzida com a primeira capacidade HTTP de negócio.

A disponibilidade de domínio, redes sociais e marcas semelhantes para OrionTask deve ser verificada antes de referências públicas definitivas. Isso não bloqueia a fundação técnica.

O titular dos direitos autorais deve ser identificado antes da inclusão de um arquivo de licença proprietário definitivo. Isso não bloqueia a fundação técnica.
