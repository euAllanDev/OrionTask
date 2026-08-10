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

export async function apiFetch(
  path: string,
  init: RequestInit = {},
): Promise<Response> {
  const method = init.method?.toUpperCase() ?? "GET";
  const mutable = !safeMethods.has(method);
  const request = async () =>
    fetch(path, {
      ...init,
      credentials: "include",
      headers: {
        ...init.headers,
        ...(mutable ? { "X-CSRF-TOKEN": await csrfToken() } : {}),
      },
    });
  let response = await request();
  if (mutable && response.status === 403) {
    clearCsrfToken();
    response = await request();
  }
  return response;
}
