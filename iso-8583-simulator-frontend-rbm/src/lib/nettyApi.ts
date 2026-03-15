import { apiPost, apiGet } from "@/lib/api";

export interface NettyStatusResponse {
  running: boolean;
  connections: number;
  timestamp: number;
}

export interface NettyActionResponse {
  running: boolean;
  connections: number;
  timestamp: number;
}

export const startNetty = async (): Promise<NettyActionResponse> => {
  const text = await apiPost("/netty/start");
  try {
    return JSON.parse(text);
  } catch {
    return { running: true, connections: 0, timestamp: Date.now() };
  }
};

export const stopNetty = async (): Promise<NettyActionResponse> => {
  const text = await apiPost("/netty/stop");
  try {
    return JSON.parse(text);
  } catch {
    return { running: false, connections: 0, timestamp: Date.now() };
  }
};

export const restartNetty = async (): Promise<NettyActionResponse> => {
  const text = await apiPost("/netty/restart");
  try {
    return JSON.parse(text);
  } catch {
    return { running: true, connections: 0, timestamp: Date.now() };
  }
};

export const getNettyStatus = (): Promise<NettyStatusResponse> =>
  apiGet<NettyStatusResponse>("/netty/status");
