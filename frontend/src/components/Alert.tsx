import type { ReactNode } from "react";

type AlertVariant = "error" | "success" | "info";

const variantClasses: Record<AlertVariant, string> = {
  error: "bg-red-50 text-red-700 border-red-200",
  success: "bg-emerald-50 text-emerald-700 border-emerald-200",
  info: "bg-slate-50 text-slate-700 border-slate-200",
};

interface AlertProps {
  variant?: AlertVariant;
  children: ReactNode;
  onDismiss?: () => void;
}

export function Alert({ variant = "info", children, onDismiss }: AlertProps) {
  return (
    <div
      role={variant === "error" ? "alert" : "status"}
      className={`flex items-start justify-between gap-3 rounded-md border px-3.5 py-2.5 text-sm ${variantClasses[variant]}`}
    >
      <span>{children}</span>
      {onDismiss && (
        <button
          type="button"
          onClick={onDismiss}
          className="shrink-0 text-current opacity-60 hover:opacity-100"
          aria-label="Dismiss"
        >
          ✕
        </button>
      )}
    </div>
  );
}
