const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1";

const TOKEN_STORAGE_KEY = "stockcheck.accessToken";

export class ApiError extends Error {
  status: number;
  path?: string;

  constructor(status: number, message: string, path?: string) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.path = path;
  }
}

export function getStoredToken(): string | null {
  return localStorage.getItem(TOKEN_STORAGE_KEY);
}

export function setStoredToken(token: string | null): void {
  if (token) {
    localStorage.setItem(TOKEN_STORAGE_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
  }
}

// Notified when the backend rejects the current token (expired/invalid),
// so the app can log the user out and return them to /login.
type UnauthorizedListener = () => void;
let unauthorizedListener: UnauthorizedListener | null = null;

export function onUnauthorized(listener: UnauthorizedListener): void {
  unauthorizedListener = listener;
}

interface RequestOptions {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  query?: Record<string, string | undefined | null>;
  skipAuth?: boolean;
}

function buildUrl(path: string, query?: RequestOptions["query"]): string {
  const url = new URL(`${API_BASE_URL}${path}`);
  if (query) {
    for (const [key, value] of Object.entries(query)) {
      if (value !== undefined && value !== null && value !== "") {
        url.searchParams.set(key, value);
      }
    }
  }
  return url.toString();
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = "GET", body, query, skipAuth = false } = options;

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };

  if (!skipAuth) {
    const token = getStoredToken();
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
  }

  let response: Response;
  try {
    response = await fetch(buildUrl(path, query), {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError(0, "Could not reach the server. Check that the backend is running.");
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const isJson = response.headers.get("content-type")?.includes("application/json");
  const payload = isJson ? await response.json().catch(() => null) : null;

  if (!response.ok) {
    if (response.status === 401 && !skipAuth) {
      unauthorizedListener?.();
    }
    const message =
      (payload && (payload.message || payload.error)) || response.statusText || "Request failed";
    throw new ApiError(response.status, message, payload?.path);
  }

  return payload as T;
}

/**
 * Like apiRequest, but for binary responses (e.g. a PDF) that must not be
 * parsed as JSON. Reuses the same base URL, auth token, and 401 handling.
 */
export async function apiRequestBlob(path: string, query?: RequestOptions["query"]): Promise<Blob> {
  const headers: Record<string, string> = {};
  const token = getStoredToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  let response: Response;
  try {
    response = await fetch(buildUrl(path, query), { method: "GET", headers });
  } catch {
    throw new ApiError(0, "Could not reach the server. Check that the backend is running.");
  }

  if (!response.ok) {
    if (response.status === 401) {
      unauthorizedListener?.();
    }
    let message = response.statusText || "Request failed";
    if (response.headers.get("content-type")?.includes("application/json")) {
      const payload = await response.json().catch(() => null);
      message = (payload && (payload.message || payload.error)) || message;
    }
    throw new ApiError(response.status, message);
  }

  return response.blob();
}
