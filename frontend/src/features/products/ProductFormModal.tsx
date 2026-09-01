import { useEffect, useState, type FormEvent } from "react";
import { Modal } from "../../components/Modal";
import { FormField, inputClass } from "../../components/FormField";
import { Button } from "../../components/Button";
import { Alert } from "../../components/Alert";
import { productService } from "../../services/productService";
import { ApiError } from "../../services/apiClient";
import type { Product } from "../../types/product";

interface ProductFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSaved: () => void;
  product?: Product | null;
}

export function ProductFormModal({ isOpen, onClose, onSaved, product }: ProductFormModalProps) {
  const isEditing = !!product;

  const [name, setName] = useState("");
  const [quantity, setQuantity] = useState("0");
  const [purchasePrice, setPurchasePrice] = useState("");
  const [defaultSalePrice, setDefaultSalePrice] = useState("");
  const [imageUrl, setImageUrl] = useState("");

  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    if (!isOpen) return;
    setName(product?.name ?? "");
    setQuantity(product ? String(product.quantity) : "");
    setPurchasePrice(product?.purchasePrice != null ? String(product.purchasePrice) : "");
    setDefaultSalePrice(product?.defaultSalePrice != null ? String(product.defaultSalePrice) : "");
    setImageUrl(product?.imageUrl ?? "");
    setError(null);
  }, [isOpen, product]);

  function validate(): string | null {
    if (!name.trim()) return "Введите название товара";
    if (quantity.trim() === "" || Number(quantity) < 0 || !Number.isFinite(Number(quantity))) {
      return "Количество должно быть больше или равно нулю";
    }
    if (purchasePrice.trim() !== "" && (Number.isNaN(Number(purchasePrice)) || Number(purchasePrice) < 0)) {
      return "Цена закупки указана неверно";
    }
    if (
      defaultSalePrice.trim() !== "" &&
      (Number.isNaN(Number(defaultSalePrice)) || Number(defaultSalePrice) < 0)
    ) {
      return "Цена продажи указана неверно";
    }
    return null;
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }

    setError(null);
    setIsSubmitting(true);
    try {
      const payload = {
        name: name.trim(),
        quantity: Number(quantity),
        purchasePrice: purchasePrice.trim() !== "" ? Number(purchasePrice) : undefined,
        defaultSalePrice: defaultSalePrice.trim() !== "" ? Number(defaultSalePrice) : undefined,
        imageUrl: imageUrl.trim() || undefined,
      };

      if (isEditing && product) {
        await productService.update(product.id, payload);
      } else {
        await productService.create(payload);
      }
      onSaved();
      onClose();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Не удалось сохранить товар");
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleDelete() {
    if (!product) return;
    if (!window.confirm(`Удалить товар «${product.name}»?`)) return;
    setIsDeleting(true);
    try {
      await productService.update(product.id, { active: false });
      onSaved();
      onClose();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Не удалось удалить товар");
    } finally {
      setIsDeleting(false);
    }
  }

  return (
    <Modal title={isEditing ? "Изменить товар" : "Добавить товар"} isOpen={isOpen} onClose={onClose}>
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        {error && <Alert variant="error">{error}</Alert>}

        <FormField label="Название товара" htmlFor="p-name" required>
          <input
            id="p-name"
            required
            value={name}
            onChange={(e) => setName(e.target.value)}
            className={inputClass}
            placeholder="Кофе"
          />
        </FormField>

        <FormField label="Количество" htmlFor="p-qty" required>
          <input
            id="p-qty"
            type="number"
            inputMode="numeric"
            min="0"
            required
            value={quantity}
            onChange={(e) => setQuantity(e.target.value)}
            className={inputClass}
            placeholder="20"
          />
        </FormField>

        <div className="grid grid-cols-2 gap-3">
          <FormField label="Цена закупки" htmlFor="p-purchase">
            <input
              id="p-purchase"
              type="number"
              inputMode="decimal"
              min="0"
              step="0.01"
              value={purchasePrice}
              onChange={(e) => setPurchasePrice(e.target.value)}
              className={inputClass}
              placeholder="150"
            />
          </FormField>
          <FormField label="Цена продажи" htmlFor="p-sale">
            <input
              id="p-sale"
              type="number"
              inputMode="decimal"
              min="0"
              step="0.01"
              value={defaultSalePrice}
              onChange={(e) => setDefaultSalePrice(e.target.value)}
              className={inputClass}
              placeholder="200"
            />
          </FormField>
        </div>

        <FormField label="Фотография (ссылка)" htmlFor="p-image">
          <input
            id="p-image"
            value={imageUrl}
            onChange={(e) => setImageUrl(e.target.value)}
            className={inputClass}
            placeholder="https://…"
          />
        </FormField>

        <div className="mt-1 flex flex-col gap-2">
          <Button type="submit" isLoading={isSubmitting} className="w-full">
            Сохранить
          </Button>
          {isEditing && (
            <Button
              type="button"
              variant="danger"
              isLoading={isDeleting}
              onClick={handleDelete}
              className="w-full"
            >
              Удалить товар
            </Button>
          )}
          <Button type="button" variant="ghost" onClick={onClose} className="w-full">
            Отмена
          </Button>
        </div>
      </form>
    </Modal>
  );
}
