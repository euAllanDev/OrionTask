"use client";

import { createContext, useContext, useEffect, useState } from "react";

import { clearCsrfToken } from "@/lib/csrf/token";

import { currentSession, type Account } from "./api";

export type AuthState =
  | { status: "loading" }
  | { status: "authenticated"; account: Account }
  | { status: "unauthenticated" };

type AuthContextValue = {
  state: AuthState;
  bootstrapError: boolean;
  refreshSession: () => Promise<AuthState>;
  setAuthenticated: (account: Account) => void;
  setUnauthenticated: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function isAccount(value: unknown): value is Account {
  return (
    typeof value === "object" &&
    value !== null &&
    typeof (value as Account).accountId === "string" &&
    typeof (value as Account).email === "string"
  );
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<AuthState>({ status: "loading" });
  const [bootstrapError, setBootstrapError] = useState(false);

  async function refreshSession(): Promise<AuthState> {
    setState({ status: "loading" });
    setBootstrapError(false);
    try {
      const response = await currentSession();
      if (response.status === 401) {
        clearCsrfToken();
        const next = { status: "unauthenticated" } as const;
        setState(next);
        return next;
      }
      if (!response.ok) throw new Error("Current session failed");
      const account: unknown = await response.json();
      if (!isAccount(account)) throw new Error("Invalid current session");
      const next = { status: "authenticated", account } as const;
      setState(next);
      return next;
    } catch {
      setBootstrapError(true);
      const next = { status: "unauthenticated" } as const;
      setState(next);
      return next;
    }
  }

  useEffect(() => {
    void Promise.resolve().then(refreshSession);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        state,
        bootstrapError,
        refreshSession,
        setAuthenticated: (account) => {
          setBootstrapError(false);
          setState({ status: "authenticated", account });
        },
        setUnauthenticated: () => {
          clearCsrfToken();
          setBootstrapError(false);
          setState({ status: "unauthenticated" });
        },
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
}
