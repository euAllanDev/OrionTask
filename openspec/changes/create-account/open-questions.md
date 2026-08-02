# Open Questions

As decisoes abaixo afetam seguranca, privacidade, modelo de dados ou comportamento de negocio. Nenhuma implementacao deve iniciar antes da aprovacao humana.

## 1. Relacao entre conta e organizacao

Uma conta criada nesta change deve tambem criar uma organizacao inicial e tornar a pessoa criadora membro administrador, ou a criacao de organizacao ocorrera obrigatoriamente em uma change e fluxo separados?

Trade-off: criar tudo no primeiro cadastro reduz etapas para a primeira empresa, mas mistura escopos do roadmap e acopla a conta a regras ainda nao definidas de organizacao e papeis.

## 2. Elegibilidade do cadastro

O cadastro inicial sera aberto a qualquer pessoa com e-mail valido, restrito a convite ou restrito a provisionamento administrativo?

Trade-off: cadastro aberto reduz friccao, mas exige protecao contra abuso e verificacao de e-mail; convite ou provisionamento reduz exposicao, mas depende de capacidades ainda fora do escopo.

## 3. Identificador e verificacao

O e-mail sera o identificador unico de login? A conta deve permanecer inativa ate confirmar a posse do e-mail?

Trade-off: e-mail verificado reduz contas indevidas e riscos de recuperacao futura, mas requer envio de e-mail, que pertence a uma capacidade posterior.

## 4. Credencial inicial

Qual metodo de credencial sera aceito no cadastro inicial e qual politica minima de senha deve ser aplicada?

Trade-off: senha local permite uma fundacao independente, mas exige definir hashing, requisitos, rate limiting e recuperacao; login externo introduz dependencia e fluxo de privacidade adicionais.

## 5. Dados pessoais e retencao

Quais dados alem de e-mail sao necessarios no cadastro inicial, para qual finalidade e por quanto tempo serao retidos se a conta nao concluir a ativacao?

Trade-off: coletar apenas e-mail minimiza dados, mas pode limitar identificacao operacional; nome adiciona dado pessoal que exige finalidade e politica de retencao.
