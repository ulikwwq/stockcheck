import type { ReactNode } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "./app/AuthContext";
import { ProtectedRoute } from "./app/ProtectedRoute";
import { AppLayout } from "./app/AppLayout";
import { getHomeRoute } from "./app/navigation";
import { LoginPage } from "./features/auth/LoginPage";
import { ProductsPage } from "./features/products/ProductsPage";
import { SellersPage } from "./features/sellers/SellersPage";
import { ProfitPage } from "./features/profit/ProfitPage";
import { AuditLogPage } from "./features/audit/AuditLogPage";
import { TenantsPage } from "./features/admin/TenantsPage";

function HomeRedirect() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return <Navigate to={getHomeRoute(user.roles)} replace />;
}

function withLayout(children: ReactNode) {
  return <AppLayout>{children}</AppLayout>;
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route path="/" element={<ProtectedRoute>{<HomeRedirect />}</ProtectedRoute>} />

      <Route
        path="/products"
        element={
          <ProtectedRoute roles={["ADMINISTRATOR", "SELLER"]}>
            {withLayout(<ProductsPage />)}
          </ProtectedRoute>
        }
      />
      <Route
        path="/sellers"
        element={
          <ProtectedRoute roles={["ADMINISTRATOR"]}>{withLayout(<SellersPage />)}</ProtectedRoute>
        }
      />
      <Route
        path="/history"
        element={
          <ProtectedRoute roles={["ADMINISTRATOR"]}>{withLayout(<AuditLogPage />)}</ProtectedRoute>
        }
      />
      <Route
        path="/profit"
        element={<ProtectedRoute roles={["ADMINISTRATOR"]}>{withLayout(<ProfitPage />)}</ProtectedRoute>}
      />
      <Route
        path="/admin/tenants"
        element={<ProtectedRoute roles={["SUPER_ADMIN"]}>{withLayout(<TenantsPage />)}</ProtectedRoute>}
      />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
