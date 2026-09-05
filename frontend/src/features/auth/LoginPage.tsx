import { useState } from "react";
import type { FormEvent } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../app/AuthContext";
import { getHomeRoute } from "../../app/navigation";
import { Button } from "../../components/Button";
import { FormField, inputClass } from "../../components/FormField";
import { Alert } from "../../components/Alert";
import { ApiError } from "../../services/apiClient";

interface LocationState {
  from?: { pathname: string };
}

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      const user = await login({ username: username.trim(), password });
      const state = location.state as LocationState | null;
      const redirectTo = state?.from?.pathname || getHomeRoute(user.roles);
      navigate(redirectTo, { replace: true });
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.status === 401 ? "Неверный логин или пароль" : err.message);
      } else {
        setError("Не удалось войти. Попробуйте еще раз.");
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-sm rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <h1 className="text-xl font-bold text-slate-900">StockCheck</h1>
        <p className="mt-1 text-sm text-slate-500">Войдите, чтобы управлять бизнесом</p>

        <form onSubmit={handleSubmit} className="mt-6 flex flex-col gap-4">
          {error && <Alert variant="error">{error}</Alert>}

          <FormField label="Логин" htmlFor="username" required>
            <input
              id="username"
              type="text"
              required
              autoComplete="username"
              autoCapitalize="none"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className={inputClass}
              placeholder="shop_owner"
            />
          </FormField>

          <FormField label="Пароль" htmlFor="password" required>
            <input
              id="password"
              type="password"
              required
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className={inputClass}
            />
          </FormField>

          <Button type="submit" isLoading={isSubmitting} className="mt-1 w-full">
            Войти
          </Button>
        </form>
      </div>
    </div>
  );
}
