import { clearCsrfToken, csrfToken } from "@/lib/csrf/token";

const safeMethods = new Set(["GET", "HEAD", "OPTIONS"]);

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

export function responseError(
  response: Response,
  organizationScoped = false,
): ApiError {
  if (response.status === 404) {
    return new ApiError(
      404,
      organizationScoped ? "Recurso indisponível." : "Recurso não encontrado.",
    );
  }
  if (response.status === 401)
    return new ApiError(401, "Autenticação necessária.");
  if (response.status === 403) return new ApiError(403, "Ação não permitida.");
  return new ApiError(
    response.status,
    "Não foi possível concluir a solicitação.",
  );
}

export type ApiFetchOptions = RequestInit & { csrf?: boolean };

export async function apiFetch(
  path: string,
  { csrf = true, ...init }: ApiFetchOptions = {},
): Promise<Response> {
  const method = init.method?.toUpperCase() ?? "GET";
  const requiresCsrf = csrf && !safeMethods.has(method);
  const response = await fetch(path, {
    ...init,
    credentials: "include",
    headers: {
      ...init.headers,
      ...(requiresCsrf ? { "X-CSRF-TOKEN": await csrfToken() } : {}),
    },
  });
  if (requiresCsrf && response.status === 403) clearCsrfToken();
  return response;
}
