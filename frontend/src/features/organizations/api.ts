import { apiFetch } from "@/lib/api/client";

export type Organization = {
  id: string;
  name: string;
  createdAt: string;
  updatedAt: string;
  role: "OWNER" | "ADMIN" | "TECHNICIAN";
};

function isOrganization(value: unknown): value is Organization {
  if (typeof value !== "object" || value === null) return false;
  const organization = value as Record<string, unknown>;
  return (
    typeof organization.id === "string" &&
    typeof organization.name === "string" &&
    typeof organization.createdAt === "string" &&
    typeof organization.updatedAt === "string" &&
    (organization.role === "OWNER" ||
      organization.role === "ADMIN" ||
      organization.role === "TECHNICIAN")
  );
}

export async function organizations(): Promise<Organization[]> {
  const response = await apiFetch("/api/v1/organizations", { csrf: false });
  if (!response.ok) throw new Error(String(response.status));
  const body: unknown = await response.json();
  if (!Array.isArray(body) || !body.every(isOrganization))
    throw new Error("Invalid organizations");
  return body;
}
