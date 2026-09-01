// purchasePrice is only populated by the backend for ADMINISTRATOR /
// SUPER_ADMIN callers; SELLER always receives null here.
export interface Product {
  id: string;
  shopId: string;
  shopName: string | null;
  categoryId: string | null;
  categoryName: string | null;
  name: string;
  sku: string | null;
  description: string | null;
  imageUrl: string | null;
  purchasePrice: number | null;
  defaultSalePrice: number | null;
  quantity: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

// Only name + quantity are required per spec. There is no shopId here:
// the backend resolves the business's single shop automatically.
export interface CreateProductRequest {
  name: string;
  sku?: string;
  description?: string;
  imageUrl?: string;
  purchasePrice?: number;
  defaultSalePrice?: number;
  quantity: number;
}

export interface UpdateProductRequest {
  name?: string;
  sku?: string;
  description?: string;
  imageUrl?: string;
  purchasePrice?: number;
  defaultSalePrice?: number;
  quantity?: number;
  active?: boolean;
}
