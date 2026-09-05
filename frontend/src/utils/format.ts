const numberFormatter = new Intl.NumberFormat("ru-RU", {
  maximumFractionDigits: 2,
});

const dateFormatter = new Intl.DateTimeFormat("ru-RU", {
  day: "2-digit",
  month: "2-digit",
  year: "numeric",
});

const timeFormatter = new Intl.DateTimeFormat("ru-RU", {
  hour: "2-digit",
  minute: "2-digit",
});

/** All amounts in this business are Kyrgyzstani som ("сом"). */
export function formatMoney(value: number | null | undefined): string {
  if (value === null || value === undefined) return "—";
  return `${numberFormatter.format(value)} сом`;
}

export function formatNumber(value: number | null | undefined): string {
  if (value === null || value === undefined) return "—";
  return numberFormatter.format(value);
}

export function formatDate(value: string | null | undefined): string {
  if (!value) return "—";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "—";
  return dateFormatter.format(date);
}

export function formatDateTime(value: string | null | undefined): string {
  if (!value) return "—";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "—";

  const today = new Date();
  const isToday = date.toDateString() === today.toDateString();
  const time = timeFormatter.format(date);

  return isToday ? `Сегодня, ${time}` : `${dateFormatter.format(date)}, ${time}`;
}
