export interface Expense {
  id: string;
  amount: number;
  title: string | null;
  description: string | null;
  userName: string | null;
  createdAt: string;
}

export interface CreateExpenseRequest {
  amount: number;
  title?: string;
  description?: string;
}