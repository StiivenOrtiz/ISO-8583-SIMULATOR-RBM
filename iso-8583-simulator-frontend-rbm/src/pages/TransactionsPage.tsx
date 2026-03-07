import { useState, useEffect } from 'react';
import { TransactionRow } from '@/components/TransactionRow';
import { TransactionFilters } from '@/components/TransactionFilters';
import { Transaction, TransactionFilter, TransactionFilterMetadata } from '@/types/transaction';
import { PageResponse } from '@/types/page';
import { Badge } from '@/components/ui/badge';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Radio } from 'lucide-react';
import { apiGet } from "@/lib/api";
import { Skeleton } from '@/components/ui/skeleton';

import { useLiveFeed } from '@/hooks/useLiveFeed';
import { useToast } from "@/hooks/use-toast";

const Transactions = () => {
  const { toast } = useToast();
  
  const [transactions, setTransactions] = useState<Transaction[]>([]);

  const [filters, setFilters] = useState<TransactionFilter>({});

  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(20);

  const [viewMode, setViewMode] = useState<'live' | 'history'>('live');

  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [filterMetadata, setFilterMetadata] = useState<TransactionFilterMetadata | null>(null);

  const [loadingFilters, setLoadingFilters] = useState(false);
  const [loadingTransactions, setLoadingTransactions] = useState(false);

  const fetchFilterMetadata = async () => {
    try {
      setLoadingFilters(true);
      const data = await apiGet<TransactionFilterMetadata>(
        '/transactions/currentinfotransactions'
      );
      setFilterMetadata(data);
    } catch (error) {
      console.error('Error fetching filter metadata:', error);
      toast({
        title: 'Error',
        description: 'Failed to load filters',
        variant: 'destructive',
      });
    } finally {
      setLoadingFilters(false);
    }
  };


  const { transactions: liveTransactionsWs, animationMap } = useLiveFeed(viewMode === 'live');

  useEffect(() => {
    if (viewMode === 'history') fetchTransactions();
  }, [viewMode, filters, currentPage, itemsPerPage]);

  useEffect(() => {
    if (viewMode === 'history') {
      fetchFilterMetadata();
    }
  }, [viewMode]);

  // Filter transactions
  const fetchTransactions = async () => {
    try {
      setLoadingTransactions(true);
      const params = new URLSearchParams();

      if (filters.terminal) params.append('terminal', filters.terminal);
      if (filters.franchise) params.append('franchise', filters.franchise);
      if (filters.transactionType)
        params.append('transactionType', filters.transactionType);
      if (filters.mti) params.append('mti', filters.mti);

      if (filters.status) params.append('status', filters.status);
      if (filters.responseCode) params.append('responseCode', filters.responseCode);
      if (filters.authCode) params.append('authCode', filters.authCode);
      if (filters.rrn) params.append('rrn', filters.rrn);

      if (filters.authCodeEmpty)
        params.append("authCodeEmpty", "true");

      if (filters.rrnEmpty)
        params.append("rrnEmpty", "true");

      if (filters.responseCodeEmpty)
        params.append("responseCodeEmpty", "true");


      if (filters.searchText) params.append('search', filters.searchText);

      if (filters.dateFrom) {
        params.append('dateFrom', `${filters.dateFrom}T00:00:00`);
      }

      if (filters.dateTo) {
        params.append('dateTo', `${filters.dateTo}T23:59:59`);
      }

      if (filters.amountFrom)
        params.append('amountFrom', String(filters.amountFrom));
      if (filters.amountTo) params.append('amountTo', String(filters.amountTo));

      params.append('page', String(currentPage - 1)); // Spring 0-based
      params.append('size', String(itemsPerPage));

      const data = await apiGet<PageResponse<Transaction>>(
        `/transactions?${params.toString()}`
      );


      setTransactions(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch (error) {
      console.error('Error fetching transactions:', error);
      toast({
        title: "Error",
        description: "Failed to load transactions",
        variant: "destructive",
      });
    } finally {
      setLoadingTransactions(false);
    }
  };

  const displayTransactions =
    viewMode === 'live' ? liveTransactionsWs : transactions;

const paginatedTransactions =
  viewMode === 'history'
    ? transactions
    : displayTransactions;

  const resetFilters = () => {
    setFilters({});
    setCurrentPage(1);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Transactions</h1>
          <p className="text-muted-foreground mt-1">
            Monitor and search ISO8583 transactions
          </p>
        </div>

        <div className="flex gap-2">
          <Button
            variant={viewMode === 'live' ? 'default' : 'outline'}
            onClick={() => setViewMode('live')}
            className="gap-2"
          >
            <Radio
              className={`h-4 w-4 ${
                viewMode === 'live' ? 'animate-pulse' : ''
              }`}
            />
            Live Feed
          </Button>
          <Button
            variant={viewMode === 'history' ? 'default' : 'outline'}
            onClick={() => setViewMode('history')}
          >
            History
          </Button>
        </div>
      </div>

      {viewMode === 'live' && (
        <div className="flex items-center gap-2">
          <Badge className="bg-destructive text-destructive-foreground animate-pulse">
            LIVE
          </Badge>
        </div>
      )}

      {viewMode === 'history' && (
        <>
          <TransactionFilters
            filters={filters}
            terminals={filterMetadata?.terminals ?? []}
            franchises={filterMetadata?.franchises ?? []}
            transactionTypes={filterMetadata?.transactionTypes ?? []}
            mtis={filterMetadata?.mtis ?? []}
            statusValues={filterMetadata?.statusValues ?? []}
            loading={loadingFilters}
            onFiltersChange={(newFilters) => {
              setFilters(newFilters);
              setCurrentPage(1);
            }}
            onReset={resetFilters}
          />



          <div className="flex items-center justify-between">
            <p className="text-sm text-muted-foreground">
              Showing {transactions.length} of {totalElements} transactions
            </p>
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-2">
                <span className="text-sm text-muted-foreground">
                  Items per page:
                </span>
                  <Select
                  value={String(itemsPerPage)}
                  onValueChange={(value) => {
                    setItemsPerPage(Number(value));
                    setCurrentPage(1);
                  }}
                >
                  <SelectTrigger className="w-[120px]">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="5">5</SelectItem>
                    <SelectItem value="10">10</SelectItem>
                    <SelectItem value="20">20</SelectItem>
                    <SelectItem value="50">50</SelectItem>
                    <SelectItem value="100">100</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>
        </>
      )}

      <Card className="p-6 shadow-card">
        <div className="space-y-3">
          {loadingTransactions && viewMode === 'history' ? (
            Array.from({ length: 5 }).map((_, i) => (
              <div key={i} className="flex items-center gap-4 p-4 border border-border rounded-lg">
                <Skeleton className="h-12 w-1.5 rounded-full" />
                <div className="flex-1 space-y-2">
                  <Skeleton className="h-4 w-3/4" />
                  <Skeleton className="h-3 w-1/2" />
                </div>
              </div>
            ))
          ) : paginatedTransactions.length === 0 ? (
            <div className="text-center py-12">
              <p className="text-muted-foreground">No transactions found</p>
            </div>
          ) : (
            paginatedTransactions.map((transaction) => (
              <TransactionRow
                key={transaction.uuid}
                transaction={transaction}
                animationClass={
                  viewMode === 'live' && animationMap[transaction.uuid] === 'new'
                    ? 'animate-tx-new'
                    : viewMode === 'live' && animationMap[transaction.uuid] === 'updated'
                    ? 'animate-tx-updated'
                    : undefined
                }
              />
            ))
          )}
        </div>
      </Card>

      {viewMode === 'history' && totalPages > 1 && (
        <div className="flex items-center justify-center gap-2">
          <Button
            variant="outline"
            onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
            disabled={currentPage === 1}
          >
            Previous
          </Button>
          <span className="text-sm text-muted-foreground">
            Page {currentPage} of {totalPages}
          </span>
          <Button
            variant="outline"
            onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
            disabled={currentPage === totalPages}
          >
            Next
          </Button>
        </div>
      )}
    </div>
  );
};

export default Transactions;
