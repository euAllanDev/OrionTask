# Convencoes de Contribuicao

## Idioma

- codigo, nomes de pacotes, classes, metodos, variaveis, endpoints e mensagens de commit usam ingles;
- documentacao interna, specs OpenSpec e comunicacao do produto usam pt-BR, salvo uma necessidade explicita de outro idioma.

## Branches

- `main` e a branch de integracao protegida;
- cada change usa uma branch curta baseada em `main`;
- use `feat/<change-id>` para implementacoes e `docs/<change-id>` para mudancas exclusivamente documentais;
- correcoes usam `fix/<descricao-curta>`;
- branches devem ser removidas depois da integracao.

## Commits

Os commits seguem Conventional Commits e possuem escopo quando ele trouxer clareza.

```text
feat(ticket): create ticket command
fix(identity): revoke expired session
docs(architecture): define package boundaries
chore(ci): run Maven verification
```

Tipos usuais: `feat`, `fix`, `docs`, `test`, `refactor`, `build`, `ci` e `chore`.

## Integracao

Uma mudanca so pode ser integrada em `main` quando:

- houver uma aprovacao humana;
- o CI estiver verde;
- a change OpenSpec correspondente estiver aprovada quando a mudanca for funcional, arquitetural, de seguranca, privacidade ou modelo de dados;
- a implementacao respeitar o escopo aprovado.
