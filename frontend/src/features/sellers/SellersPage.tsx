import { useCallback, useEffect, useState, type FormEvent } from "react";
import { sellerService } from "../../services/sellerService";
import { ApiError } from "../../services/apiClient";
import type { ManagedUser } from "../../types/user";
import { Button } from "../../components/Button";
import { Modal } from "../../components/Modal";
import { FormField, inputClass } from "../../components/FormField";
import { Alert } from "../../components/Alert";
import { Badge } from "../../components/Badge";
import { LoadingState, EmptyState, ErrorState } from "../../components/DataStates";

export function SellersPage() {
  const [sellers, setSellers] = useState<ManagedUser[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [formError, setFormError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [managingSeller, setManagingSeller] = useState<ManagedUser | null>(null);
  const [resetPassword, setResetPassword] = useState("");
  const [manageError, setManageError] = useState<string | null>(null);
  const [manageBusy, setManageBusy] = useState(false);

  const load = useCallback(() => {
    setIsLoading(true);
    setError(null);
    sellerService
      .list()
      .then(setSellers)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Не удалось загрузить продавцов"))
      .finally(() => setIsLoading(false));
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  function openCreateModal() {
    setUsername("");
    setPassword("");
    setFirstName("");
    setLastName("");
    setFormError(null);
    setIsCreateOpen(true);
  }

  async function handleCreate(e: FormEvent) {
    e.preventDefault();
    if (!username.trim()) {
      setFormError("Введите логин");
      return;
    }
    if (password.length < 6) {
      setFormError("Пароль должен содержать не менее 6 символов");
      return;
    }

    setFormError(null);
    setIsSubmitting(true);
    try {
      await sellerService.create({
        username: username.trim(),
        password,
        firstName: firstName.trim() || undefined,
        lastName: lastName.trim() || undefined,
      });
      await load();
      setIsCreateOpen(false);
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : "Не удалось создать продавца");
    } finally {
      setIsSubmitting(false);
    }
  }

  function openManageModal(seller: ManagedUser) {
    setManagingSeller(seller);
    setResetPassword("");
    setManageError(null);
  }

  async function handleToggleActive() {
    if (!managingSeller) return;
    setManageError(null);
    setManageBusy(true);
    try {
      if (managingSeller.active) {
        await sellerService.deactivate(managingSeller.id);
      } else {
        await sellerService.activate(managingSeller.id);
      }
      await load();
      setManagingSeller(null);
    } catch (err) {
      setManageError(err instanceof ApiError ? err.message : "Не удалось изменить статус продавца");
    } finally {
      setManageBusy(false);
    }
  }

  async function handleResetPassword() {
    if (!managingSeller) return;
    if (resetPassword.length < 6) {
      setManageError("Пароль должен содержать не менее 6 символов");
      return;
    }
    setManageError(null);
    setManageBusy(true);
    try {
      await sellerService.update(managingSeller.id, { newPassword: resetPassword });
      setManagingSeller(null);
    } catch (err) {
      setManageError(err instanceof ApiError ? err.message : "Не удалось сбросить пароль");
    } finally {
      setManageBusy(false);
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div>
        <h1 className="text-xl font-bold text-slate-900">Продавцы</h1>
      </div>

      {isLoading ? (
        <LoadingState label="Загрузка продавцов…" />
      ) : error ? (
        <ErrorState message={error} />
      ) : sellers.length === 0 ? (
        <EmptyState
          title="Продавцов пока нет"
          description="Создайте продавца, чтобы он мог входить в приложение и продавать товары."
          action={<Button onClick={openCreateModal}>+ Добавить продавца</Button>}
        />
      ) : (
        <div className="flex flex-col gap-2.5">
          {sellers.map((seller) => {
            const displayName = [seller.firstName, seller.lastName].filter(Boolean).join(" ");
            return (
              <button
                key={seller.id}
                type="button"
                onClick={() => openManageModal(seller)}
                className="flex items-center justify-between gap-3 rounded-2xl border border-slate-200 bg-white p-4 text-left shadow-sm active:bg-slate-50"
              >
                <div className="min-w-0">
                  <p className="truncate text-base font-semibold text-slate-900">
                    {displayName || seller.username}
                  </p>
                  {displayName && <p className="text-sm text-slate-500">{seller.username}</p>}
                </div>
                <Badge active={seller.active} />
              </button>
            );
          })}
        </div>
      )}

      {sellers.length > 0 && (
        <div className="fixed inset-x-0 bottom-20 z-20 flex justify-center px-4">
          <Button onClick={openCreateModal} className="w-full max-w-lg shadow-lg">
            + Добавить продавца
          </Button>
        </div>
      )}

      <Modal title="Добавить продавца" isOpen={isCreateOpen} onClose={() => setIsCreateOpen(false)}>
        <form onSubmit={handleCreate} className="flex flex-col gap-4">
          {formError && <Alert variant="error">{formError}</Alert>}

          <FormField label="Логин" htmlFor="s-username" required>
            <input
              id="s-username"
              required
              autoCapitalize="none"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className={inputClass}
              placeholder="seller01"
            />
          </FormField>

          <FormField label="Пароль" htmlFor="s-password" required>
            <input
              id="s-password"
              type="password"
              required
              minLength={6}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className={inputClass}
            />
          </FormField>

          <div className="grid grid-cols-2 gap-3">
            <FormField label="Имя" htmlFor="s-first">
              <input
                id="s-first"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                className={inputClass}
                placeholder="Иван"
              />
            </FormField>
            <FormField label="Фамилия" htmlFor="s-last">
              <input
                id="s-last"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                className={inputClass}
              />
            </FormField>
          </div>

          <div className="mt-1 flex flex-col gap-2">
            <Button type="submit" isLoading={isSubmitting} className="w-full">
              Создать
            </Button>
            <Button type="button" variant="ghost" onClick={() => setIsCreateOpen(false)} className="w-full">
              Отмена
            </Button>
          </div>
        </form>
      </Modal>

      <Modal
        title={managingSeller ? [managingSeller.firstName, managingSeller.lastName].filter(Boolean).join(" ") || managingSeller.username : ""}
        isOpen={!!managingSeller}
        onClose={() => setManagingSeller(null)}
      >
        {managingSeller && (
          <div className="flex flex-col gap-4">
            {manageError && <Alert variant="error">{manageError}</Alert>}
            <p className="text-sm text-slate-500">Логин: {managingSeller.username}</p>

            <Button
              type="button"
              variant={managingSeller.active ? "danger" : "primary"}
              isLoading={manageBusy}
              onClick={handleToggleActive}
              className="w-full"
            >
              {managingSeller.active ? "Деактивировать" : "Активировать"}
            </Button>

            <FormField label="Новый пароль" htmlFor="reset-password">
              <input
                id="reset-password"
                type="password"
                minLength={6}
                value={resetPassword}
                onChange={(e) => setResetPassword(e.target.value)}
                className={inputClass}
                placeholder="Не менее 6 символов"
              />
            </FormField>
            <Button
              type="button"
              variant="secondary"
              isLoading={manageBusy}
              onClick={handleResetPassword}
              className="w-full"
            >
              Сбросить пароль
            </Button>
          </div>
        )}
      </Modal>
    </div>
  );
}
