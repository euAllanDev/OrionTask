# Delta Specification: Project Foundation

## ADDED Requirements

### Requirement: Build reproduzível

O backend DEVE possuir build reproduzível com Java 21 LTS definido por `maven.compiler.release`, validado pelo Maven Enforcer e Maven Wrapper versionado.

#### Scenario: execução em ambiente limpo

- DADO um ambiente compatível sem dependências do projeto instaladas;
- QUANDO o comando documentado pelo Maven Wrapper for executado;
- ENTÃO as dependências DEVEM ser resolvidas;
- E a aplicação DEVE compilar;
- E os testes DEVEM ser executados.

#### Scenario: execução sem Maven global

- DADO um ambiente compatível sem Maven instalado globalmente;
- QUANDO o desenvolvedor executar `./mvnw verify` ou `mvnw.cmd verify`, conforme o sistema operacional;
- ENTÃO o Maven Wrapper DEVE resolver a distribuição necessária;
- E o build DEVE executar sem depender de instalação global do Maven.

### Requirement: Qualidade automatizada

O build DEVE verificar formatação com Spotless e análise estática com Checkstyle. A fundação NÃO DEVE impor meta percentual de cobertura.

#### Scenario: violação de formatação

- DADO um arquivo Java fora do padrão configurado pelo Spotless;
- QUANDO a verificação do build for executada;
- ENTÃO o build DEVE falhar.

#### Scenario: violação de análise estática

- DADO um arquivo Java que viola regra configurada no Checkstyle;
- QUANDO a verificação do build for executada;
- ENTÃO o build DEVE falhar.

### Requirement: PostgreSQL como banco de integração

Testes de persistência e migration DEVEM usar PostgreSQL real por Testcontainers.

#### Scenario: validação de migrations

- DADO um container PostgreSQL limpo;
- QUANDO a suíte de integração iniciar;
- ENTÃO o Flyway DEVE aplicar todas as migrations;
- E nenhuma migration DEVE falhar.

### Requirement: Proteção arquitetural

O build DEVE falhar quando regras centrais da arquitetura hexagonal forem violadas.

#### Scenario: domínio depende de Spring

- DADO uma classe no pacote de domínio;
- QUANDO ela depender de uma classe do Spring;
- ENTÃO o teste arquitetural DEVE falhar.

#### Scenario: domínio depende de JPA

- DADO uma classe no pacote de domínio;
- QUANDO ela depender de `jakarta.persistence`;
- ENTÃO o teste arquitetural DEVE falhar.

#### Scenario: controller acessa persistência diretamente

- DADO um adapter de entrada web;
- QUANDO ele depender diretamente de um repository ou entidade de persistência;
- ENTÃO o teste arquitetural DEVE falhar.

### Requirement: Configuração segura

Credenciais e segredos reais NÃO DEVEM ser versionados.

#### Scenario: desenvolvedor configura ambiente local

- DADO o repositório clonado;
- QUANDO o desenvolvedor seguir a documentação;
- ENTÃO ele DEVE conseguir configurar o ambiente usando variáveis locais;
- E o repositório DEVE fornecer apenas valores de exemplo não sensíveis.

### Requirement: Superfície HTTP mínima

A fundação NÃO DEVE expor endpoints funcionais de negócio e DEVE expor somente `/actuator/health` como endpoint técnico, sem detalhes internos.

#### Scenario: aplicação inicial em execução

- DADO o backend inicializado;
- QUANDO suas rotas forem inspecionadas;
- ENTÃO apenas `/actuator/health` DEVE estar disponível como endpoint técnico;
- E nenhum endpoint de negócio DEVE estar disponível;
- E a resposta de health NÃO DEVE publicar detalhes internos.

### Requirement: Repositório isolado

A raiz do OrionTask DEVE possuir repositório Git próprio, sem herdar o repositório do diretório pessoal que a contém.

#### Scenario: verificação da raiz do repositório

- DADO o diretório raiz do OrionTask;
- QUANDO o comando Git para identificar a raiz do repositório for executado a partir desse diretório;
- ENTÃO ele DEVE retornar a própria raiz do OrionTask.

### Requirement: Integração contínua em pull requests

O GitHub Actions DEVE executar build, testes, validação de migrations, regras arquiteturais, Spotless e Checkstyle em pull requests. O pipeline inicial NÃO DEVE ser disparado por push direto.

#### Scenario: pull request aberto

- DADO uma pull request no repositório;
- QUANDO ela for aberta ou atualizada;
- ENTÃO o pipeline DEVE executar todas as verificações de qualidade definidas.

#### Scenario: push sem pull request

- DADO um push que não pertence a uma pull request;
- QUANDO ele ocorrer;
- ENTÃO o pipeline inicial NÃO DEVE ser disparado.
