# Design: Bootstrap da fundação do OrionTask

## Decisão principal

Criar somente a infraestrutura necessária para receber as primeiras mudanças de domínio, evitando modelar antecipadamente tickets, organizações ou usuários.

## Estrutura proposta

```text
.
├── backend/
│   ├── src/main/java/com/oriontask/
│   │   ├── OrionTaskApplication.java
│   │   ├── shared/
│   │   │   └── architecture/
│   │   └── configuration/
│   ├── src/test/java/com/oriontask/
│   │   └── architecture/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/
│   ├── pom.xml
│   └── compose.yaml
├── frontend/
├── docs/
└── openspec/
```

Módulos de negócio serão criados apenas nas changes correspondentes.

## Decisões aprovadas

- Maven é a ferramenta de build;
- Java 21 LTS é definido por `maven.compiler.release` e validado pelo Maven Enforcer, sem Maven Toolchains nesta fundação;
- o projeto será um monorepo, com backend e frontend como aplicações separadas;
- a raiz do OrionTask terá repositório Git próprio, independente do diretório pessoal que hoje o contém;
- o CI será implementado com GitHub Actions;
- a implementação usará a linha estável suportada mais recente do Spring Boot compatível com Java 21 e fixará a versão no `pom.xml`;
- o Maven Wrapper será versionado e usado nos comandos documentados;
- Spotless e Checkstyle validarão formatação e análise estática;
- o CI executará somente em pull requests e não haverá meta percentual de cobertura na fundação;
- a escolha do provedor de deploy não bloqueia esta change e será decidida antes do deploy de produção.

## Banco

PostgreSQL em container local.

A migration inicial deve criar somente extensões ou estruturas técnicas realmente necessárias. Não criar tabelas de negócio nesta change.

## Arquitetura

ArchUnit deve começar com regras mínimas preparadas para os futuros pacotes:

- `..domain..` não depende de Spring ou JPA;
- `..application..` não depende de adapters;
- `..adapter.in..` não acessa persistência diretamente;
- `..adapter.out..` implementa portas;
- ciclos entre módulos são proibidos.

Regras devem tolerar a ausência inicial desses pacotes.

As convenções mínimas dos pacotes estão documentadas em `docs/architecture/package-conventions.md`. Nenhum módulo de negócio vazio será criado nesta change.

## Configuração

- perfil local separado;
- variáveis de ambiente;
- nenhuma credencial real no repositório;
- exemplos em `.env.example`;
- falha explícita quando configuração obrigatória estiver ausente em ambientes não locais.

## Segurança

Nesta change:

- nenhuma autenticação será implementada;
- endpoints de negócio não existirão;
- Actuator deve expor somente `/actuator/health`, sem detalhes internos;
- detalhes internos de erro não devem ser publicados;
- dependências devem ter versões fixadas/controladas.

## Observabilidade

- health check;
- logs estruturáveis;
- correlation ID pode ser adiado até existir tráfego de negócio;
- nenhuma telemetria externa automática.

## Governança de contribuição

As convenções de idioma, branches, Conventional Commits e revisão estão em `docs/workflow/contributing.md`. A integração em `main` exige uma aprovação humana e CI verde.

As verificações locais e de CI estão em `docs/workflow/quality.md`.

As decisões de ferramentas, repositório e CI estão registradas em `docs/decisions/ADR-0001-foundation-tooling-and-repository.md`.

## Testes

- teste de carregamento do contexto;
- teste com Testcontainers/PostgreSQL;
- teste de migration;
- testes ArchUnit;
- teste do health check quando adequado.

## Alternativas rejeitadas

### Criar todos os módulos vazios

Rejeitado porque produziria estrutura especulativa sem comportamento.

### Começar com microserviços

Rejeitado por custo operacional e ausência de necessidade comprovada.

### Usar H2 nos testes

Rejeitado como banco principal de integração porque diferenças em relação ao PostgreSQL podem ocultar erros.

## Questões pendentes

A validação externa do nome OrionTask (domínio, redes sociais e marcas semelhantes) será feita antes de referências públicas definitivas. Ela não bloqueia a fundação técnica.
