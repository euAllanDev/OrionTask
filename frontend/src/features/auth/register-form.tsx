"use client";

import Link from "next/link";
import { useState } from "react";

import { Button } from "@/components/ui/button";

import { register } from "./api";

const unavailable = "Serviço temporariamente indisponível. Tente novamente.";

export function RegisterForm() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [pending, setPending] = useState(false);

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (pending) return;
    if (!email.trim() || password.length < 12) {
      setMessage("Informe e-mail e senha com pelo menos 12 caracteres.");
      return;
    }
    setPending(true);
    setMessage("");
    try {
      const response = await register({ email, password });
      setPassword("");
      if (response.status === 202) {
        setMessage("Cadastro recebido. Entre para continuar.");
      } else if (response.status === 400) {
        setMessage("Não foi possível validar os dados informados.");
      } else {
        setMessage(unavailable);
      }
    } catch {
      setMessage(unavailable);
    } finally {
      setPending(false);
    }
  }

  return (
    <form className="mt-6 space-y-5" onSubmit={submit}>
      <p className="text-sm text-slate-600">
        Use seu e-mail profissional para criar acesso.
      </p>
      <label
        className="block text-sm font-medium text-slate-800"
        htmlFor="register-email"
      >
        E-mail
        <input
          autoComplete="email"
          className="mt-1.5 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-200"
          id="register-email"
          onChange={(event) => setEmail(event.target.value)}
          required
          type="email"
          value={email}
        />
      </label>
      <label
        className="block text-sm font-medium text-slate-800"
        htmlFor="register-password"
      >
        Senha
        <input
          autoComplete="new-password"
          className="mt-1.5 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-200"
          id="register-password"
          minLength={12}
          onChange={(event) => setPassword(event.target.value)}
          required
          type="password"
          value={password}
        />
      </label>
      {message && (
        <p aria-live="polite" className="text-sm text-slate-700" role="status">
          {message}
        </p>
      )}
      <Button className="w-full" disabled={pending} type="submit">
        {pending ? "Enviando..." : "Criar acesso"}
      </Button>
      <p className="text-sm text-slate-600">
        Já possui acesso?{" "}
        <Link className="font-medium text-blue-700 underline" href="/login">
          Entrar
        </Link>
      </p>
    </form>
  );
}
