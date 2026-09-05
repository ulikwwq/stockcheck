import { useCallback, useEffect, useState, type FormEvent } from "react";
import { tenantService } from "../../services/tenantService";
import { ApiError } from "../../services/apiClient";
import type { Tenant } from "../../types/tenant";
import { Button } from "../../components/Button";
import { Modal } from "../../components/Modal";
import { FormField, inputClass } from "../../components/FormField";
import { Alert } from "../../components/Alert";
import { LoadingState, EmptyState, ErrorState } from "../../components/DataStates";
import { formatDate } from "../../utils/format";

function statusLabel(status: Tenant["status"]): { label: string; tone: "active" | "warning" | "danger" } {
  switch (status) {
    case "ACTIVE":
      return { label: "Активен", tone: "active" };
    case "INACTIVE":
      return { label: "Заблокирован", tone: "warning" };
    case "DELETED":
      return { label: "Удален", tone: "danger" };
  }
}

function StatusBadge({ status }: { status: Tenant["status"] }) {
  const { label, tone } = statusLabel(status);
  const toneClass =
    tone === "active"
      ? "bg-emerald-100 text-emerald-700"
      : tone === "warning"
        ? "bg-amber-100 text-amber-700"
        : "bg-red-100 text-red-700";
  return <span className={`inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium ${toneClass}`}>{label}</span>;
}

export function TenantsPage() {
  const [tenants, setTenants] = useState<Tenant[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [tenantName, setTenantName] = useState("");
  const [shopName, setShopName] = useState("");
  const [ownerUsername, setOwnerUsername] = useState("");
  const [ownerPassword, setOwnerPassword] = useState("");
  const [ownerFirstName, setOwnerFirstName] = useState("");
  const [ownerLastName, setOwnerLastName] = useState("");
  const [formError, setFormError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [managingTenant, setManagingTenant] = useState<Tenant | null>(null);
  const [resetPassword, setResetPassword] = useState("");
  const [newUsername, setNewUsername] = useState("");
  const [manageError, setManageError] = useState<string | null>(null);
  const [manageSuccess, setManageSuccess] = useState<string | null>(null);
  const [manageBusy, setManageBusy] = useState(false);

  const load = useCallback(() => {
    setIsLoading(true);
    setError(null);
    tenantService
      .list()
      .then(setTenants)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Не удалось загрузить бизнесы"))
      .finally(() => setIsLoading(false));
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  function openCreateModal() {
    setTenantName("");
    setShopName("");
    setOwnerUsername("");
    setOwnerPassword("");
    setOwnerFirstName("");
    setOwnerLastName("");
    setFormError(null);
    setIsCreateOpen(true);
  }

  async function handleCreate(e: FormEvent) {
    e.preventDefault();
    if (!tenantName.trim()) {
      setFormError("Введите название бизнеса");
      return;
    }
    if (!ownerUsername.trim()) {
      setFormError("Введите логин владельца");
      return;
    }
    if (ownerPassword.length < 6) {
      setFormError("Пароль должен содержать не менее 6 символов");
      return;
    }

    setFormError(null);
    setIsSubmitting(true);
    try {
      await tenantService.create({
        tenantName: tenantName.trim(),
        shopName: shopName.trim() || undefined,
        ownerUsername: ownerUsername.trim(),
        ownerPassword,
        ownerFirstName: ownerFirstName.trim() || undefined,
        ownerLastName: ownerLastName.trim() || undefined,
      });
      await load();
      setIsCreateOpen(false);
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : "Не удалось создать бизнес");
    } finally {
      setIsSubmitting(false);
    }
  }

  function openManageModal(tenant: Tenant) {
    setManagingTenant(tenant);
    setResetPassword("");
    setNewUsername(tenant.ownerUsername ?? "");
    setManageError(null);
    setManageSuccess(null);
  }

  async function handleToggleActive() {
    if (!managingTenant) return;
    setManageError(null);
    setManageBusy(true);
    try {
      if (managingTenant.status === "ACTIVE") {
        await tenantService.deactivate(managingTenant.id);
      } else {
        await tenantService.activate(managingTenant.id);
      }
      await load();
      setManagingTenant(null);
    } catch (err) {
      setManageError(err instanceof ApiError ? err.message : "Не удалось изменить статус бизнеса");
    } finally {
      setManageBusy(false);
    }
  }

  async function handleResetPassword() {
    if (!managingTenant) return;
    if (resetPassword.length < 6) {
      setManageError("Пароль должен содержать не менее 6 символов");
      return;
    }
    setManageError(null);
    setManageBusy(true);
    try {
      await tenantService.resetOwnerPassword(managingTenant.id, { newPassword: resetPassword });
      setManageSuccess("Пароль владельца обновлен");
      setResetPassword("");
    } catch (err) {
      setManageError(err instanceof ApiError ? err.message : "Не удалось сбросить пароль");
    } finally {
      setManageBusy(false);
    }
  }

  async function handleChangeUsername() {
    if (!managingTenant) return;
    if (!newUsername.trim() || newUsername.trim() === managingTenant.ownerUsername) {
      setManageError("Введите новый логин");
      return;
    }
    if (!window.confirm(`Изменить логин владельца на «${newUsername.trim()}»?`)) return;

    setManageError(null);
    setManageBusy(true);
    try {
      const updated = await tenantService.changeOwnerUsername(managingTenant.id, {
        newUsername: newUsername.trim(),
      });
      setTenants((prev) => prev.map((t) => (t.id === updated.id ? updated : t)));
      setManagingTenant(updated);
      setManageSuccess("Логин владельца изменен");
    } catch (err) {
      setManageError(err instanceof ApiError ? err.message : "Не удалось изменить логин");
    } finally {
      setManageBusy(false);
    }
  }

  async function handleDeleteTenant() {
    if (!managingTenant) return;
    if (
      !window.confirm(
        `Удалить бизнес «${managingTenant.name}»? Данные бизнеса сохранятся, но вход для его пользователей будет заблокирован.`
      )
    ) {
      return;
    }

    setManageError(null);
    setManageBusy(true);
    try {
      const updated = await tenantService.remove(managingTenant.id);
      setTenants((prev) => prev.map((t) => (t.id === updated.id ? updated : t)));
      setManagingTenant(null);
    } catch (err) {
      setManageError(err instanceof ApiError ? err.message : "Не удалось удалить бизнес");
    } finally {
      setManageBusy(false);
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div>
        <h1 className="text-xl font-bold text-slate-900">Бизнесы</h1>
      </div>

      {isLoading ? (
        <LoadingState label="Загрузка бизнесов…" />
      ) : error ? (
        <ErrorState message={error} />
      ) : tenants.length === 0 ? (
        <EmptyState
          title="Бизнесов пока нет"
          description="Создайте первый бизнес и учетную запись владельца."
          action={<Button onClick={openCreateModal}>Создать бизнес</Button>}
        />
      ) : (
        <div className="flex flex-col gap-2.5">
          {tenants.map((tenant) => (
            <button
              key={tenant.id}
              type="button"
              onClick={() => openManageModal(tenant)}
              className="flex items-center justify-between gap-3 rounded-2xl border border-slate-200 bg-white p-4 text-left shadow-sm active:bg-slate-50"
            >
              <div className="min-w-0">
                <p className="truncate text-base font-semibold text-slate-900">{tenant.name}</p>
                <p className="text-sm text-slate-500">
                  {tenant.ownerUsername ?? "—"} · {formatDate(tenant.createdAt)}
                </p>
              </div>
              <StatusBadge status={tenant.status} />
            </button>
          ))}
        </div>
      )}

      {tenants.length > 0 && (
        <div className="fixed inset-x-0 bottom-6 z-20 flex justify-center px-4">
          <Button onClick={openCreateModal} className="w-full max-w-lg shadow-lg">
            Создать бизнес
          </Button>
        </div>
      )}

      <Modal title="Создать бизнес" isOpen={isCreateOpen} onClose={() => setIsCreateOpen(false)}>
        <form onSubmit={handleCreate} className="flex flex-col gap-4">
          {formError && <Alert variant="error">{formError}</Alert>}

          <FormField label="Название бизнеса" htmlFor="t-name" required>
            <input
              id="t-name"
              required
              value={tenantName}
              onChange={(e) => setTenantName(e.target.value)}
              className={inputClass}
              placeholder="Coffee House"
            />
          </FormField>

          <FormField label="Название магазина" htmlFor="t-shop">
            <input
              id="t-shop"
              value={shopName}
              onChange={(e) => setShopName(e.target.value)}
              className={inputClass}
              placeholder="Необязательно — по умолчанию совпадает с названием бизнеса"
            />
          </FormField>

          <FormField label="Логин владельца" htmlFor="t-owner-username" required>
            <input
              id="t-owner-username"
              required
              autoCapitalize="none"
              value={ownerUsername}
              onChange={(e) => setOwnerUsername(e.target.value)}
              className={inputClass}
              placeholder="coffee_admin"
            />
          </FormField>

          <FormField label="Пароль" htmlFor="t-owner-password" required>
            <input
              id="t-owner-password"
              type="password"
              required
              minLength={6}
              value={ownerPassword}
              onChange={(e) => setOwnerPassword(e.target.value)}
              className={inputClass}
            />
          </FormField>

          <div className="grid grid-cols-2 gap-3">
            <FormField label="Имя владельца" htmlFor="t-owner-first">
              <input
                id="t-owner-first"
                value={ownerFirstName}
                onChange={(e) => setOwnerFirstName(e.target.value)}
                className={inputClass}
              />
            </FormField>
            <FormField label="Фамилия владельца" htmlFor="t-owner-last">
              <input
                id="t-owner-last"
                value={ownerLastName}
                onChange={(e) => setOwnerLastName(e.target.value)}
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

      <Modal title={managingTenant?.name ?? ""} isOpen={!!managingTenant} onClose={() => setManagingTenant(null)}>
        {managingTenant && (
          <div className="flex flex-col gap-4">
            {manageError && <Alert variant="error">{manageError}</Alert>}
            {manageSuccess && <Alert variant="success">{manageSuccess}</Alert>}
            <div className="flex items-center justify-between">
              <p className="text-sm text-slate-500">Владелец: {managingTenant.ownerUsername ?? "—"}</p>
              <StatusBadge status={managingTenant.status} />
            </div>

            <Button
              type="button"
              variant={managingTenant.status === "ACTIVE" ? "danger" : "primary"}
              isLoading={manageBusy}
              onClick={handleToggleActive}
              className="w-full"
            >
              {managingTenant.status === "ACTIVE" ? "Заблокировать" : "Активировать"}
            </Button>

            <hr className="border-slate-100" />

            <FormField label="Логин владельца" htmlFor="t-change-username">
              <input
                id="t-change-username"
                value={newUsername}
                onChange={(e) => setNewUsername(e.target.value)}
                className={inputClass}
              />
            </FormField>
            <Button
              type="button"
              variant="secondary"
              isLoading={manageBusy}
              onClick={handleChangeUsername}
              className="w-full"
            >
              Изменить логин
            </Button>

            <FormField label="Новый пароль владельца" htmlFor="t-reset-password">
              <input
                id="t-reset-password"
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

            <hr className="border-slate-100" />

            <Button
              type="button"
              variant="danger"
              isLoading={manageBusy}
              disabled={managingTenant.status === "DELETED"}
              onClick={handleDeleteTenant}
              className="w-full"
            >
              {managingTenant.status === "DELETED" ? "Бизнес удален" : "Удалить бизнес"}
            </Button>
          </div>
        )}
      </Modal>
    </div>
  );
}
