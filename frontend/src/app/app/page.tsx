"use client";

import { AppGuard } from "@/features/auth/app-guard";
import { OrganizationList } from "@/features/organizations/organization-list";

export default function AppPage() {
  return (
    <AppGuard>
      {() => (
        <main className="mx-auto min-h-screen w-full max-w-5xl px-6 py-12">
          <p className="text-sm font-semibold tracking-[0.18em] text-blue-700">
            ORIONTASK
          </p>
          <h1 className="mt-8 text-3xl font-semibold tracking-tight text-slate-950">
            Suas organizações
          </h1>
          <p className="mt-2 text-slate-600">
            Escolha uma organização para continuar.
          </p>
          <div className="mt-8">
            <OrganizationList />
          </div>
        </main>
      )}
    </AppGuard>
  );
}
