import { useEffect, useRef, useState } from "react";
import { Button } from "../../components/Button";
import { Alert } from "../../components/Alert";
import { productService } from "../../services/productService";
import { ApiError } from "../../services/apiClient";

const MAX_BYTES = 8 * 1024 * 1024; // keep in sync with backend ProductImageService
const ALLOWED_TYPES = ["image/jpeg", "image/png", "image/webp"];

interface ProductPhotoFieldProps {
  /** Existing product id, if editing. Undefined while creating a new product. */
  productId?: string;
  /** Current signed image URL from the backend, if the product already has a photo. */
  currentImageUrl: string | null;
  /** Called with the picked File so the parent can upload it after save. */
  onFileSelected: (file: File | null) => void;
  /** Called after an immediate delete succeeds, so the parent can refresh its data. */
  onImageDeleted?: () => void;
}

export function ProductPhotoField({
  productId,
  currentImageUrl,
  onFileSelected,
  onImageDeleted,
}: ProductPhotoFieldProps) {
  const [stagedFile, setStagedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const galleryInputRef = useRef<HTMLInputElement>(null);
  const cameraInputRef = useRef<HTMLInputElement>(null);

  // Local object URL for a staged (not-yet-uploaded) file - revoke it
  // whenever it changes or the component unmounts, to avoid leaking memory.
  useEffect(() => {
    if (!stagedFile) {
      setPreviewUrl(null);
      return;
    }
    const url = URL.createObjectURL(stagedFile);
    setPreviewUrl(url);
    return () => URL.revokeObjectURL(url);
  }, [stagedFile]);

  function validateAndStage(file: File | undefined) {
    if (!file) return;
    setError(null);

    if (!ALLOWED_TYPES.includes(file.type)) {
      setError("Поддерживаются только изображения формата JPEG, PNG или WebP");
      return;
    }
    if (file.size > MAX_BYTES) {
      setError("Размер изображения не должен превышать 8 МБ");
      return;
    }

    setStagedFile(file);
    onFileSelected(file);
  }

  async function handleDeleteExisting() {
    if (!productId) return;
    if (!window.confirm("Удалить фотографию товара?")) return;

    setError(null);
    setIsDeleting(true);
    try {
      await productService.deleteImage(productId);
      onImageDeleted?.();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Не удалось удалить фотографию");
    } finally {
      setIsDeleting(false);
    }
  }

  function clearStaged() {
    setStagedFile(null);
    onFileSelected(null);
    if (galleryInputRef.current) galleryInputRef.current.value = "";
    if (cameraInputRef.current) cameraInputRef.current.value = "";
  }

  const displayUrl = previewUrl ?? currentImageUrl;

  return (
    <div className="flex flex-col gap-2.5">
      <label className="text-sm font-medium text-slate-700">Фотография</label>

      {error && <Alert variant="error">{error}</Alert>}

      <div className="flex items-center gap-3">
        {displayUrl ? (
          <img
            src={displayUrl}
            alt="Товар"
            className="h-20 w-20 shrink-0 rounded-xl border border-slate-200 object-cover"
          />
        ) : (
          <div className="flex h-20 w-20 shrink-0 items-center justify-center rounded-xl border border-dashed border-slate-300 text-xs text-slate-400">
            Нет фото
          </div>
        )}

        <div className="flex flex-1 flex-col gap-2">
          <div className="flex flex-wrap gap-2">
            <Button
              type="button"
              variant="secondary"
              onClick={() => galleryInputRef.current?.click()}
              className="!min-h-9 !px-3 !py-1.5 text-sm"
            >
              Выбрать из галереи
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={() => cameraInputRef.current?.click()}
              className="!min-h-9 !px-3 !py-1.5 text-sm"
            >
              Сделать фото
            </Button>
          </div>

          {(stagedFile || (currentImageUrl && productId)) && (
            <div className="flex flex-wrap gap-2">
              {stagedFile && (
                <button
                  type="button"
                  onClick={clearStaged}
                  className="text-left text-xs font-medium text-slate-500 underline-offset-2 active:underline"
                >
                  Отменить выбор
                </button>
              )}
              {!stagedFile && currentImageUrl && productId && (
                <button
                  type="button"
                  disabled={isDeleting}
                  onClick={handleDeleteExisting}
                  className="text-left text-xs font-medium text-red-600 underline-offset-2 active:underline disabled:opacity-50"
                >
                  {isDeleting ? "Удаление…" : "Удалить фотографию"}
                </button>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Plain gallery/file picker - works identically on desktop and mobile. */}
      <input
        ref={galleryInputRef}
        type="file"
        accept="image/jpeg,image/png,image/webp"
        className="hidden"
        onChange={(e) => validateAndStage(e.target.files?.[0])}
      />
      {/* capture="environment" opens the rear camera on mobile browsers that
          support it; browsers that don't (most desktops) simply fall back
          to the normal file picker, which is the graceful degradation the
          spec asks for. */}
      <input
        ref={cameraInputRef}
        type="file"
        accept="image/jpeg,image/png,image/webp"
        capture="environment"
        className="hidden"
        onChange={(e) => validateAndStage(e.target.files?.[0])}
      />
    </div>
  );
}
