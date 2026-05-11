type ApiErrorShape = {
  response?: {
    status?: number;
    data?: {
      message?: string;
    };
  };
  message?: string;
};

type ApiErrorOptions = {
  unauthorizedMessage?: string;
};

export function getApiErrorMessage(
  err: unknown,
  fallback: string,
  options?: ApiErrorOptions
): string | null {
  if (!err) return null;

  const error = err as ApiErrorShape;
  const status = error.response?.status;

  if (status === 401) {
    return options?.unauthorizedMessage ?? "Oturumun süresi dolmuş olabilir. Lütfen tekrar giriş yap.";
  }
  if (status === 403) return "Bu işlem için yetkin yok.";
  if (status === 404) return "İstenen kaynak bulunamadı.";
  if (status === 413) return "Dosya çok büyük. Daha küçük bir PDF dene.";
  if (status === 415) return "Desteklenmeyen dosya türü.";

  return error.response?.data?.message || error.message || fallback;
}