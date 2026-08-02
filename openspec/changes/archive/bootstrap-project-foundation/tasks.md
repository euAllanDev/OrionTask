# Tasks: Bootstrap da fundação

## 1. Decisões aprovadas

- [x] Aprovar a change para implementação
- [x] Aprovar Maven
- [x] Aprovar Java 21 LTS
- [x] Aprovar monorepo com `backend/` e `frontend/`
- [x] Registrar decisões no `design.md`
- [x] Definir convenções de contribuição
- [x] Registrar ADR das decisões de fundação

## 2. Projeto backend

- [x] Criar a estrutura do monorepo e gerar projeto Spring Boot mínimo em `backend/`
- [x] Inicializar repositório Git na raiz do OrionTask
- [x] Configurar build reproduzível
- [x] Versionar Maven Wrapper
- [x] Definir Java 21 por `maven.compiler.release` e validar a versão com Maven Enforcer, sem Maven Toolchains nesta fundação
- [x] Configurar formatação e análise estática mínimas
- [x] Criar configuração por ambiente
- [x] Criar `.env.example`
- [x] Manter consistentes os valores locais de PostgreSQL em `.env.example`, `compose.yaml` e `application-local.yml`
- [x] Garantir que segredos não sejam versionados

## 3. Banco

- [x] Criar PostgreSQL no Docker Compose
- [x] Configurar datasource
- [x] Configurar Flyway
- [x] Avaliar migration técnica inicial: não necessária sem estruturas técnicas ou de negócio
- [x] Validar migrations com Testcontainers

## 4. Arquitetura

- [x] Criar testes ArchUnit
- [x] Impedir dependência do domínio em Spring
- [x] Impedir dependência do domínio em JPA
- [x] Impedir acesso direto de adapters de entrada à persistência
- [x] Documentar convenções de pacotes

## 5. Operação

- [x] Configurar Actuator
- [x] Expor apenas health necessário
- [x] Configurar respostas de erro técnicas seguras
- [x] Criar instruções para execução local

## 6. CI

- [x] Configurar GitHub Actions
- [x] Compilar
- [x] Executar testes
- [x] Executar regras arquiteturais
- [x] Executar verificações Spotless e Checkstyle
- [x] Validar migrations
- [x] Falhar em qualquer violação

## 7. Verificação

- [x] Clonar em ambiente limpo e executar `mvnw.cmd verify` em 2 de agosto de 2026
- [x] Subir PostgreSQL
- [x] Executar aplicação
- [x] Confirmar health check
- [x] Executar todos os testes
- [x] Confirmar ausência de endpoints de negócio
- [x] Confirmar ausência de segredos
- [x] Executar `mvnw.cmd verify` com Docker e Testcontainers em 2 de agosto de 2026
- [x] Comparar implementação com proposal e design em revisão humana
