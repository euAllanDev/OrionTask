# Open Questions: Gestao de clientes

## Q1 - Dados coletados no cadastro inicial

Quais campos entram nesta capacidade?

Opcoes:

- somente `name` obrigatorio, de 1 a 120 caracteres apos trim;
- `name` obrigatorio e `email`/`phone` opcionais;
- pessoa juridica e pessoa fisica com documento, endereco e contatos.

Recomendacao: somente `name`. Tickets futuros precisam de cliente identificado; contatos, documentos e endereco aumentam dados pessoais e exigem finalidade, retencao e controles adicionais.

## Q2 - Autoridade por papel

Quais memberships gerenciam clientes?

Opcoes:

- `OWNER` e `ADMIN` criam, editam e desativam; `TECHNICIAN` somente consulta;
- tres papeis criam e editam; apenas `OWNER` e `ADMIN` desativam;
- tres papeis gerenciam integralmente.

Recomendacao: primeira opcao. Mantem menor privilegio ate necessidade operacional concreta para tecnicos alterarem cadastros.

## Q3 - Desativacao e reativacao

Qual lifecycle e efeito futuro?

Opcoes:

- status `ACTIVE`/`INACTIVE`; desativacao preserva registro e reativacao fica fora desta change;
- status `ACTIVE`/`INACTIVE`; administradores tambem podem reativar;
- exclusao fisica.

Recomendacao: primeira opcao. Preserva historico de tickets futuro sem ampliar superficie funcional.

## Q4 - Unicidade

Nome de cliente deve ser unico dentro da organizacao?

Opcoes:

- nao impor unicidade;
- unicidade exata de nome normalizado por organizacao;
- unicidade por e-mail ou documento.

Recomendacao: nao impor unicidade. Nomes semelhantes ou iguais podem representar unidades, pessoas ou registros legitimamente distintos; campos identificadores nao pertencem a esta change.

## Q5 - Listagem inicial

Listagem deve incluir clientes inativos?

Opcoes:

- retornar somente `ACTIVE` por padrao, com filtro explicito para `INACTIVE` e todos;
- retornar ativos e inativos sempre;
- nao implementar listagem nesta change.

Recomendacao: primeira opcao. Mantem operacao diaria simples e preserva acesso administrativo ao historico.
