import { useCallback, useEffect, useState } from "react";
import { useAuth } from "../../app/AuthContext";
import { isOwner } from "../../app/navigation";
import { productService } from "../../services/productService";
import { ApiError } from "../../services/apiClient";
import type { Product } from "../../types/product";
import { Button } from "../../components/Button";
import { LoadingState, EmptyState, ErrorState } from "../../components/DataStates";
import { Alert } from "../../components/Alert";
import { formatMoney } from "../../utils/format";
import { ProductFormModal } from "./ProductFormModal";
import { SellModal } from "./SellModal";

export function ProductsPage() {
  const { user } = useAuth();
  const owner = isOwner(user?.roles ?? []);

  const [products, setProducts] = useState<Product[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);

  const [sellingProduct, setSellingProduct] = useState<Product | null>(null);

  const [isPdfLoading, setIsPdfLoading] = useState(false);
  const [pdfError, setPdfError] = useState<string | null>(null);

  const load = useCallback(() => {
    setIsLoading(true);
    setError(null);
    productService
      .list()
      .then(setProducts)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Не удалось загрузить товары"))
      .finally(() => setIsLoading(false));
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  function openCreateModal() {
    setEditingProduct(null);
    setIsFormOpen(true);
  }

  function openEditModal(product: Product) {
    setSellingProduct(null);
    setEditingProduct(product);
    setIsFormOpen(true);
  }

  async function handleOpenPdfReport() {
    setPdfError(null);
    setIsPdfLoading(true);
    try {
      const blob = await productService.downloadInventoryReportPdf();
      const url = URL.createObjectURL(blob);
      // Opens in a new tab where the browser's own PDF viewer provides
      // both a preview and a download button - no extra UI needed here.
      window.open(url, "_blank");
      setTimeout(() => URL.revokeObjectURL(url), 60_000);
    } catch (err) {
      setPdfError(err instanceof ApiError ? err.message : "Не удалось сформировать отчет");
    } finally {
      setIsPdfLoading(false);
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between gap-3">
        <h1 className="text-xl font-bold text-slate-900">Товары</h1>
        <Button
          type="button"
          variant="secondary"
          isLoading={isPdfLoading}
          onClick={handleOpenPdfReport}
          className="!min-h-9 !px-3 !py-1.5 text-sm"
        >
          PDF
        </Button>
      </div>

      {pdfError && <Alert variant="error">{pdfError}</Alert>}

      {isLoading ? (
        <LoadingState label="Загрузка товаров…" />
      ) : error ? (
        <ErrorState message={error} />
      ) : products.length === 0 ? (
        <EmptyState
          title="Товаров пока нет"
          description={owner ? "Добавьте первый товар, чтобы начать работу." : undefined}
          action={
            owner ? (
              <Button onClick={openCreateModal}>+ Добавить товар</Button>
            ) : undefined
          }
        />
      ) : (
        <div className="flex flex-col gap-2.5">
          {products.map((product) => (
            <button
              key={product.id}
              type="button"
              onClick={() => setSellingProduct(product)}
              className="flex items-center justify-between gap-3 rounded-2xl border border-slate-200 bg-white p-4 text-left shadow-sm active:bg-slate-50"
            >
              <div className="min-w-0">
                <p className="truncate text-base font-semibold text-slate-900">{product.name}</p>
                <p
                  className={`mt-0.5 text-sm ${
                    product.quantity <= 0 ? "font-medium text-red-600" : "text-slate-500"
                  }`}
                >
                  {product.quantity <= 0 ? "Товар закончился" : `${product.quantity} шт.`}
                </p>
              </div>
              <p className="shrink-0 text-base font-semibold text-slate-900">
                {formatMoney(product.defaultSalePrice)}
              </p>
            </button>
          ))}
        </div>
      )}

      {owner && products.length > 0 && (
        <div className="fixed inset-x-0 bottom-20 z-20 flex justify-center px-4">
          <Button onClick={openCreateModal} className="w-full max-w-lg shadow-lg">
            + Добавить товар
          </Button>
        </div>
      )}

      {owner && (
        <ProductFormModal
          isOpen={isFormOpen}
          onClose={() => setIsFormOpen(false)}
          onSaved={load}
          product={editingProduct}
        />
      )}

      <SellModal
        isOpen={!!sellingProduct}
        onClose={() => setSellingProduct(null)}
        onSold={load}
        onEdit={owner && sellingProduct ? () => openEditModal(sellingProduct) : undefined}
        product={sellingProduct}
        canEdit={owner}
      />
    </div>
  );
}
