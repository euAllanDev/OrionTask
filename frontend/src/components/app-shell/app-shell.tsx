"use client";

import { Menu, X } from "lucide-react";
import { AnimatePresence, motion, useReducedMotion } from "motion/react";
import { useState } from "react";

import { Button } from "@/components/ui/button";

const navigation = ["Visão geral", "Clientes", "Membros"];

export function AppShell({
  organizationId,
  children,
}: {
  organizationId: string;
  children: React.ReactNode;
}) {
  const [menuOpen, setMenuOpen] = useState(false);
  const reducedMotion = useReducedMotion();

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
            Organização {organizationId}
          </p>
        </div>
        <p className="text-sm font-semibold tracking-[0.14em] text-blue-700">
          ORIONTASK
        </p>
      </header>
      <div className="flex">
        <aside className="hidden w-60 shrink-0 border-r border-slate-200 bg-white p-4 lg:block">
          <Navigation />
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
              <Navigation />
            </motion.aside>
          )}
        </AnimatePresence>
        <main className="min-w-0 flex-1 p-4 sm:p-6 lg:p-8">{children}</main>
      </div>
    </div>
  );
}

function Navigation() {
  return (
    <nav aria-label="Navegação da organização">
      <ul className="space-y-1">
        {navigation.map((item, index) => (
          <li
            className={
              index === 0
                ? "rounded-md bg-blue-50 px-3 py-2 text-sm font-medium text-blue-800"
                : "px-3 py-2 text-sm text-slate-600"
            }
            key={item}
          >
            {item}
          </li>
        ))}
      </ul>
    </nav>
  );
}
