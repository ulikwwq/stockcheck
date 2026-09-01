export interface SaleItem {
  id: string;
  productId: string;
  productName: string;
  quantity: number;
  // purchasePrice / profit are only populated for ADMINISTRATOR / SUPER_ADMIN.
  purchasePrice: number | null;
  salePrice: number;
  profit: number | null;
  createdAt: string;
}

export interface Sale {
  id: string;
  shopId: string;
  shopName: string | null;
  sellerId: string;
  sellerName: string | null;
  totalAmount: number;
  items: SaleItem[];
  createdAt: string;
}

export interface CreateSaleItemRequest {
  productId: string;
  quantity: number;
  customSalePrice?: number;
}

// No shopId: the backend resolves the business's single shop automatically.
export interface CreateSaleRequest {
  items: CreateSaleItemRequest[];
}
