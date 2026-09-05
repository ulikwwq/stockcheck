import { apiRequest } from "./apiClient";
import type { CreateSaleRequest, Sale } from "../types/sale";

export const saleService = {
  list(shopId?: string): Promise<Sale[]> {
    return apiRequest<Sale[]>("/sales", { query: { shopId } });
  },

  get(id: string): Promise<Sale> {
    return apiRequest<Sale>(`/sales/${id}`);
  },

  create(request: CreateSaleRequest): Promise<Sale> {
    return apiRequest<Sale>("/sales", { method: "POST", body: request });
  },
};
