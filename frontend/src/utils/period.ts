export type PresetKey =
  | "thisMonth" | "prevMonth" | "last7" | "last30" | "last3m" | "last6m" | "last12m"
  | "thisQuarter" | "prevQuarter" | "custom";

export interface Period { from: string; to: string; compareFrom?: string; compareTo?: string }

export const PRESETS: { key: PresetKey; label: string }[] = [
  { key: "thisMonth", label: "Текущий месяц" },
  { key: "prevMonth", label: "Прошлый месяц" },
  { key: "last7", label: "Последние 7 дней" },
  { key: "last30", label: "Последние 30 дней" },
  { key: "last3m", label: "Последние 3 месяца (скользящие)" },
  { key: "last6m", label: "Последние 6 месяцев" },
  { key: "last12m", label: "Последние 12 месяцев" },
  { key: "thisQuarter", label: "Текущий календарный квартал" },
  { key: "prevQuarter", label: "Прошлый календарный квартал" },
  { key: "custom", label: "Произвольный период" },
];

const pad = (n: number) => String(n).padStart(2, "0");
export const iso = (d: Date) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
const lastDay = (y: number, m: number) => new Date(y, m + 1, 0);

function addMonths(d: Date, n: number): Date {
  const target = new Date(d.getFullYear(), d.getMonth() + n, 1);
  return new Date(target.getFullYear(), target.getMonth(), Math.min(d.getDate(), lastDay(target.getFullYear(), target.getMonth()).getDate()));
}
function addDays(d: Date, n: number): Date { return new Date(d.getFullYear(), d.getMonth(), d.getDate() + n); }
const monthRange = (y: number, m: number) => ({ from: iso(new Date(y, m, 1)), to: iso(lastDay(y, m)) });
const quarterRange = (y: number, q: number) => ({ from: iso(new Date(y, q * 3, 1)), to: iso(lastDay(y, q * 3 + 2)) });

export function buildPeriod(key: PresetKey, customFrom: string, customTo: string, now = new Date()): Period | null {
  const y = now.getFullYear(), m = now.getMonth(), q = Math.floor(m / 3);
  switch (key) {
    case "thisMonth": {
      const p = new Date(y, m - 1, 1);
      return { ...monthRange(y, m), compareFrom: monthRange(p.getFullYear(), p.getMonth()).from, compareTo: monthRange(p.getFullYear(), p.getMonth()).to };
    }
    case "prevMonth": {
      const p = new Date(y, m - 1, 1), pp = new Date(y, m - 2, 1);
      return { ...monthRange(p.getFullYear(), p.getMonth()), compareFrom: monthRange(pp.getFullYear(), pp.getMonth()).from, compareTo: monthRange(pp.getFullYear(), pp.getMonth()).to };
    }
    case "thisQuarter": {
      const pq = q === 0 ? { y: y - 1, q: 3 } : { y, q: q - 1 };
      const c = quarterRange(pq.y, pq.q);
      return { ...quarterRange(y, q), compareFrom: c.from, compareTo: c.to };
    }
    case "prevQuarter": {
      const pq = q === 0 ? { y: y - 1, q: 3 } : { y, q: q - 1 };
      const ppq = pq.q === 0 ? { y: pq.y - 1, q: 3 } : { y: pq.y, q: pq.q - 1 };
      const c = quarterRange(ppq.y, ppq.q);
      return { ...quarterRange(pq.y, pq.q), compareFrom: c.from, compareTo: c.to };
    }
    case "last7": return { from: iso(addDays(now, -6)), to: iso(now) };
    case "last30": return { from: iso(addDays(now, -29)), to: iso(now) };
    case "last3m": return { from: iso(addDays(addMonths(now, -3), 1)), to: iso(now) };
    case "last6m": return { from: iso(addDays(addMonths(now, -6), 1)), to: iso(now) };
    case "last12m": return { from: iso(addDays(addMonths(now, -12), 1)), to: iso(now) };
    case "custom":
      return customFrom && customTo && customFrom <= customTo ? { from: customFrom, to: customTo } : null;
  }
}