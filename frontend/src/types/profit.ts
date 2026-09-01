export interface ProfitSummary {
  totalRevenue: number;
  totalCost: number;
  totalProfit: number;
  totalSalesCount: number;
}

export interface DailyProfit {
  date: string;
  revenue: number;
  cost: number;
  profit: number;
  profitPartiallyUnavailable: boolean;
}
