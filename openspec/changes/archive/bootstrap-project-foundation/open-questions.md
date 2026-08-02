# Decisões da bootstrap

As decisões bloqueantes desta change foram resolvidas e aprovadas para implementação.

## Decisões resolvidas

- Ferramenta de build: Maven.
- Versão do Java: Java 21 LTS.
- Organização dos repositórios: monorepo com `backend/` e `frontend/`.
- Nome adotado na documentação: OrionTask.
- Repositório Git: repositório próprio na raiz do OrionTask.
- Spring Boot: versão 4.1.0 fixada no `pom.xml`.
- Build: Maven Wrapper versionado.
- Versionamento: o projeto utilizará commits pequenos e frequentes, preferencialmente um conjunto coerente de commits por change, seguindo Conventional Commits.
- Integração inicial: nesta etapa, não haverá exigência de pull request.
- Revisão humana: a revisão continuará obrigatória antes do archive, mesmo sem uso de pull request.
- Branches: branches curtas por change continuam recomendadas, mas o fluxo poderá ser simplificado enquanto o projeto for desenvolvido individualmente.
- Idioma: código em inglês e documentação em pt-BR.
- Endpoint técnico: somente `/actuator/health`, sem detalhes internos.
- Qualidade: Spotless e Checkstyle, sem meta percentual inicial de cobertura.
- CI: GitHub Actions está preparado no repositório, mas o fluxo inicial não depende de pull requests. Os gatilhos definitivos do pipeline serão revisados quando o processo de colaboração e integração for formalizado.
- Licença: repositório proprietário; o titular dos direitos autorais ainda precisa ser identificado para o arquivo legal definitivo.

A bootstrap somente pode ser arquivada após revisão humana do diff, execução bem-sucedida de `backend/mvnw.cmd verify`, validação do Maven Wrapper, confirmação do Git isolado na raiz do OrionTask, criação dos commits, atualização da documentação e tasks e aderência entre implementação, proposal, design e delta spec.

## Decisões adiadas

- OpenAPI será introduzido na primeira change que expuser uma API de negócio.
- A bootstrap expõe exclusivamente `/actuator/health`.
- Os gatilhos definitivos do GitHub Actions serão revisados em uma change operacional futura.

## Pendências não bloqueantes

- A disponibilidade de domínio, redes sociais e marcas semelhantes para OrionTask deve ser verificada antes de referências públicas definitivas.
- O titular dos direitos autorais deve ser identificado antes da inclusão de um arquivo de licença proprietário definitivo.
