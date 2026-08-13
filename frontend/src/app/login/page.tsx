import { AuthLayout } from "@/features/auth/auth-layout";
import { LoginForm } from "@/features/auth/login-form";
import { PublicGuard } from "@/features/auth/public-guard";

export default function LoginPage() {
  return (
    <PublicGuard>
      <AuthLayout title="Entrar">
        <LoginForm />
      </AuthLayout>
    </PublicGuard>
  );
}
