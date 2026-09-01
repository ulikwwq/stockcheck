import { apiRequest } from "./apiClient";
import type { CreateProductRequest, Product, UpdateProductRequest } from "../types/product";

export const productService = {
  list(shopId?: string): Promise<Product[]> {
    return apiRequest<Product[]>("/products", { query: { shopId } });
  },

  get(id: string): Promise<Product> {
    return apiRequest<Product>(`/products/${id}`);
  },

  create(request: CreateProductRequest): Promise<Product> {
    return apiRequest<Product>("/products", { method: "POST", body: request });
  },

  update(id: string, request: UpdateProductRequest): Promise<Product> {
    return apiRequest<Product>(`/products/${id}`, { method: "PUT", body: request });
  },
};
