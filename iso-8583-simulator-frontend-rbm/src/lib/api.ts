const API_BASE = "/api";

export interface ApiTerminal {
  idDB: string;
  terminalID: string;
  delay: number;
  responseCode: string;
}

export interface ApiConfigResponse {
  globalDelay: boolean;
  globalDelayValue: number;
  globalResponseCode: boolean;
  globalResponseCodeValue: string;
  terminals: ApiTerminal[];
}

export async function apiGet<T>(url: string): Promise<T> {
  const res = await fetch(`${API_BASE}${url}`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export async function apiPost(url: string, params?: Record<string, any>) {
  const body = params
    ? new URLSearchParams(params).toString()
    : undefined;

  const res = await fetch(`${API_BASE}${url}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body,
  });

  if (!res.ok) throw new Error(await res.text());
  return res.text();
}
