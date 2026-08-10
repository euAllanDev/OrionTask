import { Skeleton } from "@/components/ui/skeleton";

export function LoadingState() {
  return (
    <div aria-label="Carregando" className="space-y-4">
      <Skeleton className="h-7 w-48" />
      <Skeleton className="h-32 w-full" />
    </div>
  );
}
