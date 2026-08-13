"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { LogOut, Menu, X } from "lucide-react";
import { AnimatePresence, motion, useReducedMotion } from "motion/react";
import { useState } from "react";

import { Button } from "@/components/ui/button";
import { logout } from "@/features/auth/api";
import { useAuth } from "@/features/auth/auth-provider";
import type { Organization } from "@/features/organizations/api";

export function AppShell({
  organization,
  organizations,
  email,
  children,
}: {
  organization: Organization;
  organizations: Organization[];
  email: string;
  children: React.ReactNode;
}) {
  const router = useRouter();
  const { setUnauthenticated } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);
  const [pending, setPending] = useState(false);
  const [logoutError, setLogoutError] = useState(false);
  const reducedMotion = useReducedMotion();

  async function handleLogout() {
    if (pending) return;
    setPending(true);
    setLogoutError(false);
    try {
      const response = await logout();
      if (response.status === 204 || response.status === 401) {
        setUnauthenticated();
        router.replace("/login");
      } else {
        setLogoutError(true);
      }
    } catch {
      setLogoutError(true);
    } finally {
      setPending(false);
    }
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-950">
      <header className="flex h-16 items-center justify-between border-b border-slate-200 bg-white px-4 lg:px-6">
        <div className="flex min-w-0 items-center gap-3">
          <Button
            aria-expanded={menuOpen}
            aria-label="Alternar navegação"
            className="lg:hidden"
            onClick={() => setMenuOpen((open) => !open)}
            size="icon"
            variant="ghost"
          >
            {menuOpen ? <X /> : <Menu />}
          </Button>
          <p className="truncate text-sm font-medium text-slate-700">
            {organization.name}
          </p>
        </div>
        <div className="flex items-center gap-3">
          <p className="hidden text-sm text-slate-500 sm:block">{email}</p>
          <Button
            aria-label="Sair"
            disabled={pending}
            onClick={() => void handleLogout()}
            size="icon"
            variant="ghost"
          >
            <LogOut />
          </Button>
          {logoutError && (
            <p className="sr-only" role="alert">
              Não foi possível encerrar sua sessão. Tente novamente.
            </p>
          )}
        </div>
      </header>
      <div className="flex">
        <aside className="hidden w-64 shrink-0 border-r border-slate-200 bg-white p-4 lg:block">
          <Navigation
            currentId={organization.id}
            organizations={organizations}
          />
        </aside>
        <AnimatePresence initial={false}>
          {menuOpen && (
            <motion.aside
              animate={{ opacity: 1, x: 0 }}
              className="fixed inset-x-0 top-16 z-10 border-b border-slate-200 bg-white p-4 shadow-lg lg:hidden"
              exit={{ opacity: 0, x: -12 }}
              initial={reducedMotion ? false : { opacity: 0, x: -12 }}
              transition={{ duration: reducedMotion ? 0 : 0.16 }}
            >
              <Navigation
                currentId={organization.id}
                organizations={organizations}
                onNavigate={() => setMenuOpen(false)}
              />
            </motion.aside>
          )}
        </AnimatePresence>
        <main className="min-w-0 flex-1 p-4 sm:p-6 lg:p-8">{children}</main>
      </div>
    </div>
  );
}

function Navigation({
  currentId,
  organizations,
  onNavigate,
}: {
  currentId: string;
  organizations: Organization[];
  onNavigate?: () => void;
}) {
  return (
    <nav aria-label="Organizações">
      <p className="px-3 pb-2 text-xs font-semibold tracking-wider text-slate-500">
        ORGANIZAÇÕES
      </p>
      <ul className="space-y-1">
        {organizations.map((organization) => (
          <li key={organization.id}>
            <Link
              className={
                organization.id === currentId
                  ? "block rounded-md bg-blue-50 px-3 py-2 text-sm font-medium text-blue-800"
                  : "block rounded-md px-3 py-2 text-sm text-slate-600 hover:bg-slate-100"
              }
              href={`/organizations/${organization.id}`}
              onClick={onNavigate}
            >
              {organization.name}
            </Link>
          </li>
        ))}
      </ul>
    </nav>
  );
}
