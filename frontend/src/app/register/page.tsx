import { AuthLayout } from "@/features/auth/auth-layout";
import { RegisterForm } from "@/features/auth/register-form";
import { PublicGuard } from "@/features/auth/public-guard";

export default function RegisterPage() {
  return (
    <PublicGuard>
      <AuthLayout title="Criar acesso">
        <RegisterForm />
      </AuthLayout>
    </PublicGuard>
  );
}
