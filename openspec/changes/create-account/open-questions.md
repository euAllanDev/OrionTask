# Perguntas abertas da criação de conta

As decisões abaixo afetam segurança, privacidade, modelo de dados ou comportamento de negócio. Nenhuma implementação deve iniciar antes da aprovação humana das questões ainda abertas.

## Decisões ainda abertas

### 1. Relação entre conta e organização

Uma conta criada nesta change também cria automaticamente uma organização inicial e uma membership administrativa, ou conta, organização e membership serão criadas em fluxos separados?

Trade-off: criar tudo no primeiro cadastro simplifica o onboarding da primeira empresa, mas mistura domínios e antecipa regras de organização e papéis. Separar os fluxos preserva as fronteiras do roadmap, mas exige definir como a primeira conta alcança uma organização.

### 2. Elegibilidade do cadastro

O cadastro inicial será aberto, por convite ou por provisionamento administrativo?

Trade-off: cadastro aberto reduz fricção, mas, sem confirmação de e-mail nesta etapa, exige proteções adicionais contra abuso. Convite ou provisionamento reduz a exposição, mas depende de capacidades ainda fora do escopo.

### 3. Identificador de login

O e-mail será o identificador único de login?

### 4. Credencial inicial

O cadastro inicial aceitará senha local ou um provedor externo de identidade?

Trade-off: senha local mantém a fundação independente, mas exige hashing seguro, política mínima de senha, limitação de tentativas e respostas que não revelem a existência de conta. Provedor externo introduz dependência, tratamento adicional de dados e novos fluxos de privacidade.

### 5. Dados pessoais e minimização

Quais dados além do e-mail são realmente necessários para criar e operar a conta inicial, qual é a finalidade de cada campo e qual será a política de exclusão de contas sem uso?

Direção inicial recomendada: e-mail obrigatório; nome opcional ou obrigatório somente quando houver finalidade operacional aprovada; não coletar telefone, CPF, endereço ou outros dados nesta primeira change.

## Decisões adiadas

- A verificação e a confirmação da posse do e-mail permanecem planejadas para uma change futura de identidade e comunicação, mas não farão parte desta primeira change.
- Tokens de ativação.
- Reenvio de confirmação.
- Expiração de ativação.
- Integração com serviço de e-mail.
- Políticas de conta pendente por falta de confirmação.
- Recuperação de senha.

A primeira versão não deve criar um estado de ativação que dependa de um e-mail que o sistema ainda não consegue enviar. Não devem ser introduzidos estados como `PENDING_EMAIL_VERIFICATION`, `UNVERIFIED` ou `AWAITING_ACTIVATION` sem outra justificativa funcional aprovada.

## Requisitos e decisões de segurança

Mesmo sem confirmação de e-mail, a change deve considerar:

- normalização de e-mail;
- unicidade case-insensitive;
- prevenção de enumeração de contas;
- hashing seguro de senha e proibição de armazenamento em texto puro;
- política mínima de senha, se senha local for escolhida;
- validação de entrada e proteção contra mass assignment;
- rate limiting ou estratégia equivalente para cadastro e, quando implementado, login;
- auditoria mínima de eventos de segurança;
- proteção de dados e proibição de senhas nos logs;
- sessões revogáveis quando o login for implementado;
- recuperação de senha futura fora do escopo desta change.

## Fora do escopo desta change

- confirmação de e-mail;
- envio de e-mail;
- recuperação de senha;
- alteração de e-mail com reconfirmação;
- MFA;
- login social;
- login sem senha;
- convites por e-mail;
- notificações;
- portal do cliente.
