import { apiFetch } from "@/lib/api/client";

export type Account = { accountId: string; email: string };

type Credentials = { email: string; password: string };

export async function currentSession(): Promise<Response> {
  return apiFetch("/api/v1/session", { csrf: false });
}

export async function login(credentials: Credentials): Promise<Response> {
  return apiFetch("/api/v1/sessions", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(credentials),
  });
}

export async function register(credentials: Credentials): Promise<Response> {
  return apiFetch("/api/v1/accounts", {
    method: "POST",
    csrf: false,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(credentials),
  });
}

export async function logout(): Promise<Response> {
  return apiFetch("/api/v1/session", { method: "DELETE" });
}
