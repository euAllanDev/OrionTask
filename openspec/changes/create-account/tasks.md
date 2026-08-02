# Tasks: Criação de conta interna

## 1. Revisão e aprovação

- [ ] Revisar proposal, design, delta spec e decisões de segurança
- [ ] Aprovar a change para implementação

## 2. Domínio e aplicação

- [ ] Criar modelo de domínio de conta sem dependências de framework
- [ ] Criar caso de uso e portas para registro de conta
- [ ] Normalizar e validar e-mail como identificador único
- [ ] Validar senha entre 12 e 128 caracteres
- [ ] Criar testes unitários do domínio e da aplicação

## 3. Persistência e segurança

- [ ] Criar migration versionada para contas e unicidade do e-mail normalizado
- [ ] Implementar adapter JPA separado do domínio
- [ ] Configurar hash Argon2id sem persistir ou registrar senha em texto puro
- [ ] Implementar rate limit por IP e e-mail normalizado
- [ ] Registrar auditoria mínima sem dados sensíveis
- [ ] Criar testes de integração com PostgreSQL e Testcontainers

## 4. API e verificação

- [ ] Criar endpoint de cadastro e validação de entrada
- [ ] Garantir resposta indistinguível para conta existente
- [ ] Cobrir abuso, enumeração, mass assignment e ausência de senha em logs
- [ ] Executar `backend/mvnw.cmd verify`
- [ ] Revisar aderência entre implementação e delta spec
