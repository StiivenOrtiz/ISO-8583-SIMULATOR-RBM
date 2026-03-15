import { useEffect, useRef, useState, useCallback } from 'react';
import { Transaction } from '@/types/transaction';

export type TxAnimationType = 'new' | 'updated' | null;

export function useLiveFeed(isActive: boolean) {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [animationMap, setAnimationMap] = useState<Record<string, TxAnimationType>>({});
  const socketRef = useRef<WebSocket | null>(null);
  const retryTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const isActiveRef = useRef(isActive);

  const clearAnimation = useCallback((id: string) => {
    setTimeout(() => {
      setAnimationMap((prev) => {
        const next = { ...prev };
        delete next[id];
        return next;
      });
    }, 1200);
  }, []);

  const connect = () => {
    const protocol = window.location.protocol === "https:" ? "wss" : "ws";
    const host = window.location.host;
    const socketUrl = `${protocol}://${host}/ws/tx`;

    const socket = new WebSocket(socketUrl);
    socketRef.current = socket;

    socket.onopen = () => {
      if (retryTimeoutRef.current) clearTimeout(retryTimeoutRef.current);
    };

    socket.onmessage = (event) => {
      const tx: Transaction = JSON.parse(event.data);

      const STATUS_PRIORITY: Record<string, number> = {
        pending: 0,
        failed: 1,
        success: 2,
      };

      setTransactions((prev) => {
        const existingIndex = prev.findIndex((t) => t.uuid === tx.uuid);
        if (existingIndex !== -1) {
          const existing = prev[existingIndex];
          const existingPriority = STATUS_PRIORITY[existing.status] ?? -1;
          const incomingPriority = STATUS_PRIORITY[tx.status] ?? -1;

          // Don't downgrade status (e.g. success → pending)
          if (incomingPriority < existingPriority) {
            return prev;
          }

          const updated = [...prev];
          updated[existingIndex] = tx;
          setAnimationMap((m) => ({ ...m, [tx.uuid]: 'updated' }));
          clearAnimation(tx.uuid);
          return updated;
        }
        // New transaction — prepend
        setAnimationMap((m) => ({ ...m, [tx.uuid]: 'new' }));
        clearAnimation(tx.uuid);
        return [tx, ...prev].slice(0, 50);
      });
    };

    socket.onclose = () => {
      if (isActiveRef.current) {
        retryTimeoutRef.current = setTimeout(connect, 2000);
      }
    };

    socket.onerror = (err) => {
      console.error("WebSocket error:", err);
      socket.close();
    };
  };

  useEffect(() => {
    isActiveRef.current = isActive;

    if (!isActive) {
      setTransactions([]);
      setAnimationMap({});
      if (socketRef.current) {
        socketRef.current.close();
        socketRef.current = null;
      }
      if (retryTimeoutRef.current) {
        clearTimeout(retryTimeoutRef.current);
        retryTimeoutRef.current = null;
      }
      return;
    }

    setTransactions([]);
    setAnimationMap({});
    connect();

    return () => {
      isActiveRef.current = false;
      if (socketRef.current) {
        socketRef.current.close();
        socketRef.current = null;
      }
      if (retryTimeoutRef.current) {
        clearTimeout(retryTimeoutRef.current);
        retryTimeoutRef.current = null;
      }
    };
  }, [isActive]);

  return { transactions, animationMap };
}
