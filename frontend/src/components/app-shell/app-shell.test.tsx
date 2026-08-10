import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";

vi.mock("motion/react", async () => {
  const actual =
    await vi.importActual<typeof import("motion/react")>("motion/react");
  return { ...actual, useReducedMotion: () => true };
});

import { AppShell } from "@/components/app-shell/app-shell";

describe("AppShell", () => {
  it("renders organization context and opens mobile navigation with reduced motion", () => {
    render(<AppShell organizationId="org-123">Conteúdo estrutural</AppShell>);
    expect(screen.getByText("Organização org-123")).toBeInTheDocument();
    expect(screen.getByText("Conteúdo estrutural")).toBeInTheDocument();
    const toggle = screen.getByRole("button", { name: "Alternar navegação" });
    fireEvent.click(toggle);
    expect(toggle).toHaveAttribute("aria-expanded", "true");
    expect(screen.getAllByText("Visão geral")).toHaveLength(2);
  });
});
