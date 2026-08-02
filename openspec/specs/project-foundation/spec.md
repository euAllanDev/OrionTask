# Project Foundation Specification

## Purpose

Definir a fundacao tecnica minima do OrionTask antes de capacidades de negocio.

## Requirements

### Requirement: Build reproduzivel

O backend DEVE possuir build reproduzivel com Java 21 LTS definido por `maven.compiler.release`, validado pelo Maven Enforcer e Maven Wrapper versionado.

#### Scenario: execucao em ambiente limpo

- DADO um ambiente compativel sem dependencias do projeto instaladas;
- QUANDO o comando documentado pelo Maven Wrapper for executado;
- ENTAO as dependencias DEVEM ser resolvidas;
- E a aplicacao DEVE compilar;
- E os testes DEVEM ser executados.

#### Scenario: execucao sem Maven global

- DADO um ambiente compativel sem Maven instalado globalmente;
- QUANDO o desenvolvedor executar `./mvnw verify` ou `mvnw.cmd verify`, conforme o sistema operacional;
- ENTAO o Maven Wrapper DEVE resolver a distribuicao necessaria;
- E o build DEVE executar sem depender de instalacao global do Maven.

### Requirement: Qualidade automatizada

O build DEVE verificar formatacao com Spotless e analise estatica com Checkstyle. A fundacao NAO DEVE impor meta percentual de cobertura.

#### Scenario: violacao de formatacao

- DADO um arquivo Java fora do padrao configurado pelo Spotless;
- QUANDO a verificacao do build for executada;
- ENTAO o build DEVE falhar.

#### Scenario: violacao de analise estatica

- DADO um arquivo Java que viola regra configurada no Checkstyle;
- QUANDO a verificacao do build for executada;
- ENTAO o build DEVE falhar.

### Requirement: PostgreSQL como banco de integracao

Testes de persistencia e migration DEVEM usar PostgreSQL real por Testcontainers.

#### Scenario: validacao de migrations

- DADO um container PostgreSQL limpo;
- QUANDO a suite de integracao iniciar;
- ENTAO o Flyway DEVE aplicar todas as migrations;
- E nenhuma migration DEVE falhar.

### Requirement: Protecao arquitetural

O build DEVE falhar quando regras centrais da arquitetura hexagonal forem violadas.

#### Scenario: dominio depende de Spring

- DADO uma classe no pacote de dominio;
- QUANDO ela depender de uma classe do Spring;
- ENTAO o teste arquitetural DEVE falhar.

#### Scenario: dominio depende de JPA

- DADO uma classe no pacote de dominio;
- QUANDO ela depender de `jakarta.persistence`;
- ENTAO o teste arquitetural DEVE falhar.

#### Scenario: controller acessa persistencia diretamente

- DADO um adapter de entrada web;
- QUANDO ele depender diretamente de um repository ou entidade de persistencia;
- ENTAO o teste arquitetural DEVE falhar.

### Requirement: Configuracao segura

Credenciais e segredos reais NAO DEVEM ser versionados.

#### Scenario: desenvolvedor configura ambiente local

- DADO o repositorio clonado;
- QUANDO o desenvolvedor seguir a documentacao;
- ENTAO ele DEVE conseguir configurar o ambiente usando variaveis locais;
- E o repositorio DEVE fornecer apenas valores de exemplo nao sensiveis.

### Requirement: Superficie HTTP minima

A fundacao NAO DEVE expor endpoints funcionais de negocio e DEVE expor somente `/actuator/health` como endpoint tecnico, sem detalhes internos.

#### Scenario: aplicacao inicial em execucao

- DADO o backend inicializado;
- QUANDO suas rotas forem inspecionadas;
- ENTAO apenas `/actuator/health` DEVE estar disponivel como endpoint tecnico;
- E nenhum endpoint de negocio DEVE estar disponivel;
- E a resposta de health NAO DEVE publicar detalhes internos.

### Requirement: Repositorio isolado

A raiz do OrionTask DEVE possuir repositorio Git proprio, sem herdar o repositorio do diretorio pessoal que a contem.

#### Scenario: verificacao da raiz do repositorio

- DADO o diretorio raiz do OrionTask;
- QUANDO o comando Git para identificar a raiz do repositorio for executado a partir desse diretorio;
- ENTAO ele DEVE retornar a propria raiz do OrionTask.

### Requirement: Integracao continua em pull requests

O GitHub Actions DEVE executar build, testes, validacao de migrations, regras arquiteturais, Spotless e Checkstyle em pull requests. O pipeline inicial NAO DEVE ser disparado por push direto.

#### Scenario: pull request aberto

- DADO uma pull request no repositorio;
- QUANDO ela for aberta ou atualizada;
- ENTAO o pipeline DEVE executar todas as verificacoes de qualidade definidas.

#### Scenario: push sem pull request

- DADO um push que nao pertence a uma pull request;
- QUANDO ele ocorrer;
- ENTAO o pipeline inicial NAO DEVE ser disparado.
