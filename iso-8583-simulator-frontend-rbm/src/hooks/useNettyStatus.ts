import { useState, useEffect, useCallback } from 'react';
import { getNettyStatus, type NettyStatusResponse } from '@/lib/nettyApi';

export function useNettyStatus(intervalMs = 5000) {
  const [status, setStatus] = useState<NettyStatusResponse | null>(null);
  const [error, setError] = useState(false);

  const fetchStatus = useCallback(async () => {
    try {
      const data = await getNettyStatus();
      setStatus(data);
      setError(false);
    } catch {
      setError(true);
      setStatus(null);
    }
  }, []);

  useEffect(() => {
    fetchStatus();
    const id = setInterval(fetchStatus, intervalMs);
    return () => clearInterval(id);
  }, [fetchStatus, intervalMs]);

  return { status, error, refetch: fetchStatus };
}
