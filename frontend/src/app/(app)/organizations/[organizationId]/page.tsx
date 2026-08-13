import { AppGuard } from "@/features/auth/app-guard";
import { OrganizationPage as OrganizationContent } from "@/features/organizations/organization-page";

export default async function OrganizationPage({
  params,
}: {
  params: Promise<{ organizationId: string }>;
}) {
  const { organizationId } = await params;
  return (
    <AppGuard>
      {() => <OrganizationContent organizationId={organizationId} />}
    </AppGuard>
  );
}
