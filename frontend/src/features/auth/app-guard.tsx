"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";

import { Button } from "@/components/ui/button";
import { LoadingState } from "@/components/states/loading-state";

import { useAuth } from "./auth-provider";

export function AppGuard({
  children,
}: {
  children: (email: string) => React.ReactNode;
}) {
  const router = useRouter();
  const { state, bootstrapError, refreshSession } = useAuth();

  useEffect(() => {
    if (state.status === "unauthenticated" && !bootstrapError)
      router.replace("/login");
  }, [bootstrapError, router, state.status]);

  if (state.status === "loading")
    return (
      <main className="mx-auto max-w-3xl px-6 py-12">
        <LoadingState />
      </main>
    );
  if (bootstrapError)
    return (
      <main className="mx-auto max-w-3xl px-6 py-12">
        <section className="rounded-lg border border-red-200 bg-red-50 p-6">
          <p role="alert">Não foi possível verificar sua sessão.</p>
          <Button className="mt-4" onClick={() => void refreshSession()}>
            Tentar novamente
          </Button>
        </section>
      </main>
    );
  if (state.status === "unauthenticated") return null;
  return <>{children(state.account.email)}</>;
}
