import { apiPost } from "@/lib/api";

export const startNetty = () => apiPost("/netty/start");
export const stopNetty = () => apiPost("/netty/stop");
export const restartNetty = () => apiPost("/netty/restart");
