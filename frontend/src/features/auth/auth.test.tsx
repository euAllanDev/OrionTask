import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";

const replace = vi.fn();
vi.mock("next/navigation", () => ({ useRouter: () => ({ replace }) }));

import AppPage from "@/app/app/page";
import { AuthProvider } from "./auth-provider";
import { LoginForm } from "./login-form";
import { RegisterForm } from "./register-form";
import { clearCsrfToken } from "@/lib/csrf/token";

function response(status: number, body?: unknown) {
  return new Response(body ? JSON.stringify(body) : null, { status });
}

function renderWithAuth(node: React.ReactNode) {
  return render(<AuthProvider>{node}</AuthProvider>);
}

describe("authentication flows", () => {
  beforeEach(() => {
    clearCsrfToken();
    vi.stubGlobal("fetch", vi.fn());
    replace.mockReset();
  });

  it("bootstraps authenticated app and loads organization discovery", async () => {
    vi.mocked(fetch)
      .mockResolvedValueOnce(
        response(200, { accountId: "id", email: "person@example.com" }),
      )
      .mockResolvedValueOnce(
        response(200, [
          {
            id: "org-1",
            name: "Alfa",
            createdAt: "2026-01-01T00:00:00Z",
            updatedAt: "2026-01-01T00:00:00Z",
            role: "OWNER",
          },
        ]),
      );
    renderWithAuth(<AppPage />);
    expect(screen.getByLabelText("Carregando")).toBeInTheDocument();
    expect(await screen.findByRole("link", { name: /Alfa/ })).toHaveAttribute(
      "href",
      "/organizations/org-1",
    );
  });

  it("redirects unauthenticated app bootstrap to login", async () => {
    vi.mocked(fetch).mockResolvedValueOnce(response(401));
    renderWithAuth(<AppPage />);
    await waitFor(() => expect(replace).toHaveBeenCalledWith("/login"));
    expect(screen.queryByText("Acesso confirmado")).not.toBeInTheDocument();
  });

  it("shows recoverable error for bootstrap API failure", async () => {
    vi.mocked(fetch).mockResolvedValueOnce(response(500));
    renderWithAuth(<AppPage />);
    expect(
      await screen.findByText("Não foi possível verificar sua sessão."),
    ).toBeInTheDocument();
  });

  it("logs in with CSRF then confirms current session", async () => {
    vi.mocked(fetch)
      .mockResolvedValueOnce(response(401))
      .mockResolvedValueOnce(response(200, { token: "csrf" }))
      .mockResolvedValueOnce(response(204))
      .mockResolvedValueOnce(
        response(200, { accountId: "id", email: "person@example.com" }),
      );
    renderWithAuth(<LoginForm />);
    await screen.findByLabelText("E-mail");
    fireEvent.change(screen.getByLabelText("E-mail"), {
      target: { value: "person@example.com" },
    });
    fireEvent.change(screen.getByLabelText("Senha"), {
      target: { value: "long-password" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Entrar" }));
    expect(screen.getByRole("button", { name: "Entrando..." })).toBeDisabled();
    await waitFor(() => expect(replace).toHaveBeenCalledWith("/app"));
    expect(fetch).toHaveBeenNthCalledWith(
      3,
      "/api/v1/sessions",
      expect.objectContaining({
        headers: expect.objectContaining({ "X-CSRF-TOKEN": "csrf" }),
      }),
    );
  });

  it("shows exact invalid credential message", async () => {
    vi.mocked(fetch)
      .mockResolvedValueOnce(response(401))
      .mockResolvedValueOnce(response(200, { token: "csrf" }))
      .mockResolvedValueOnce(response(401));
    renderWithAuth(<LoginForm />);
    await screen.findByLabelText("E-mail");
    fireEvent.change(screen.getByLabelText("E-mail"), {
      target: { value: "person@example.com" },
    });
    fireEvent.change(screen.getByLabelText("Senha"), {
      target: { value: "long-password" },
    });
    fireEvent.submit(
      screen.getByRole("button", { name: "Entrar" }).closest("form")!,
    );
    expect(await screen.findByRole("alert")).toHaveTextContent(
      "E-mail ou senha inválidos.",
    );
  });

  it("registers without CSRF and does not redirect or authenticate", async () => {
    vi.mocked(fetch)
      .mockResolvedValueOnce(response(401))
      .mockResolvedValueOnce(response(202));
    renderWithAuth(<RegisterForm />);
    await screen.findByLabelText("E-mail");
    fireEvent.change(screen.getByLabelText("E-mail"), {
      target: { value: "person@example.com" },
    });
    fireEvent.change(screen.getByLabelText("Senha"), {
      target: { value: "long-password" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Criar acesso" }));
    expect(
      await screen.findByText("Cadastro recebido. Entre para continuar."),
    ).toBeInTheDocument();
    expect(fetch).toHaveBeenCalledTimes(2);
    expect(replace).not.toHaveBeenCalled();
  });
});
