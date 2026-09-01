import { apiRequest } from "./apiClient";
import type { DailyProfit, ProfitSummary } from "../types/profit";

export const profitService = {
  summary(): Promise<ProfitSummary> {
    return apiRequest<ProfitSummary>("/profit");
  },

  daily(): Promise<DailyProfit[]> {
    return apiRequest<DailyProfit[]>("/profit/daily");
  },
};
