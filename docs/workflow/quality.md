# Qualidade e CI

## Build local

O backend deve versionar o Maven Wrapper. Os comandos documentados usam `./mvnw` em ambientes POSIX e `mvnw.cmd` no Windows, sem exigir uma instalacao global do Maven.

## Verificacoes

O build deve executar:

- compilacao e testes relevantes;
- testes de integracao com PostgreSQL por Testcontainers;
- validacao de migrations Flyway;
- regras arquiteturais ArchUnit;
- verificacao de formatacao com Spotless;
- verificacao estatica com Checkstyle.

Nao ha meta percentual de cobertura na fundacao. A exigencia e que cada change tenha os testes relevantes aos seus riscos e requisitos.

## CI

O GitHub Actions executa o pipeline somente em pull requests. A integracao em `main` depende do pipeline verde e da aprovacao humana exigida em `docs/workflow/contributing.md`.

## Licenca

O repositorio e proprietario por enquanto. Nenhuma licenca open source e concedida nesta fase. O arquivo legal definitivo depende da identificacao do titular dos direitos autorais.
