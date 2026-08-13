# Open Questions: Auditoria de acoes criticas

## Q1 - Acoes cobertas nesta fase

Opcoes:

- A: somente mutacoes organizacionais ja existentes: criacao de organizacao, convite criado ou aceito, revogacao de membership e criacao, edicao ou desativacao de cliente;
- B: A mais eventos de identidade: cadastro de conta, login, logout e rate limit;
- C: todo evento operacional atual e futuras acoes de ticket.

Trade-off: A preserva isolamento organizacional e dados minimos; B cria eventos sem organizacao e aumenta tratamento de dados de seguranca; C amplia escopo e depende de capacidades ainda inexistentes.

Decisao aprovada: A. Eventos de identidade permanecem nos logs operacionais nesta fase e recebem change propria se exigirem auditoria persistida.

## Q2 - Retencao

Opcoes:

- A: definir prazo fixo agora;
- B: persistir sem exclusao ate change de politica de retencao;
- C: adiar auditoria persistida ate politica de retencao da Fase 5.

Trade-off: A exige justificativa e prazo de LGPD agora; B cria dado pessoal potencialmente retido por tempo indefinido; C posterga evidência de seguranca prevista na Fase 1.

Decisao aprovada: auditoria persistida fica adiada. Nenhum evento de auditoria sera persistido ate politica explicita de retencao e exclusao ser aprovada em change propria.

## Q3 - Consulta de auditoria

Opcoes:

- A: persistir eventos sem endpoint nesta fase;
- B: `OWNER` e `ADMIN` consultam listagem paginada da propria organizacao;
- C: somente `OWNER` consulta listagem paginada.

Trade-off: A reduz superficie mas nao entrega visibilidade operacional; B atende gestores e aumenta superficie de autorizacao; C reduz privilegio, mas limita administradores.

Decisao aprovada: B. `OWNER` e `ADMIN` poderao consultar listagem paginada da propria organizacao, com somente IDs, tipo e timestamp e filtro obrigatorio por `organization_id`. `TECHNICIAN` nao consulta auditoria nesta primeira versao.

## Q4 - Tentativas negadas

Opcoes:

- A: registrar somente acoes efetivadas apos commit;
- B: A mais tentativas de mutacao negadas por autorizacao;
- C: B mais erros de validacao e CSRF.

Trade-off: A preserva sinal limpo e minimizacao; B ajuda investigar abuso, mas exige modelar tentativa e evitar enumeracao; C eleva volume e pode registrar dados tecnicos desnecessarios.

Decisao aprovada: A. Tentativas negadas permanecem em logs operacionais minimizados e limitados nesta fase.

## Q5 - Imutabilidade

Opcoes:

- A: aplicacao nao expoe update ou delete e migration nao cria mecanismos adicionais;
- B: A mais permissao PostgreSQL dedicada, trigger ou hash encadeado;
- C: armazenamento externo imutavel.

Trade-off: A e proporcional ao monolito inicial; B adiciona administracao e modelo de ameaca que precisa ser definido; C excede escopo e infraestrutura atual.

Decisao aprovada: A. Aplicacao nao expoe update ou delete para registros de auditoria. Mudancas de privilegios de banco, integridade criptografica ou armazenamento externo exigem change arquitetural propria.

## Q6 - Paginacao e expurgo

Decisao aprovada: offset com `page` iniciado em zero, `size` padrao 50 e maximo 100, ordenacao deterministica por timestamp e ID decrescentes; expurgo diario. Isto mantem contrato simples para volume atual e assegura aplicacao continua do prazo de 12 meses. Cursor e infraestrutura de expurgo mais complexa exigem necessidade concreta futura.
