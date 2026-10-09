import { apiRequest } from "./apiClient";
import type { CreateExpenseRequest, Expense } from "../types/expense";

export const expenseService = {
  list(): Promise<Expense[]> {
    return apiRequest<Expense[]>("/expenses");
  },

  create(request: CreateExpenseRequest): Promise<Expense> {
    return apiRequest<Expense>("/expenses", {
      method: "POST",
      body: request,
    });
  },
};