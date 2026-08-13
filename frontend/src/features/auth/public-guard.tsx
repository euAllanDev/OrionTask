"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";

import { LoadingState } from "@/components/states/loading-state";

import { useAuth } from "./auth-provider";

export function PublicGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const { state } = useAuth();

  useEffect(() => {
    if (state.status === "authenticated") router.replace("/app");
  }, [router, state.status]);

  if (state.status === "loading")
    return (
      <main className="mx-auto max-w-md px-6 py-12">
        <LoadingState />
      </main>
    );
  if (state.status === "authenticated") return null;
  return <>{children}</>;
}
