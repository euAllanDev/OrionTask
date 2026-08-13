# Decisões e perguntas da criação de conta

As decisões abaixo afetam segurança, privacidade, modelo de dados ou comportamento de negócio. Nenhuma implementação deve iniciar antes da aprovação humana dos artefatos desta change.

## Decisões de produto e dados resolvidas

- Relação entre conta, organização e membership: serão criadas em fluxos separados. Esta change não criará organização nem membership.
- Elegibilidade: o cadastro será aberto, com proteções contra abuso compatíveis com esse modelo.
- Identificador: o e-mail será o identificador único de login, normalizado e único sem distinção entre maiúsculas e minúsculas.
- Credencial: a primeira versão usará senha local.
- Dados pessoais: a primeira versão coletará somente o e-mail; não coletará nome, telefone, CPF, endereço ou outros dados.

## Decisões de segurança resolvidas

- Política de senha: aceitar de 12 a 128 caracteres, sem regras obrigatórias de composição.
- Hashing: usar Argon2id com parâmetros definidos e testáveis na implementação.
- Proteção contra abuso: limitar cadastro aberto a cinco tentativas por endereço IP e três por e-mail normalizado em uma janela de quinze minutos.
- E-mail já cadastrado: devolver resposta indistinguível do cadastro novo, sem criar uma segunda conta.

Não há questões bloqueantes em aberto nesta change. Os artefatos seguem para revisão humana antes da implementação.

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
