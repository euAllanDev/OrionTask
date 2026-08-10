import { AppShell } from "@/components/app-shell/app-shell";
import { EmptyState } from "@/components/states/empty-state";

export default async function OrganizationPage({
  params,
}: {
  params: Promise<{ organizationId: string }>;
}) {
  const { organizationId } = await params;
  return (
    <AppShell organizationId={organizationId}>
      <div className="mx-auto max-w-5xl">
        <p className="text-sm font-medium text-slate-500">Área de trabalho</p>
        <h1 className="mt-1 text-2xl font-semibold tracking-tight text-slate-950">
          Fundação da organização
        </h1>
        <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-600">
          Este shell estabelece navegação e contexto por URL. Recursos
          organizacionais serão adicionados em changes futuras.
        </p>
        <div className="mt-8">
          <EmptyState
            title="Nenhum recurso disponível"
            description="Clientes, membros e tickets serão exibidos aqui quando suas interfaces forem implementadas."
          />
        </div>
      </div>
    </AppShell>
  );
}
