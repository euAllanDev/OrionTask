"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/states/empty-state";
import { ErrorState } from "@/components/states/error-state";
import { LoadingState } from "@/components/states/loading-state";
import { useAuth } from "@/features/auth/auth-provider";

import { organizations, type Organization } from "./api";

type ListState =
  | { status: "loading" }
  | { status: "empty" }
  | { status: "ready"; organizations: Organization[] }
  | { status: "error" };

export function OrganizationList() {
  const router = useRouter();
  const { setUnauthenticated } = useAuth();
  const [state, setState] = useState<ListState>({ status: "loading" });

  async function retry() {
    setState({ status: "loading" });
    try {
      const items = await organizations();
      setState(
        items.length === 0
          ? { status: "empty" }
          : { status: "ready", organizations: items },
      );
    } catch (error) {
      if (error instanceof Error && error.message === "401") {
        setUnauthenticated();
        router.replace("/login");
        return;
      }
      setState({ status: "error" });
    }
  }

  useEffect(() => {
    async function loadInitial() {
      try {
        const items = await organizations();
        setState(
          items.length === 0
            ? { status: "empty" }
            : { status: "ready", organizations: items },
        );
      } catch (error) {
        if (error instanceof Error && error.message === "401") {
          setUnauthenticated();
          router.replace("/login");
          return;
        }
        setState({ status: "error" });
      }
    }
    void loadInitial();
    // Organization discovery happens once when protected page mounts.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (state.status === "loading") return <LoadingState />;
  if (state.status === "empty")
    return (
      <EmptyState
        title="Nenhuma organização acessível"
        description="Você ainda não possui acesso a uma organização."
      />
    );
  if (state.status === "error")
    return (
      <div className="space-y-4">
        <ErrorState message="Não foi possível carregar suas organizações." />
        <Button onClick={() => void retry()}>Tentar novamente</Button>
      </div>
    );
  return (
    <ul
      aria-label="Organizações acessíveis"
      className="grid gap-3 sm:grid-cols-2"
    >
      {state.organizations.map((organization) => (
        <li key={organization.id}>
          <Link
            className="block rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition-colors hover:border-blue-300 hover:bg-blue-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600"
            href={`/organizations/${organization.id}`}
          >
            <p className="font-semibold text-slate-950">{organization.name}</p>
            <p className="mt-1 text-sm text-slate-600">
              {roleLabel(organization.role)}
            </p>
          </Link>
        </li>
      ))}
    </ul>
  );
}

function roleLabel(role: Organization["role"]) {
  return {
    OWNER: "Proprietário",
    ADMIN: "Administrador",
    TECHNICIAN: "Técnico",
  }[role];
}
