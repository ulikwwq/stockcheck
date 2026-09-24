import { useEffect, useState } from "react";
import { auditLogService } from "../../services/auditLogService";
import { ApiError } from "../../services/apiClient";
import type { AuditLog } from "../../types/auditLog";
import { LoadingState, EmptyState, ErrorState } from "../../components/DataStates";
import { formatDateTime, formatMoney } from "../../utils/format";

function describe(log: AuditLog): string {
  const details = log.details ?? "";

  switch (log.action) {
    case "PRODUCT_CREATED":
      return `Добавлен товар «${details}»`;
    case "PRODUCT_ACTIVATED":
      return `Товар «${details}» снова активен`;
    case "PRODUCT_DELETED":
      return `Удален товар «${details}»`;
    case "PRODUCT_UPDATED":
      return `Изменен товар «${details}»`;
    case "SALE_CREATED":
      return `Продажа на сумму ${formatMoney(Number(details))}`;
    case "SELLER_CREATED":
      return `Добавлен продавец «${details}»`;
    case "SELLER_ACTIVATED":
      return `Продавец «${details}» активирован`;
    case "SELLER_DEACTIVATED":
      return `Продавец «${details}» деактивирован`;
    case "PASSWORD_RESET":
      return `Сброшен пароль для «${details}»`;
    case "BUSINESS_CREATED":
      return `Создан бизнес «${details}»`;
    case "BUSINESS_ACTIVATED":
      return `Бизнес «${details}» активирован`;
    case "BUSINESS_DEACTIVATED":
      return `Бизнес «${details}» заблокирован`;
    case "BUSINESS_DELETED":
      return `Бизнес «${details}» удален`;
    case "OWNER_USERNAME_CHANGED":
      return `Логин владельца изменен: ${details}`;
    default:
      return details || log.action;
  }
}

function fieldLabel(key: string): string {
  const labels: Record<string, string> = {
    name: "Название",
    sku: "Артикул",
    description: "Описание",
    purchasePrice: "Цена закупки",
    defaultSalePrice: "Цена продажи",
    quantity: "Количество",
    active: "Активен",
    categoryId: "Категория",
  };

  return labels[key] ?? key;
}

function formatAuditValue(key: string, value: unknown): string {
  if (value === null || value === undefined || value === "") {
    return "—";
  }

  if (
    key === "purchasePrice" ||
    key === "defaultSalePrice"
  ) {
    return formatMoney(Number(value));
  }

  if (key === "active") {
    return value === true ? "Да" : "Нет";
  }

  return String(value);
}

function parseAuditValues(value: string | null): Record<string, unknown> | null {
  if (!value) {
    return null;
  }

  try {
    const parsed: unknown = JSON.parse(value);

    if (parsed && typeof parsed === "object" && !Array.isArray(parsed)) {
      return parsed as Record<string, unknown>;
    }

    return null;
  } catch {
    return null;
  }
}

function AuditChanges({ log }: { log: AuditLog }) {
  const oldValues = parseAuditValues(log.oldValue);
  const newValues = parseAuditValues(log.newValue);

  if (oldValues && newValues) {
    const keys = Array.from(
      new Set([...Object.keys(oldValues), ...Object.keys(newValues)])
    );

    return (
      <div className="mt-4 border-t border-slate-100 pt-4">
        <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">
          Изменения
        </p>

        <div className="mt-3 overflow-x-auto">
          <table className="w-full min-w-[520px] text-left text-sm">
            <thead>
              <tr className="border-b border-slate-100 text-xs text-slate-400">
                <th className="px-2 py-2 font-medium">Поле</th>
                <th className="px-2 py-2 font-medium">Было</th>
                <th className="px-2 py-2 font-medium">Стало</th>
              </tr>
            </thead>

            <tbody>
              {keys.map((key) => {
                const oldValue = oldValues[key];
                const newValue = newValues[key];

                if (JSON.stringify(oldValue) === JSON.stringify(newValue)) {
                  return null;
                }

                return (
                  <tr
                    key={key}
                    className="border-b border-slate-50 last:border-0"
                  >
                    <td className="px-2 py-2 font-medium text-slate-700">
                      {fieldLabel(key)}
                    </td>
                    <td className="px-2 py-2 text-slate-500">
                      {formatAuditValue(key, oldValue)}
                    </td>
                    <td className="px-2 py-2 font-medium text-slate-900">
                      {formatAuditValue(key, newValue)}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    );
  }

  if (log.oldValue !== null || log.newValue !== null) {
    return (
      <div className="mt-4 border-t border-slate-100 pt-4">
        <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">
          Изменения
        </p>

        <div className="mt-3 grid gap-3 sm:grid-cols-2">
          <div>
            <p className="text-xs text-slate-400">Было</p>
            <p className="mt-1 text-sm text-slate-500">
              {log.oldValue || "—"}
            </p>
          </div>

          <div>
            <p className="text-xs text-slate-400">Стало</p>
            <p className="mt-1 text-sm font-medium text-slate-900">
              {log.newValue || "—"}
            </p>
          </div>
        </div>
      </div>
    );
  }

  return null;
}

export function AuditLogPage() {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expandedLogId, setExpandedLogId] = useState<string | null>(null);

  useEffect(() => {
    auditLogService
      .list()
      .then(setLogs)
      .catch((err) =>
        setError(
          err instanceof ApiError
            ? err.message
            : "Не удалось загрузить историю"
        )
      )
      .finally(() => setIsLoading(false));
  }, []);

  return (
    <div className="flex flex-col gap-4">
      <div>
        <h1 className="text-xl font-bold text-slate-900">История</h1>
      </div>

      {isLoading ? (
        <LoadingState label="Загрузка истории…" />
      ) : error ? (
        <ErrorState message={error} />
      ) : logs.length === 0 ? (
        <EmptyState title="История пока пуста" />
      ) : (
        <div className="flex flex-col gap-2.5">
          {logs.map((log) => {
            const isExpanded = expandedLogId === log.id;

            return (
              <button
                key={log.id}
                type="button"
                onClick={() =>
                  setExpandedLogId(isExpanded ? null : log.id)
                }
                className="w-full rounded-2xl border border-slate-200 bg-white p-4 text-left shadow-sm transition hover:border-slate-300"
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <p className="text-xs text-slate-400">
                      {formatDateTime(log.createdAt)}
                    </p>

                    <p className="mt-1 text-sm font-medium text-slate-900">
                      {describe(log)}
                    </p>

                    {log.userName && (
                      <p className="mt-0.5 text-xs text-slate-400">
                        {log.userName}
                      </p>
                    )}
                  </div>

                  <span className="shrink-0 text-sm text-slate-400">
                    {isExpanded ? "▲" : "▼"}
                  </span>
                </div>

                {isExpanded && (
                  <div className="mt-4 border-t border-slate-100 pt-4">
                    <div className="grid gap-3 text-sm sm:grid-cols-2">
                      <div>
                        <p className="text-xs text-slate-400">Кто</p>
                        <p className="mt-1 text-slate-900">
                          {log.userName || "Неизвестно"}
                        </p>
                      </div>

                      <div>
                        <p className="text-xs text-slate-400">Когда</p>
                        <p className="mt-1 text-slate-900">
                          {formatDateTime(log.createdAt)}
                        </p>
                      </div>

                      <div>
                        <p className="text-xs text-slate-400">Действие</p>
                        <p className="mt-1 text-slate-900">
                          {describe(log)}
                        </p>
                      </div>

                      <div>
                        <p className="text-xs text-slate-400">Тип объекта</p>
                        <p className="mt-1 text-slate-900">
                          {log.entityType}
                        </p>
                      </div>
                    </div>

                    <AuditChanges log={log} />
                  </div>
                )}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}