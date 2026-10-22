import { useEffect, useMemo, useState } from "react";
import { analyticsService } from "../../services/analyticsService";
import { ApiError } from "../../services/apiClient";
import type { MonthPoint, ProductHistory, Summary, TopProduct, TopSort, Totals } from "../../types/analytics";
import { LoadingState, EmptyState, ErrorState } from "../../components/DataStates";
import { Modal } from "../../components/Modal";
import { inputClass } from "../../components/FormField";
import { formatDate, formatMoney, formatNumber } from "../../utils/format";
import { PRESETS, buildPeriod, type PresetKey } from "../../utils/period";

const SORTS: { key: TopSort; label: string }[] = [
  { key: "UNITS", label: "По количеству" }, { key: "REVENUE", label: "По выручке" },
  { key: "PROFIT", label: "По прибыли" }, { key: "SALES", label: "По числу продаж" }, { key: "TREND", label: "По динамике" },
];

const monthLabel = (iso: string) =>
  new Date(iso).toLocaleDateString("ru-RU", { month: "short", year: "2-digit" });

function delta(cur: number, prev: number): string {
  if (prev === 0) return "—";
  const v = ((cur - prev) / prev) * 100;
  return `${v >= 0 ? "+" : ""}${v.toFixed(1)}%`;
}

function Card({ title, value, prev, note }: { title: string; value: number; prev: number; note?: string }) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
      <p className="text-xs text-slate-500">{title}</p>
      <p className="mt-1 text-lg font-bold text-slate-900">{formatMoney(value)}</p>
      <p className="text-xs text-slate-400">к прошлому периоду: {delta(value, prev)}</p>
      {note && <p className="mt-1 text-[11px] text-slate-400">{note}</p>}
    </div>
  );
}

function Bars({ title, data, series }: {
  title: string; data: MonthPoint[];
  series: { key: keyof MonthPoint; label: string; color: string }[];
}) {
  const max = Math.max(1, ...data.flatMap((d) => series.map((s) => Math.abs(Number(d[s.key])))));
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
      <div className="flex items-center justify-between">
        <p className="text-sm font-medium text-slate-700">{title}</p>
        <div className="flex gap-3 text-[11px] text-slate-500">
          {series.map((s) => (
            <span key={s.label} className="flex items-center gap-1">
              <span className={`inline-block h-2 w-2 rounded-sm ${s.color}`} />{s.label}
            </span>
          ))}
        </div>
      </div>
      <div className="mt-3 flex h-32 items-end gap-1.5 overflow-x-auto">
        {data.map((d) => (
          <div key={d.month} className="flex min-w-[28px] flex-1 flex-col items-center gap-1">
            <div className="flex h-24 w-full items-end justify-center gap-0.5">
              {series.map((s) => {
                const v = Number(d[s.key]);
                return (
                  <div key={s.label} title={`${s.label}: ${formatMoney(v)}`}
                    className={`w-full max-w-[14px] rounded-t ${v < 0 ? "bg-red-400" : s.color}`}
                    style={{ height: `${(Math.abs(v) / max) * 100}%` }} />
                );
              })}
            </div>
            <span className="text-[10px] text-slate-400">{monthLabel(d.month)}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

export function AnalyticsPage() {
  const [preset, setPreset] = useState<PresetKey>("last3m");
  const [customFrom, setCustomFrom] = useState("");
  const [customTo, setCustomTo] = useState("");
  const [sort, setSort] = useState<TopSort>("UNITS");
  const [search, setSearch] = useState("");
  const [debounced, setDebounced] = useState("");

  const [summary, setSummary] = useState<Summary | null>(null);
  const [monthly, setMonthly] = useState<MonthPoint[]>([]);
  const [top, setTop] = useState<TopProduct[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [history, setHistory] = useState<ProductHistory | null>(null);
  const [historyLoading, setHistoryLoading] = useState(false);

  const period = useMemo(() => buildPeriod(preset, customFrom, customTo), [preset, customFrom, customTo]);

  useEffect(() => {
    const t = setTimeout(() => setDebounced(search.trim()), 300);
    return () => clearTimeout(t);
  }, [search]);

  useEffect(() => {
    if (!period) return;
    let cancelled = false;
    setIsLoading(true);
    setError(null);
    Promise.all([
      analyticsService.summary(period),
      analyticsService.monthly(period),
      analyticsService.topProducts(period, sort, debounced),
    ])
      .then(([s, m, t]) => { if (!cancelled) { setSummary(s); setMonthly(m); setTop(t); } })
      .catch((e) => { if (!cancelled) setError(e instanceof ApiError ? e.message : "Не удалось загрузить аналитику"); })
      .finally(() => { if (!cancelled) setIsLoading(false); });
    return () => { cancelled = true; };
  }, [period, sort, debounced]);

  async function openHistory(p: TopProduct) {
    if (!period) return;
    setHistoryLoading(true);
    setHistory({ productId: p.productId, name: p.name, months: [], total: { month: null, unitsSold: 0, revenue: 0, cost: 0, grossProfit: 0 } });
    try {
      setHistory(await analyticsService.productHistory(p.productId, period));
    } catch (e) {
      setHistory(null);
      setError(e instanceof ApiError ? e.message : "Не удалось загрузить историю товара");
    } finally {
      setHistoryLoading(false);
    }
  }

  const cur: Totals | undefined = summary?.current;
  const prev: Totals | undefined = summary?.previous;

  return (
    <div className="flex flex-col gap-4 pb-8">
      <h1 className="text-xl font-bold text-slate-900">Аналитика</h1>

      <div className="flex flex-col gap-2">
        <select value={preset} onChange={(e) => setPreset(e.target.value as PresetKey)} className={inputClass}>
          {PRESETS.map((p) => <option key={p.key} value={p.key}>{p.label}</option>)}
        </select>
        {preset === "custom" && (
          <div className="grid grid-cols-2 gap-2">
            <input type="date" value={customFrom} max={customTo || undefined} onChange={(e) => setCustomFrom(e.target.value)} className={inputClass} />
            <input type="date" value={customTo} min={customFrom || undefined} onChange={(e) => setCustomTo(e.target.value)} className={inputClass} />
          </div>
        )}
        {period && <p className="text-xs text-slate-400">{formatDate(period.from)} — {formatDate(period.to)}</p>}
      </div>

      {!period ? (
        <EmptyState title="Выберите период" description="Укажите дату начала и окончания." />
      ) : isLoading ? (
        <LoadingState label="Загрузка аналитики…" />
      ) : error ? (
        <ErrorState message={error} />
      ) : !cur || !prev ? null : (
        <>
          <div className="grid grid-cols-2 gap-2.5">
            <Card title="Выручка" value={cur.revenue} prev={prev.revenue} />
            <Card title="Себестоимость" value={cur.cost} prev={prev.cost} />
            <Card title="Валовая прибыль" value={cur.grossProfit} prev={prev.grossProfit}
              note={cur.unknownCostUnits > 0 ? `Без учёта ${cur.unknownCostUnits} шт. без цены закупки/продажи` : undefined} />
            <Card title="Расходы" value={cur.expenses} prev={prev.expenses} />
            <Card title="Операционный результат (после всех расходов)" value={cur.operatingResult} prev={prev.operatingResult} />
            <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
              <p className="text-xs text-slate-500">Продаж / единиц</p>
              <p className="mt-1 text-lg font-bold text-slate-900">{formatNumber(cur.salesCount)} / {formatNumber(cur.unitsSold)}</p>
            </div>
          </div>

          <Bars title="Выручка по месяцам" data={monthly} series={[{ key: "revenue", label: "Выручка", color: "bg-slate-800" }]} />
          <Bars title="Себестоимость и валовая прибыль" data={monthly} series={[
            { key: "cost", label: "Себестоимость", color: "bg-amber-400" },
            { key: "grossProfit", label: "Прибыль", color: "bg-emerald-500" },
          ]} />
          <Bars title="Расходы по месяцам" data={monthly} series={[{ key: "expenses", label: "Расходы", color: "bg-red-400" }]} />

          <div className="overflow-x-auto rounded-2xl border border-slate-200 bg-white shadow-sm">
            <table className="w-full min-w-[560px] text-left text-sm">
              <thead><tr className="border-b border-slate-100 text-xs text-slate-400">
                <th className="px-3 py-2">Месяц</th><th className="px-3 py-2">Выручка</th><th className="px-3 py-2">Себест.</th>
                <th className="px-3 py-2">Вал. прибыль</th><th className="px-3 py-2">Расходы</th><th className="px-3 py-2">Результат</th>
              </tr></thead>
              <tbody>
                {monthly.map((m) => (
                  <tr key={m.month} className="border-b border-slate-50 last:border-0">
                    <td className="px-3 py-2">{monthLabel(m.month)}</td>
                    <td className="px-3 py-2">{formatMoney(m.revenue)}</td>
                    <td className="px-3 py-2">{formatMoney(m.cost)}</td>
                    <td className="px-3 py-2">{formatMoney(m.grossProfit)}</td>
                    <td className="px-3 py-2">{formatMoney(m.expenses)}</td>
                    <td className="px-3 py-2 font-medium">{formatMoney(m.operatingResult)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="flex flex-col gap-2">
            <p className="text-sm font-semibold text-slate-700">Самые продаваемые товары</p>
            <div className="grid grid-cols-2 gap-2">
              <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Название или артикул…" className={inputClass} />
              <select value={sort} onChange={(e) => setSort(e.target.value as TopSort)} className={inputClass}>
                {SORTS.map((s) => <option key={s.key} value={s.key}>{s.label}</option>)}
              </select>
            </div>
            {top.length === 0 ? (
              <EmptyState title="Продаж за период нет" />
            ) : top.map((p) => (
              <button key={p.productId} type="button" onClick={() => openHistory(p)}
                className="rounded-2xl border border-slate-200 bg-white p-4 text-left shadow-sm active:bg-slate-50">
                <div className="flex items-start justify-between gap-2">
                  <div className="min-w-0">
                    <p className="truncate font-semibold text-slate-900">{p.name}</p>
                    {p.sku && <p className="text-xs text-slate-400">{p.sku}</p>}
                  </div>
                  <p className="shrink-0 text-base font-bold text-slate-900">{formatNumber(p.unitsSold)} шт.</p>
                </div>
                <p className="mt-1 text-xs text-slate-500">
                  Выручка {formatMoney(p.revenue)} · Себест. {formatMoney(p.cost)} · Прибыль {formatMoney(p.grossProfit)}
                </p>
                <p className="text-xs text-slate-400">
                  Продаж: {p.salesCount} · Последняя: {formatDate(p.lastSaleAt)} ·{" "}
                  {p.changePercent === null ? "динамика: нет данных" : `динамика: ${p.changePercent >= 0 ? "+" : ""}${p.changePercent}%`}
                </p>
              </button>
            ))}
          </div>
        </>
      )}

      <Modal title={history?.name ?? ""} isOpen={!!history} onClose={() => setHistory(null)}>
        {historyLoading || !history ? (
          <LoadingState />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[420px] text-left text-sm">
              <thead><tr className="border-b border-slate-100 text-xs text-slate-400">
                <th className="py-2">Месяц</th><th>Шт.</th><th>Выручка</th><th>Себест.</th><th>Прибыль</th>
              </tr></thead>
              <tbody>
                {history.months.map((m) => (
                  <tr key={m.month!} className="border-b border-slate-50">
                    <td className="py-2">{monthLabel(m.month!)}</td><td>{m.unitsSold}</td>
                    <td>{formatMoney(m.revenue)}</td><td>{formatMoney(m.cost)}</td><td>{formatMoney(m.grossProfit)}</td>
                  </tr>
                ))}
                <tr className="font-semibold">
                  <td className="py-2">Итого</td><td>{history.total.unitsSold}</td>
                  <td>{formatMoney(history.total.revenue)}</td><td>{formatMoney(history.total.cost)}</td><td>{formatMoney(history.total.grossProfit)}</td>
                </tr>
              </tbody>
            </table>
          </div>
        )}
      </Modal>
    </div>
  );
}