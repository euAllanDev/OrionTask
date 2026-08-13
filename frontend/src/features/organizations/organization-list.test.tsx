import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";

const { replace, setUnauthenticated, organizations } = vi.hoisted(() => ({
  replace: vi.fn(),
  setUnauthenticated: vi.fn(),
  organizations: vi.fn(),
}));

vi.mock("next/navigation", () => ({ useRouter: () => ({ replace }) }));
vi.mock("@/features/auth/auth-provider", () => ({
  useAuth: () => ({ setUnauthenticated }),
}));
vi.mock("./api", () => ({ organizations }));

import { OrganizationList } from "./organization-list";

const items = [
  {
    id: "org-123",
    name: "Alfa",
    createdAt: "2026-01-01T00:00:00Z",
    updatedAt: "2026-01-01T00:00:00Z",
    role: "OWNER" as const,
  },
];

describe("OrganizationList", () => {
  beforeEach(() => {
    organizations.mockReset();
    replace.mockReset();
    setUnauthenticated.mockReset();
  });

  it("shows accessible loading then organizations with URL links", async () => {
    organizations.mockResolvedValue(items);
    render(<OrganizationList />);
    expect(screen.getByLabelText("Carregando")).toBeInTheDocument();
    expect(await screen.findByRole("link", { name: /Alfa/ })).toHaveAttribute(
      "href",
      "/organizations/org-123",
    );
  });

  it("shows empty state without invented organization", async () => {
    organizations.mockResolvedValue([]);
    render(<OrganizationList />);
    expect(
      await screen.findByText("Nenhuma organização acessível"),
    ).toBeInTheDocument();
  });

  it("retries generic loading failure", async () => {
    organizations
      .mockRejectedValueOnce(new Error("500"))
      .mockResolvedValueOnce(items);
    render(<OrganizationList />);
    expect(
      await screen.findByText("Não foi possível carregar suas organizações."),
    ).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: "Tentar novamente" }));
    expect(
      await screen.findByRole("link", { name: /Alfa/ }),
    ).toBeInTheDocument();
  });

  it("redirects to login for a lost session", async () => {
    organizations.mockRejectedValue(new Error("401"));
    render(<OrganizationList />);
    await waitFor(() => expect(setUnauthenticated).toHaveBeenCalled());
    expect(replace).toHaveBeenCalledWith("/login");
  });
});
