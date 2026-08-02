# Tasks: Criação de conta interna

## 1. Revisão e aprovação

- [x] Revisar proposal, design, delta spec e decisões de segurança
- [x] Aprovar a change para implementação

## 2. Domínio e aplicação

- [x] Criar modelo de domínio de conta sem dependências de framework
- [x] Criar caso de uso e portas para registro de conta
- [x] Normalizar e validar e-mail como identificador único
- [x] Validar senha entre 12 e 128 caracteres
- [x] Criar testes unitários do domínio e da aplicação

## 3. Persistência e segurança

- [x] Criar migration versionada para contas e unicidade do e-mail normalizado
- [x] Implementar adapter JPA separado do domínio
- [x] Configurar hash Argon2id sem persistir ou registrar senha em texto puro
- [x] Implementar rate limit por IP e e-mail normalizado
- [x] Registrar auditoria mínima sem dados sensíveis
- [x] Criar testes de integração com PostgreSQL e Testcontainers

## 4. API e verificação

- [x] Criar endpoint de cadastro e validação de entrada
- [x] Garantir resposta indistinguível para conta existente
- [x] Cobrir abuso, enumeração, mass assignment e ausência de senha em logs
- [x] Executar `backend/mvnw.cmd verify`
- [x] Revisar aderência entre implementação e delta spec
