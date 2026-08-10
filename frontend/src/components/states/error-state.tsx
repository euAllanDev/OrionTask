export function ErrorState({
  message = "Não foi possível carregar este conteúdo.",
}: {
  message?: string;
}) {
  return (
    <section
      aria-live="polite"
      className="rounded-lg border border-red-200 bg-red-50 px-6 py-5 text-sm text-red-900"
    >
      {message}
    </section>
  );
}
