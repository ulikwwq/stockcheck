import { useEffect, useState } from "react";
import { profitService } from "../../services/profitService";
import { ApiError } from "../../services/apiClient";
import type { DailyProfit } from "../../types/profit";
import { LoadingState, EmptyState, ErrorState } from "../../components/DataStates";
import { formatDate, formatMoney } from "../../utils/format";

export function ProfitPage() {
  const [days, setDays] = useState<DailyProfit[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    profitService
      .daily()
      .then(setDays)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Не удалось загрузить данные о прибыли"))
      .finally(() => setIsLoading(false));
  }, []);

  if (isLoading) return <LoadingState label="Загрузка данных о прибыли…" />;
  if (error) return <ErrorState message={error} />;

  const todayKey = new Date().toISOString().slice(0, 10);
  const today = days.find((d) => d.date === todayKey);
  const history = days.filter((d) => d.date !== todayKey);

  return (
    <div className="flex flex-col gap-4">
      <div>
        <h1 className="text-xl font-bold text-slate-900">Прибыль</h1>
      </div>

      {days.length === 0 ? (
        <EmptyState title="Продаж пока нет" />
      ) : (
        <>
          <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <p className="text-sm font-medium text-slate-500">Сегодня</p>
            <dl className="mt-3 flex flex-col gap-2 text-base">
              <div className="flex items-center justify-between">
                <dt className="text-slate-500">Выручка</dt>
                <dd className="font-semibold text-slate-900">{formatMoney(today?.revenue ?? 0)}</dd>
              </div>
              <div className="flex items-center justify-between">
                <dt className="text-slate-500">Себестоимость</dt>
                <dd className="font-semibold text-slate-900">{formatMoney(today?.cost ?? 0)}</dd>
              </div>
              <div className="flex items-center justify-between border-t border-slate-100 pt-2">
                <dt className={(today?.profit ?? 0) < 0 ? "text-red-600" : "text-emerald-700"}>
                  {(today?.profit ?? 0) < 0 ? "Убыток" : "Прибыль"}
                </dt>
                <dd
                  className={`text-lg font-bold ${
                    (today?.profit ?? 0) < 0 ? "text-red-600" : "text-emerald-700"
                  }`}
                >
                  {(today?.profit ?? 0) >= 0 ? "+" : ""}
                  {formatMoney(today?.profit ?? 0)}
                </dd>
              </div>
            </dl>
            {today?.profitPartiallyUnavailable && (
              <p className="mt-2 text-xs text-slate-400">
                Для части товаров не указана цена закупки — прибыль по ним не рассчитана.
              </p>
            )}
          </div>

          {history.length > 0 && (
            <div className="flex flex-col gap-2.5">
              <p className="text-sm font-medium text-slate-500">Предыдущие дни</p>
              {history.map((day) => (
                <div
                  key={day.date}
                  className="flex items-center justify-between rounded-2xl border border-slate-200 bg-white p-4 shadow-sm"
                >
                  <div>
                    <p className="text-sm font-medium text-slate-900">{formatDate(day.date)}</p>
                    <p className="text-xs text-slate-500">Выручка: {formatMoney(day.revenue)}</p>
                  </div>
                  <p className={`font-semibold ${day.profit < 0 ? "text-red-600" : "text-emerald-700"}`}>
                    {day.profit >= 0 ? "+" : ""}
                    {formatMoney(day.profit)}
                  </p>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
}
