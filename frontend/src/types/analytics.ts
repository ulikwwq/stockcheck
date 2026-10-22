export interface Totals {
  revenue: number; cost: number; grossProfit: number; expenses: number; operatingResult: number;
  salesCount: number; unitsSold: number; unknownCostUnits: number;
}
export interface Summary { from: string; to: string; previousFrom: string; previousTo: string; current: Totals; previous: Totals }
export interface MonthPoint {
  month: string; revenue: number; cost: number; grossProfit: number; expenses: number;
  operatingResult: number; salesCount: number; unitsSold: number;
}
export interface TopProduct {
  productId: string; name: string; sku: string | null; unitsSold: number; revenue: number; cost: number;
  grossProfit: number; salesCount: number; lastSaleAt: string; previousUnitsSold: number; changePercent: number | null;
}
export interface ProductMonth { month: string | null; unitsSold: number; revenue: number; cost: number; grossProfit: number }
export interface ProductHistory { productId: string; name: string; months: ProductMonth[]; total: ProductMonth }
export type TopSort = "UNITS" | "REVENUE" | "PROFIT" | "SALES" | "TREND";