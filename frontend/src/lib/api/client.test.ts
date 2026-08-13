import { afterEach, describe, expect, it, vi } from "vitest";

import { apiFetch, responseError } from "@/lib/api/client";
import { clearCsrfToken } from "@/lib/csrf/token";

describe("responseError", () => {
  it("does not disclose whether organization-scoped resource exists", () => {
    expect(
      responseError(new Response(null, { status: 404 }), true).message,
    ).toBe("Recurso indisponível.");
  });
});

describe("apiFetch", () => {
  afterEach(() => {
    clearCsrfToken();
    vi.unstubAllGlobals();
  });
  it("includes browser credentials on read requests", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response());
    vi.stubGlobal("fetch", fetchMock);
    await apiFetch("/api/v1/organizations/example");
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/v1/organizations/example",
      expect.objectContaining({ credentials: "include" }),
    );
  });

  it("does not retry a 403 mutation", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(new Response(JSON.stringify({ token: "csrf" })))
      .mockResolvedValueOnce(new Response(null, { status: 403 }));
    vi.stubGlobal("fetch", fetchMock);
    const response = await apiFetch("/api/v1/session", { method: "DELETE" });
    expect(response.status).toBe(403);
    expect(fetchMock).toHaveBeenCalledTimes(2);
  });

  it("does not request or send CSRF when disabled", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValue(new Response(null, { status: 202 }));
    vi.stubGlobal("fetch", fetchMock);
    await apiFetch("/api/v1/accounts", { method: "POST", csrf: false });
    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/v1/accounts",
      expect.objectContaining({ headers: {} }),
    );
  });
});
