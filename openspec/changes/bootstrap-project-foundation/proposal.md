# Proposal: Bootstrap da fundação do OrionTask

## Change ID

`bootstrap-project-foundation`

## Status

Em implementação. A fundação foi registrada no commit `bfa5a79`. A verificacao em clone limpo com `mvnw.cmd verify` passou em 2 de agosto de 2026, com Docker e Testcontainers. Faltam a pull request com CI verde e o archive da change.

## Problema

Antes desta change, o projeto não possuía repositório próprio, convenções, estrutura arquitetural executável ou pipeline de qualidade. Iniciar diretamente por funcionalidades criaria risco de decisões implícitas, inconsistência arquitetural e perda de contexto entre sessões do agente.

## Objetivo

Criar a fundação mínima do backend e do workflow de desenvolvimento sem implementar regras funcionais de tickets.

## Escopo

- monorepo com projeto Spring Boot compilável em `backend/` e diretório reservado para `frontend/`;
- Maven com Java 21 LTS definido por `maven.compiler.release` e validado pelo Maven Enforcer;
- Maven Wrapper versionado;
- repositório Git próprio na raiz do OrionTask;
- organização inicial para monólito modular e arquitetura hexagonal;
- PostgreSQL local;
- Flyway;
- testes;
- ArchUnit;
- Spotless e Checkstyle;
- CI no GitHub Actions executado somente em pull requests;
- tratamento básico de configuração;
- health check técnico em `/actuator/health`, sem detalhes internos;
- convenções de segurança para segredos;
- convenções de contribuição para commits, branches e revisão;
- documentação de qualidade, sem meta percentual inicial de cobertura;
- documentação para execução local.

A versão exata do Spring Boot será fixada na implementação, usando a linha estável suportada mais recente compatível com Java 21.

## Fora do escopo

- cadastro;
- login;
- organizações;
- tickets;
- usuários;
- autorização funcional;
- frontend;
- pagamentos;
- anexos;
- notificações.
- documentação OpenAPI, que será introduzida com a primeira capacidade HTTP de negócio.

## Resultado esperado

Um repositório vazio de funcionalidades, porém capaz de compilar, testar, subir localmente e impedir violações arquiteturais fundamentais.

## Riscos

- criar abstrações hexagonais sem casos de uso reais;
- antecipar estrutura excessiva de módulos;
- misturar entidades de domínio com JPA;
- adicionar bibliotecas sem necessidade;
- gerar configuração insegura de desenvolvimento.

## Critérios mínimos de sucesso

- build reproduzível;
- testes executáveis;
- PostgreSQL local funcional;
- primeira migration técnica;
- health check;
- regras ArchUnit passando;
- domínio de exemplo inexistente ou mínimo e não funcional;
- nenhum segredo versionado;
- README operacional.
