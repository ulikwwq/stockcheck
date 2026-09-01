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

export function AuditLogPage() {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    auditLogService
      .list()
      .then(setLogs)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Не удалось загрузить историю"))
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
          {logs.map((log) => (
            <div key={log.id} className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
              <p className="text-xs text-slate-400">{formatDateTime(log.createdAt)}</p>
              <p className="mt-1 text-sm font-medium text-slate-900">{describe(log)}</p>
              {log.userName && <p className="mt-0.5 text-xs text-slate-400">{log.userName}</p>}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
