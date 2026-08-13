# Open Questions: Definir retencao de auditoria

## Mapa de dados proposto

Eventos futuros conterao somente `organizationId`, `actorAccountId`, identificador tecnico do recurso afetado quando aplicavel, tipo de evento e timestamp UTC. Embora nao contenham nome ou e-mail, `actorAccountId` e `organizationId` permitem vinculo indireto a pessoa e devem ser tratados como dados pseudonimizados, nao anonimos.

Finalidade proposta: seguranca, responsabilidade operacional e investigacao de incidentes da organizacao controladora no OrionTask.

## Q1 - Prazo de retencao no banco primario

Opcoes:

- A: 90 dias;
- B: 12 meses;
- C: 24 meses;
- D: prazo definido pelo controlador por organizacao.

Trade-off: 90 dias maximiza minimizacao, mas pode ser insuficiente para incidentes tardios; 12 ou 24 meses ampliam capacidade investigativa e retencao de dados; configuracao por organizacao aumenta complexidade, superficie de erro e precisa contrato de controlador.

Decisao aprovada: 12 meses a partir da ocorrencia. Finalidade: seguranca, responsabilidade operacional e investigacao de incidentes, limitada aos eventos aprovados de auditoria organizacional.

## Q2 - Acao ao fim do prazo

Opcoes:

- A: exclusao fisica automatizada;
- B: anonimizar `actorAccountId` e manter demais metadados;
- C: manter somente agregados anonimos;
- D: exclusao manual pelo operador.

Trade-off: A cumpre minimizacao e simplifica consulta; B e C preservam algum valor operacional, mas precisam provar irreversibilidade; D cria risco operacional e nao garante prazo.

Decisao aprovada: A. Eventos serao excluidos fisicamente ao fim de 12 meses. Excecao de conservacao exige fundamento legal ou regulatorio especifico e change propria que documente esse fundamento.

## Q3 - Backups e restauracao

Opcoes:

- A: backups seguem prazo proprio de 30 dias; registros vencidos podem reaparecer somente ate expurgo automatico posterior;
- B: backups seguem o mesmo prazo do banco primario;
- C: nao definir backups nesta change.

Trade-off: A e operacionalmente simples, mas exige documentar janela residual; B reduz janela, mas pode elevar custo e complexidade; C deixa exclusao incompleta.

Decisao aprovada: backups terao retencao maxima de 90 dias. Apos restauracao, rotina de expurgo deve ser reaplicada antes de exposicao normal do sistema, eliminando eventos cujo prazo de 12 meses ja venceu.

## Q4 - Exclusao futura de organizacao

Opcoes:

- A: eventos seguem prazo normal mesmo apos exclusao da organizacao;
- B: eventos sao excluidos junto com a organizacao;
- C: exclusao de organizacao fica bloqueada ate politica especifica decidir esse efeito.

Trade-off: A preserva evidencias, mas retém vinculo com organizacao encerrada; B maximiza exclusao, mas elimina trilha de responsabilidade; C evita assumir regra antes de existir capacidade de exclusao.

Decisao aprovada: A. Exclusao futura de organizacao nao altera prazo original: eventos existentes permanecem somente pelo periodo residual ate completar 12 meses e depois sao excluidos fisicamente, salvo fundamento especifico de conservacao.

## Q5 - Responsabilidades e aprovacao juridica

Opcoes:

- A: responsavel do produto aprova finalidade e prazo nesta change;
- B: controlador ou assessoria juridica valida antes de implementacao;
- C: adiar a definicao ate Central de Confianca.

Trade-off: A permite avancar rapidamente, mas pode ser insuficiente para decisao LGPD; B aumenta evidencia de conformidade; C posterga auditoria de seguranca.

Decisao aprovada: validacao formal de controlador e orientacao juridica e obrigatoria antes da exposicao publica do OrionTask. Ela deve cobrir finalidade, categorias registradas, prazo, base aplicavel, exclusao e tratamento de backups.
