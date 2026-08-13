"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

import { Button } from "@/components/ui/button";
import { clearCsrfToken } from "@/lib/csrf/token";

import { currentSession, login } from "./api";
import { useAuth } from "./auth-provider";

const unavailable = "Serviço temporariamente indisponível. Tente novamente.";

export function LoginForm() {
  const router = useRouter();
  const { setAuthenticated, setUnauthenticated } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [pending, setPending] = useState(false);

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (pending) return;
    if (!email.trim() || !password) {
      setError("Informe e-mail e senha.");
      return;
    }
    setPending(true);
    setError("");
    try {
      const response = await login({ email, password });
      setPassword("");
      if (response.status === 401) {
        setError("E-mail ou senha inválidos.");
        return;
      }
      if (response.status !== 204) {
        setError(unavailable);
        return;
      }
      clearCsrfToken();
      const session = await currentSession();
      if (!session.ok) {
        if (session.status === 401) setUnauthenticated();
        setError(unavailable);
        return;
      }
      const account = (await session.json()) as {
        accountId: string;
        email: string;
      };
      setAuthenticated(account);
      router.replace("/app");
    } catch {
      setError(unavailable);
    } finally {
      setPending(false);
    }
  }

  return (
    <form className="mt-6 space-y-5" onSubmit={submit}>
      <p className="text-sm text-slate-600">
        Entre para continuar no OrionTask.
      </p>
      <label
        className="block text-sm font-medium text-slate-800"
        htmlFor="login-email"
      >
        E-mail
        <input
          autoComplete="email"
          className="mt-1.5 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-200"
          id="login-email"
          onChange={(event) => setEmail(event.target.value)}
          required
          type="email"
          value={email}
        />
      </label>
      <label
        className="block text-sm font-medium text-slate-800"
        htmlFor="login-password"
      >
        Senha
        <input
          autoComplete="current-password"
          className="mt-1.5 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-200"
          id="login-password"
          onChange={(event) => setPassword(event.target.value)}
          minLength={12}
          required
          type="password"
          value={password}
        />
      </label>
      {error && (
        <p aria-live="polite" className="text-sm text-red-700" role="alert">
          {error}
        </p>
      )}
      <Button className="w-full" disabled={pending} type="submit">
        {pending ? "Entrando..." : "Entrar"}
      </Button>
    </form>
  );
}
