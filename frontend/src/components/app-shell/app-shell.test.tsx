import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";

const replace = vi.fn();
vi.mock("next/navigation", () => ({ useRouter: () => ({ replace }) }));
const { logout, setUnauthenticated } = vi.hoisted(() => ({
  logout: vi.fn(),
  setUnauthenticated: vi.fn(),
}));
vi.mock("@/features/auth/api", () => ({ logout }));
vi.mock("@/features/auth/auth-provider", () => ({
  useAuth: () => ({ setUnauthenticated }),
}));
vi.mock("motion/react", async () => {
  const actual =
    await vi.importActual<typeof import("motion/react")>("motion/react");
  return { ...actual, useReducedMotion: () => true };
});

import { AppShell } from "@/components/app-shell/app-shell";

const organizations = [
  {
    id: "org-123",
    name: "Alfa",
    createdAt: "2026-01-01T00:00:00Z",
    updatedAt: "2026-01-01T00:00:00Z",
    role: "OWNER" as const,
  },
  {
    id: "org-456",
    name: "Beta",
    createdAt: "2026-01-01T00:00:00Z",
    updatedAt: "2026-01-01T00:00:00Z",
    role: "TECHNICIAN" as const,
  },
];

describe("AppShell", () => {
  it("renders URL organization context and opens mobile navigation", () => {
    render(
      <AppShell
        email="person@example.com"
        organization={organizations[0]}
        organizations={organizations}
      >
        Conteúdo estrutural
      </AppShell>,
    );
    expect(screen.getByRole("link", { name: "Alfa" })).toBeInTheDocument();
    expect(screen.getByText("Conteúdo estrutural")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Alfa" })).toHaveAttribute(
      "href",
      "/organizations/org-123",
    );
    const toggle = screen.getByRole("button", { name: "Alternar navegação" });
    fireEvent.click(toggle);
    expect(toggle).toHaveAttribute("aria-expanded", "true");
    expect(screen.getAllByText("Beta")).toHaveLength(2);
  });

  it("logs out from organization shell", async () => {
    logout.mockResolvedValue(new Response(null, { status: 204 }));
    render(
      <AppShell
        email="person@example.com"
        organization={organizations[0]}
        organizations={organizations}
      >
        Conteúdo
      </AppShell>,
    );
    fireEvent.click(screen.getByRole("button", { name: "Sair" }));
    await vi.waitFor(() => expect(setUnauthenticated).toHaveBeenCalled());
    expect(replace).toHaveBeenCalledWith("/login");
  });
});
