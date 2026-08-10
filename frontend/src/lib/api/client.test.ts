import { describe, expect, it, vi } from "vitest";

import { apiFetch, responseError } from "@/lib/api/client";

describe("responseError", () => {
  it("does not disclose whether organization-scoped resource exists", () => {
    expect(
      responseError(new Response(null, { status: 404 }), true).message,
    ).toBe("Recurso indisponível.");
  });
});

describe("apiFetch", () => {
  it("includes browser credentials on read requests", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response());
    vi.stubGlobal("fetch", fetchMock);
    await apiFetch("/api/v1/organizations/example");
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/v1/organizations/example",
      expect.objectContaining({ credentials: "include" }),
    );
  });
});
