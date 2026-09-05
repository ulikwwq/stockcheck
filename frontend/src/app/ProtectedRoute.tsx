import type { ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "./AuthContext";
import type { RoleName } from "../types/auth";
import { LoadingState } from "../components/DataStates";

interface ProtectedRouteProps {
  children: ReactNode;
  roles?: RoleName[];
}

export function ProtectedRoute({ children, roles }: ProtectedRouteProps) {
  const { isAuthenticated, isLoading, hasRole } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return <LoadingState label="Проверка сеанса…" />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (roles && roles.length > 0 && !hasRole(...roles)) {
    return (
      <div className="mx-auto mt-16 max-w-md rounded-lg border border-slate-200 bg-white p-6 text-center shadow-sm">
        <h1 className="text-lg font-semibold text-slate-900">Доступ запрещен</h1>
        <p className="mt-1 text-sm text-slate-500">
          У вас нет прав для просмотра этой страницы.
        </p>
      </div>
    );
  }

  return <>{children}</>;
}
