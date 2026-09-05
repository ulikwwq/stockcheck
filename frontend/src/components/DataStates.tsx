import type { ReactNode } from "react";

export function LoadingState({ label = "Загрузка…" }: { label?: string }) {
  return (
    <div className="flex items-center justify-center gap-2 py-12 text-sm text-slate-500">
      <span className="h-4 w-4 animate-spin rounded-full border-2 border-slate-300 border-t-slate-600" />
      {label}
    </div>
  );
}

export function EmptyState({
  title,
  description,
  action,
}: {
  title: string;
  description?: string;
  action?: ReactNode;
}) {
  return (
    <div className="flex flex-col items-center justify-center gap-2 rounded-2xl border border-dashed border-slate-300 px-4 py-14 text-center">
      <p className="text-base font-medium text-slate-700">{title}</p>
      {description && <p className="max-w-xs text-sm text-slate-500">{description}</p>}
      {action && <div className="mt-2">{action}</div>}
    </div>
  );
}

export function ErrorState({ message }: { message: string }) {
  return (
    <div className="flex flex-col items-center justify-center gap-1 rounded-2xl border border-red-200 bg-red-50 py-12 text-center">
      <p className="text-sm font-medium text-red-700">Что-то пошло не так</p>
      <p className="text-xs text-red-600">{message}</p>
    </div>
  );
}
