import { useEffect, useState, type FormEvent } from "react";
import { Modal } from "../../components/Modal";
import { FormField, inputClass } from "../../components/FormField";
import { Button } from "../../components/Button";
import { Alert } from "../../components/Alert";
import { ApiError } from "../../services/apiClient";
import { expenseService } from "../../services/expenseService";

interface ExpenseFormModalProps {
isOpen: boolean;
onClose: () => void;
onSaved: () => void;
}

export function ExpenseFormModal({
isOpen,
onClose,
onSaved,
}: ExpenseFormModalProps) {
const [amount, setAmount] = useState("");
const [title, setTitle] = useState("");
const [description, setDescription] = useState("");
const [error, setError] = useState<string | null>(null);
const [isSubmitting, setIsSubmitting] = useState(false);

useEffect(() => {
if (!isOpen) return;

setAmount("");
setTitle("");
setDescription("");
setError(null);

}, [isOpen]);

async function handleSubmit(e: FormEvent<HTMLFormElement>) {
e.preventDefault();

const numericAmount = Number(amount);

if (
  amount.trim() === "" ||
  !Number.isFinite(numericAmount) ||
  numericAmount <= 0
) {
  setError("Введите сумму расхода больше нуля");
  return;
}

if (!/^\d+(\.\d{1,2})?$/.test(amount.trim())) {
  setError("Сумма должна содержать не более двух знаков после запятой");
  return;
}

setError(null);
setIsSubmitting(true);

try {
  await expenseService.create({
    amount: numericAmount,
    title: title.trim() || undefined,
    description: description.trim() || undefined,
  });

  onSaved();
  onClose();
} catch (err) {
  setError(
    err instanceof ApiError
      ? err.message
      : "Не удалось добавить расход",
  );
} finally {
  setIsSubmitting(false);
}

}

return (
<Modal title="Добавить расход" isOpen={isOpen} onClose={onClose}>
<form onSubmit={handleSubmit} className="flex flex-col gap-4">
{error && <Alert variant="error">{error}</Alert>}

    <FormField label="Сумма расхода" htmlFor="expense-amount" required>
      <input
        id="expense-amount"
        type="number"
        inputMode="decimal"
        min="0.01"
        step="0.01"
        required
        autoFocus
        value={amount}
        onChange={(e) => setAmount(e.target.value)}
        className={inputClass}
        placeholder="500"
      />
    </FormField>

    <FormField label="Название" htmlFor="expense-title">
      <input
        id="expense-title"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        maxLength={255}
        className={inputClass}
        placeholder="Например, аренда помещения"
      />
    </FormField>

    <FormField label="Описание" htmlFor="expense-description">
      <textarea
        id="expense-description"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        maxLength={5000}
        rows={3}
        className={inputClass}
        placeholder="Дополнительная информация"
      />
    </FormField>

    <div className="mt-1 flex flex-col gap-2">
      <Button
        type="submit"
        isLoading={isSubmitting}
        className="w-full"
      >
        Сохранить расход
      </Button>

      <Button
        type="button"
        variant="ghost"
        onClick={onClose}
        className="w-full"
      >
        Отмена
      </Button>
    </div>
  </form>
</Modal>

);
}