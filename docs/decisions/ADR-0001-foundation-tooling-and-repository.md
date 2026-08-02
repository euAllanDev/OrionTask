# ADR-0001: Ferramentas e Repositorio da Fundacao

## Status

Aceito.

## Contexto

O OrionTask precisa de uma fundacao reproduzivel e simples de operar antes das changes de negocio. A pasta atual herda um repositorio Git do diretorio pessoal, o que impede o versionamento isolado do projeto. Tambem era necessario definir ferramentas de build, qualidade e integracao continua para a primeira change.

## Decisao

- usar Java 21 LTS e Maven no backend;
- versionar o Maven Wrapper;
- manter um monorepo com `backend/`, `frontend/`, `docs/` e `openspec/`;
- inicializar um repositorio Git proprio na raiz do OrionTask;
- usar GitHub Actions somente em pull requests;
- usar Spotless e Checkstyle, sem meta percentual inicial de cobertura;
- integrar em `main` somente com CI verde e uma aprovacao humana;
- manter o repositorio proprietario ate a definicao do titular dos direitos autorais.

## Alternativas consideradas

- Gradle Kotlin DSL foi descartado em favor de Maven pela previsibilidade inicial;
- Java 25 foi descartado em favor da estabilidade do Java 21 LTS;
- repositorios separados foram descartados para manter specs, documentacao e aplicacoes coordenadas;
- CI em todos os pushes foi descartado nesta fase para limitar o gatilho a pull requests;
- uma meta de cobertura foi descartada para evitar percentual artificial antes de haver dominio de negocio.

## Consequencias

- a implementacao deve gerar `mvnw`, `mvnw.cmd` e `.mvn/` versionados;
- o pipeline inicial deve executar build, testes, Testcontainers, Flyway, ArchUnit, Spotless e Checkstyle em pull requests;
- a protecao efetiva da branch `main` sera configurada quando o repositorio estiver hospedado;
- a versao exata do Spring Boot sera fixada no `pom.xml` durante a implementacao, a partir da linha estavel suportada mais recente compativel com Java 21;
- nenhum arquivo de licenca definitivo deve ser criado antes de identificar o titular dos direitos autorais.

## Relacao com a change

`bootstrap-project-foundation`
