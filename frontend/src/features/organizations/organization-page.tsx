"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

import { AppShell } from "@/components/app-shell/app-shell";
import { Button } from "@/components/ui/button";
import { ErrorState } from "@/components/states/error-state";
import { LoadingState } from "@/components/states/loading-state";
import { useAuth } from "@/features/auth/auth-provider";
import { responseError } from "@/lib/api/client";

import { organizations, type Organization } from "./api";

export function OrganizationPage({
  organizationId,
}: {
  organizationId: string;
}) {
  const router = useRouter();
  const { state, setUnauthenticated } = useAuth();
  const [organizationsState, setOrganizationsState] = useState<
    | { status: "loading" }
    | { status: "ready"; organizations: Organization[] }
    | { status: "error" }
  >({ status: "loading" });

  async function retry() {
    setOrganizationsState({ status: "loading" });
    try {
      setOrganizationsState({
        status: "ready",
        organizations: await organizations(),
      });
    } catch (error) {
      if (error instanceof Error && error.message === "401") {
        setUnauthenticated();
        router.replace("/login");
        return;
      }
      setOrganizationsState({ status: "error" });
    }
  }

  useEffect(() => {
    async function loadInitial() {
      try {
        setOrganizationsState({
          status: "ready",
          organizations: await organizations(),
        });
      } catch (error) {
        if (error instanceof Error && error.message === "401") {
          setUnauthenticated();
          router.replace("/login");
          return;
        }
        setOrganizationsState({ status: "error" });
      }
    }
    void loadInitial();
    // Organization discovery happens once when protected route mounts.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (organizationsState.status === "loading") return <LoadingState />;
  if (organizationsState.status === "error")
    return (
      <div className="space-y-4">
        <ErrorState message="Não foi possível carregar suas organizações." />
        <Button onClick={() => void retry()}>Tentar novamente</Button>
      </div>
    );
  const organization = organizationsState.organizations.find(
    (item) => item.id === organizationId,
  );
  if (!organization)
    return (
      <ErrorState
        message={
          responseError(new Response(null, { status: 404 }), true).message
        }
      />
    );
  if (state.status !== "authenticated") return null;
  return (
    <AppShell
      email={state.account.email}
      organization={organization}
      organizations={organizationsState.organizations}
    >
      <div className="mx-auto max-w-5xl">
        <p className="text-sm font-medium text-slate-500">Área de trabalho</p>
        <h1 className="mt-1 text-2xl font-semibold tracking-tight text-slate-950">
          {organization.name}
        </h1>
        <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-600">
          Recursos organizacionais serão adicionados em changes futuras.
        </p>
      </div>
    </AppShell>
  );
}
