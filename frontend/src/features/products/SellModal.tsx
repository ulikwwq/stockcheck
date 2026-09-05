import { useEffect, useState } from "react";
import { Modal } from "../../components/Modal";
import { Button } from "../../components/Button";
import { Alert } from "../../components/Alert";
import { FormField, inputClass } from "../../components/FormField";
import { saleService } from "../../services/saleService";
import { ApiError } from "../../services/apiClient";
import { formatMoney } from "../../utils/format";
import type { Product } from "../../types/product";

interface SellModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSold: () => void;
  onEdit?: () => void;
  product: Product | null;
  canEdit: boolean;
}

export function SellModal({ isOpen, onClose, onSold, onEdit, product, canEdit }: SellModalProps) {
  const [quantity, setQuantity] = useState(1);
  const [showCustomPrice, setShowCustomPrice] = useState(false);
  const [customPrice, setCustomPrice] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (!isOpen) return;
    // Quantity always defaults to 1 per sale, per spec.
    setQuantity(1);
    setShowCustomPrice(false);
    setCustomPrice("");
    setError(null);
    setSuccess(null);
  }, [isOpen, product?.id]);

  if (!product) return null;

  const outOfStock = product.quantity <= 0;
  const maxQuantity = product.quantity;

  function clampQuantity(value: number): number {
    if (Number.isNaN(value)) return 1;
    return Math.min(Math.max(1, Math.trunc(value)), Math.max(1, maxQuantity));
  }

  async function sell(customSalePrice?: number) {
    if (!product) return;
    if (quantity < 1 || quantity > maxQuantity) {
      setError("Недостаточно товара на складе");
      return;
    }

    setError(null);
    setIsSubmitting(true);
    try {
      await saleService.create({
        items: [{ productId: product.id, quantity, customSalePrice }],
      });
      const unitPrice = customSalePrice ?? product.defaultSalePrice ?? undefined;
      setSuccess(unitPrice != null ? `Продано за ${formatMoney(unitPrice * quantity)}` : "Продано");
      onSold();
      setTimeout(() => {
        onClose();
      }, 700);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Не удалось выполнить продажу");
    } finally {
      setIsSubmitting(false);
    }
  }

  function handleCustomSubmit() {
    const value = Number(customPrice);
    if (customPrice.trim() === "" || Number.isNaN(value) || value < 0) {
      setError("Цена указана неверно");
      return;
    }
    void sell(value);
  }

  return (
    <Modal title={product.name} isOpen={isOpen} onClose={onClose}>
      <div className="flex flex-col gap-4">
        {error && <Alert variant="error">{error}</Alert>}
        {success && <Alert variant="success">{success}</Alert>}

        <div className="rounded-xl bg-slate-50 p-4">
          <p className="text-sm text-slate-500">
            Остаток: <span className="font-medium text-slate-900">{product.quantity} шт.</span>
          </p>
          {product.defaultSalePrice != null && (
            <p className="mt-1 text-sm text-slate-500">
              Цена: <span className="font-medium text-slate-900">{formatMoney(product.defaultSalePrice)}</span>
            </p>
          )}
        </div>

        {outOfStock ? (
          <Alert variant="info">Товар закончился</Alert>
        ) : (
          <>
            <FormField label="Количество" htmlFor="sell-quantity">
              <div className="flex items-center gap-3">
                <button
                  type="button"
                  disabled={isSubmitting || quantity <= 1}
                  onClick={() => setQuantity((q) => clampQuantity(q - 1))}
                  className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-slate-300 text-lg font-semibold text-slate-700 active:bg-slate-100 disabled:opacity-40"
                  aria-label="Уменьшить количество"
                >
                  −
                </button>
                <input
                  id="sell-quantity"
                  type="number"
                  inputMode="numeric"
                  min={1}
                  max={maxQuantity}
                  value={quantity}
                  onChange={(e) => setQuantity(clampQuantity(Number(e.target.value)))}
                  className={`${inputClass} w-full text-center`}
                />
                <button
                  type="button"
                  disabled={isSubmitting || quantity >= maxQuantity}
                  onClick={() => setQuantity((q) => clampQuantity(q + 1))}
                  className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-slate-300 text-lg font-semibold text-slate-700 active:bg-slate-100 disabled:opacity-40"
                  aria-label="Увеличить количество"
                >
                  +
                </button>
              </div>
            </FormField>

            {!showCustomPrice ? (
              <div className="flex flex-col gap-2">
                {product.defaultSalePrice != null ? (
                  <Button
                    type="button"
                    isLoading={isSubmitting}
                    onClick={() => sell(undefined)}
                    className="w-full"
                  >
                    Продать за {formatMoney(product.defaultSalePrice * quantity)}
                  </Button>
                ) : (
                  // Product has no configured sale price - selling it must
                  // not force the seller to type one in.
                  <Button
                    type="button"
                    isLoading={isSubmitting}
                    onClick={() => sell(undefined)}
                    className="w-full"
                  >
                    Продано
                  </Button>
                )}
                <Button
                  type="button"
                  variant="secondary"
                  disabled={isSubmitting}
                  onClick={() => setShowCustomPrice(true)}
                  className="w-full"
                >
                  Указать цену
                </Button>
              </div>
            ) : (
              <div className="flex flex-col gap-3">
                <FormField label="Цена за единицу" htmlFor="custom-price" required>
                  <input
                    id="custom-price"
                    type="number"
                    inputMode="decimal"
                    min="0"
                    step="0.01"
                    autoFocus
                    value={customPrice}
                    onChange={(e) => setCustomPrice(e.target.value)}
                    className={inputClass}
                    placeholder="180"
                  />
                </FormField>
                <Button type="button" isLoading={isSubmitting} onClick={handleCustomSubmit} className="w-full">
                  Продать
                </Button>
                <Button
                  type="button"
                  variant="ghost"
                  disabled={isSubmitting}
                  onClick={() => setShowCustomPrice(false)}
                  className="w-full"
                >
                  Назад
                </Button>
              </div>
            )}
          </>
        )}

        {canEdit && onEdit && (
          <button
            type="button"
            onClick={onEdit}
            className="mt-1 text-center text-sm font-medium text-slate-500 underline-offset-2 active:underline"
          >
            Изменить товар
          </button>
        )}
      </div>
    </Modal>
  );
}
