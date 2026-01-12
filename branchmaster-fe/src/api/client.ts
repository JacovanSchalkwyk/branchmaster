const API_BASE = "http://localhost:8080/api";

let staffToken: string | null = null;

export function setStaffToken(token: string | null) {
  staffToken = token;
}

function buildHeaders(extra?: HeadersInit): HeadersInit {
  const base: Record<string, string> = {
    "Content-Type": "application/json",
  };

  if (staffToken) {
    base["Authorization"] = `Bearer ${staffToken}`;
  }

  return {
    ...base,
    ...(extra as Record<string, string> | undefined),
  };
}

async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`API ${res.status}: ${text}`);
  }

  if (res.status === 204) {
    return undefined as T;
  }

  const text = await res.text();
  if (!text) return undefined as T;

  return JSON.parse(text) as T;
}

export async function getApi<T>(path: string, opts?: { headers?: HeadersInit }): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: buildHeaders(opts?.headers),
  });
  return handleResponse<T>(res);
}

export async function postApi<TResponse, TBody = unknown>(
  path: string,
  body: TBody,
  opts?: { headers?: HeadersInit }
): Promise<TResponse> {
  const res = await fetch(`${API_BASE}${path}`, {
    method: "POST",
    headers: buildHeaders(opts?.headers),
    body: JSON.stringify(body),
  });

  return handleResponse<TResponse>(res);
}

export async function putApi<TResponse, TBody = unknown>(
  path: string,
  body: TBody,
  opts?: { headers?: HeadersInit }
): Promise<TResponse> {
  const res = await fetch(`${API_BASE}${path}`, {
    method: "PUT",
    headers: buildHeaders(opts?.headers),
    body: JSON.stringify(body),
  });

  return handleResponse<TResponse>(res);
}

export async function delApi<TResponse = void>(
  path: string,
  opts?: { headers?: HeadersInit }
): Promise<TResponse> {
  const res = await fetch(`${API_BASE}${path}`, {
    method: "DELETE",
    headers: buildHeaders(opts?.headers),
  });

  return handleResponse<TResponse>(res);
}
