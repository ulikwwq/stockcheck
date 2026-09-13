import { apiRequest, apiRequestBlob, apiRequestMultipart } from "./apiClient";
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

  uploadImage(id: string, file: File): Promise<Product> {
    const formData = new FormData();
    formData.append("file", file);
    return apiRequestMultipart<Product>(`/products/${id}/image`, formData);
  },

  deleteImage(id: string): Promise<void> {
    return apiRequestMultipart<void>(`/products/${id}/image`, new FormData(), "DELETE");
  },

  downloadInventoryReportPdf(): Promise<Blob> {
    return apiRequestBlob("/products/report/pdf");
  },
};

