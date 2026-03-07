import { useState } from "react";
import { ChevronDown, ChevronRight, Copy, Check } from "lucide-react";
import { Transaction } from "@/types/transaction";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";
import { getFranchiseLogo } from "@/utils/franchiseLogos";

import isoFieldNames from "@/lib/iso8583/iso8583-fields.json";
import { sortIsoFields } from "@/lib/iso8583/sortIsoFields";
import { IsoFieldRow } from "@/components/ui/IsoFieldRow";


interface TransactionRowProps {
  transaction: Transaction;
  animationClass?: string;
}

function CopyButton({ value }: { value: string }) {
  const [copied, setCopied] = useState(false);

  const handleCopy = async (e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await navigator.clipboard.writeText(value);
      setCopied(true);
      toast.success("Copied to clipboard");
      setTimeout(() => setCopied(false), 2000);
    } catch {
      toast.error("Failed to copy");
    }
  };

  return (
    <Button
      variant="ghost"
      size="sm"
      className="h-6 w-6 p-0 opacity-60 hover:opacity-100"
      onClick={handleCopy}
    >
      {copied ? (
        <Check className="h-3 w-3 text-success" />
      ) : (
        <Copy className="h-3 w-3" />
      )}
    </Button>
  );
}

export function TransactionRow({ transaction, animationClass }: TransactionRowProps) {
  const [expanded, setExpanded] = useState(false);

  const [requestExpanded, setRequestExpanded] = useState(false);

  const requestFields = transaction.requestDataElements
    ? sortIsoFields(transaction.requestDataElements, isoFieldNames)
    : [];


  const responseFields = transaction.responseDataElements
    ? sortIsoFields(transaction.responseDataElements, isoFieldNames)
    : [];


  const [responseExpanded, setResponseExpanded] = useState(false);


  // Determine transaction status for the indicator based on status field
  const getStatusColor = () => {
    switch (transaction.status) {
      case "success":
        return "bg-success";
      case "pending":
        return "bg-warning";
      case "failed":
        return "bg-destructive";
      default:
        return "bg-muted";
    }
  };

  return (
    <div className={`border border-border rounded-lg overflow-hidden bg-card flex ${animationClass || ''}`}>
      {/* Status Indicator */}
      <div className={`w-1.5 flex-shrink-0 ${getStatusColor()}`} />
      
      {/* Content Container */}
      <div className="flex-1">
        {/* Main Row */}
        <div className="p-4 hover:bg-muted/50 transition-colors cursor-pointer" onClick={() => setExpanded(!expanded)}>
        <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 flex-1">
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-8 w-8 p-0"
                  onClick={(e) => {
                    e.stopPropagation();
                    setExpanded(!expanded);
                  }}
                >
                  {expanded ? <ChevronDown className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />}
                </Button>

                <div className="grid grid-cols-[1fr_1fr_1fr_1fr_2fr_1.8fr_1fr_1fr_1fr_1fr] gap-4 flex-1 text-sm">

                    <div className="min-w-0">
                        <span className="text-muted-foreground text-xs block">Date</span>
                        <div className="min-h-10 flex items-center">
                            <span className="font-medium truncate block">
                                {new Date(transaction.txTimestamp).toLocaleDateString()}
                            </span>
                        </div>
                    </div>

                    <div className="min-w-0">
                        <span className="text-muted-foreground text-xs block">Time</span>
                        <div className="min-h-10 flex items-center">
                            <span className="font-medium truncate block">
                                {new Date(transaction.txTimestamp).toLocaleTimeString()}
                            </span>
                        </div>
                    </div>

                    <div className="min-w-0">
                        <span className="text-muted-foreground text-xs block">Terminal</span>
                        <div className="min-h-10 flex items-center"> 
                            <span className="font-mono font-medium truncate block">{transaction.terminal}</span>
                        </div>
                    </div>

                    <div className="min-w-0">
                        <span className="text-muted-foreground text-xs block">Amount</span>
                        <div className="min-h-10 flex items-center"> 
                            <span className="font-semibold text-foreground truncate block">
                              {(() => {
                                if (!transaction) return "—"

                                const amount = Number(transaction.amount)

                                if (isNaN(amount)) return "—"

                                return `$${amount.toLocaleString("en-US", {
                                  minimumFractionDigits: 2,
                                })}`
                              })()}
                            </span>
                        </div>
                    </div>

                    <div className="min-w-0">
                      <span className="text-muted-foreground text-xs block">Franchise</span>
                      <div className="flex items-center gap-2 min-w-0 min-h-10">
                        {transaction.franchise && transaction.franchise.toLowerCase() !== "none" ? (
                          <>
                            <div className="h-10 w-14 flex items-center justify-center rounded overflow-hidden flex-shrink-0">
                              <img
                                src={getFranchiseLogo(transaction.franchiseLogo)}
                                alt={`Logo of ${transaction.franchise}`}
                                className="h-full w-full object-scale-down"
                                onError={(e) => {
                                  (e.currentTarget as HTMLImageElement).src = '/franchises/logo_unknown.svg';
                                }}
                              />
                            </div>
                            <span className="font-medium truncate">{transaction.franchise}</span>
                          </>
                        ) : (
                          <span className="text-muted-foreground">—</span>
                        )}
                      </div>
                    </div>

                    <div className="min-w-0">
                        <span className="text-muted-foreground text-xs block">Type</span>
                        <div className="min-h-10 flex items-center"> 
                            <span className="font-medium truncate block">{transaction.transactionType}</span>
                        </div>
                    </div>

                    <div className="min-w-0">
                        <span className="text-muted-foreground text-xs block">MTI</span>
                        <div className="min-h-10 flex items-center">
                            <span className="font-mono font-medium truncate block">{transaction.mti}</span>
                        </div>
                    </div>
                    
                    <div className="min-w-0">
                        <span className="text-muted-foreground text-xs block">Auth Code</span>
                        <div className="min-h-10 flex items-center">
                            <span className="font-mono font-medium truncate block">{transaction.authCode || "-"}</span>
                        </div>
                    </div>

                    <div className="min-w-0">
                        <span className="text-muted-foreground text-xs block">RRN</span>
                        <div className="min-h-10 flex items-center">
                            <span className="font-mono font-medium truncate block">{transaction.rrn || "-"}</span>
                        </div>
                    </div>
                    
                    <div className="min-w-0">
                        <span className="text-muted-foreground text-xs block">Response</span>
                        <div className="min-h-10 flex items-center">
                            <Badge
                                className={
                                    transaction.responseCode === "00"
                                        ? "bg-success/10 text-success border-success/20 hover:bg-success/10"
                                        : "bg-destructive/10 text-destructive border-destructive/20 hover:bg-destructive/10"
                                }
                            >
                                {transaction.responseCode}
                            </Badge>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

      {/* Expanded Details */}
      {expanded && (
        <div className="border-t border-border bg-muted/30 p-6 space-y-6">
          {/* MTI & Bitmap */}
          <div className="grid grid-cols-2 lg:grid-cols-3 gap-4">
            <div className="bg-card p-3 rounded border border-border">
              <div className="flex items-center justify-between mb-1">
                <p className="text-xs text-muted-foreground font-semibold">MTI</p>
                <CopyButton value={transaction.mti} />
              </div>
              <p className="font-mono text-sm text-foreground">{transaction.mti}</p>
            </div>
            <div className="bg-card p-3 rounded border border-border">
              <div className="flex items-center justify-between mb-1">
                <p className="text-xs text-muted-foreground font-semibold">Bitmap Primary</p>
                <CopyButton value={transaction.bitmapPrimary} />
              </div>
              <p className="font-mono text-sm text-foreground break-all">{transaction.bitmapPrimary}</p>
            </div>
            {transaction.bitmapSecondary && (
              <div className="bg-card p-3 rounded border border-border">
                <div className="flex items-center justify-between mb-1">
                  <p className="text-xs text-muted-foreground font-semibold">Bitmap Secondary</p>
                  <CopyButton value={transaction.bitmapSecondary} />
                </div>
                <p className="font-mono text-sm text-foreground break-all">{transaction.bitmapSecondary}</p>
              </div>
            )}
          </div>

          {/* Request Data Elements - Only active fields based on bitmap */}
          {requestFields.length > 0 && (
            <div>
              <div 
                className={`flex items-center gap-2 cursor-pointer mb-2 p-2 rounded border-t border-border hover:bg-muted/100 transition-colors`}
                onClick={() => setRequestExpanded(!requestExpanded)}
              >
                <p className="text-sm font-semibold text-foreground">
                  Request Data Elements
                </p>
                <ChevronDown className={`h-5 w-5 text-primary transition-transform ${requestExpanded ? "rotate-180" : ""}`} />
              </div>

              {requestExpanded && (
                <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-3">
                  {requestFields.map((field) => (
                    <div key={field.key} className="bg-card p-3 rounded border border-border">
                      
                      <div className="flex items-center justify-between mb-1">
                        <p className="text-xs font-semibold text-muted-foreground">
                          {field.key}
                        </p>
                        <CopyButton value={field.value} />
                      </div>

                      {isoFieldNames[field.key] && (
                        <p className="text-[9px] leading-tight text-muted-foreground truncate">
                          {isoFieldNames[field.key]}
                        </p>
                      )}

                      <p className="font-mono text-sm text-foreground break-all">
                        {field.value}
                      </p>
                      
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Response Data Elements */}
          {responseFields.length > 0 && (
            <div>
              <div 
                className={`flex items-center gap-2 cursor-pointer mb-2 p-2 rounded border-t border-border hover:bg-muted/100 transition-colors`}
                onClick={() => setResponseExpanded(!responseExpanded)}
              >
                <p className="text-sm font-semibold text-foreground">
                  Response Data Elements
                </p>
                <ChevronDown className={`h-5 w-5 text-primary transition-transform ${responseExpanded ? "rotate-180" : ""}`} />
              </div>

              {responseExpanded && (
                <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-3">
                  {responseFields.map((field) => (
                    <div key={field.key} className="bg-card p-3 rounded border border-border">
                      
                      <div className="flex items-center justify-between mb-1">
                        <p className="text-xs font-semibold text-muted-foreground">
                          {field.key}
                        </p>
                        <CopyButton value={field.value} />
                      </div>

                      {isoFieldNames[field.key] && (
                        <p className="text-[9px] leading-tight text-muted-foreground truncate">
                          {isoFieldNames[field.key]}
                        </p>
                      )}

                      <p className="font-mono text-sm text-foreground break-all">
                        {field.value}
                      </p>
                      
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Hex Messages */}
          <div className="space-y-4">
            {transaction.hexRequest && (
              <div className="bg-card p-4 rounded border border-border">
                <div className="flex items-center justify-between mb-2">
                  <p className="text-sm font-semibold text-foreground">Hex Request</p>
                  <CopyButton value={transaction.hexRequest} />
                </div>
                <div className="overflow-x-auto">
                  <p className="font-mono text-xs text-foreground break-all">{transaction.hexRequest}</p>
                </div>
              </div>
            )}
            {transaction.hexResponse && (
              <div className="bg-card p-4 rounded border border-border">
                <div className="flex items-center justify-between mb-2">
                  <p className="text-sm font-semibold text-foreground">Hex Response</p>
                  <CopyButton value={transaction.hexResponse} />
                </div>
                <div className="overflow-x-auto">
                  <p className="font-mono text-xs text-foreground break-all">{transaction.hexResponse}</p>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
      </div>
    </div>
  );
}
