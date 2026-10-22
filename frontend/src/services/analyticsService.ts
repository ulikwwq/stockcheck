import { apiRequest } from "./apiClient";
import type { MonthPoint, ProductHistory, Summary, TopProduct, TopSort } from "../types/analytics";
import type { Period } from "../utils/period";

const q = (p: Period) => ({ from: p.from, to: p.to, compareFrom: p.compareFrom, compareTo: p.compareTo });

export const analyticsService = {
  summary: (p: Period) => apiRequest<Summary>("/analytics/summary", { query: q(p) }),
  monthly: (p: Period) => apiRequest<MonthPoint[]>("/analytics/monthly", { query: { from: p.from, to: p.to } }),
  topProducts: (p: Period, sort: TopSort, query: string) =>
    apiRequest<TopProduct[]>("/analytics/top-products", { query: { ...q(p), sort, query } }),
  productHistory: (id: string, p: Period) =>
    apiRequest<ProductHistory>(`/analytics/products/${id}/history`, { query: { from: p.from, to: p.to } }),
};