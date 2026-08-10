let token: string | undefined;

export async function csrfToken(): Promise<string> {
  if (token) return token;
  const response = await fetch("/api/v1/csrf", { credentials: "include" });
  if (!response.ok) throw new Error("Não foi possível preparar a solicitação.");
  token = ((await response.json()) as { token: string }).token;
  return token;
}

export function clearCsrfToken() {
  token = undefined;
}
