# Frontend OrionTask

Execute `npm install`, copie `.env.example` para `.env.local` e rode `npm run dev`.

`ORIONTASK_API_ORIGIN` aponta para backend local. Browser chama somente `/api`; Next encaminha localmente. Produção deve servir frontend e backend na mesma origem HTTPS para preservar cookies seguros. `npm run check` executa gates locais.
