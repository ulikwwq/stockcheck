import type { ButtonHTMLAttributes } from "react";

type Variant = "primary" | "secondary" | "danger" | "ghost";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  isLoading?: boolean;
}

const variantClasses: Record<Variant, string> = {
  primary: "bg-slate-900 text-white active:bg-slate-700 disabled:bg-slate-400",
  secondary: "bg-white text-slate-900 border border-slate-300 active:bg-slate-50 disabled:text-slate-400",
  danger: "bg-red-600 text-white active:bg-red-700 disabled:bg-red-300",
  ghost: "bg-transparent text-slate-600 active:bg-slate-100 disabled:text-slate-300",
};

export function Button({
  variant = "primary",
  isLoading = false,
  disabled,
  className = "",
  children,
  ...rest
}: ButtonProps) {
  return (
    <button
      // min-h-11 (44px) keeps every button a comfortable thumb target on a phone.
      className={`inline-flex min-h-11 items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-sm font-semibold transition-colors disabled:cursor-not-allowed ${variantClasses[variant]} ${className}`}
      disabled={disabled || isLoading}
      {...rest}
    >
      {isLoading && (
        <span className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
      )}
      {children}
    </button>
  );
}
