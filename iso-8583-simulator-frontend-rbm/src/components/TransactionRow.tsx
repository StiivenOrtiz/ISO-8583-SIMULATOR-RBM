import { useState } from "react";
import { ChevronDown, ChevronRight, Copy, Check, FileCode2, Globe, Eye } from "lucide-react";
import { Transaction } from "@/types/transaction";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";
import { getFranchiseLogo } from "@/utils/franchiseLogos";
import { useLanguage } from "@/i18n/LanguageContext";
import { Tooltip, TooltipContent, TooltipTrigger, TooltipProvider } from "@/components/ui/tooltip";

import isoFieldNames from "@/lib/iso8583/iso8583-fields.json";
import { sortIsoFields } from "@/lib/iso8583/sortIsoFields";
import { IsoFieldRow } from "@/components/ui/IsoFieldRow";
import { VoucherPreview } from "@/components/VoucherPreview";


interface TransactionRowProps {
  transaction: Transaction;
  animationClass?: string;
}

function CopyButton({ value, successMsg, errorMsg }: { value: string; successMsg?: string; errorMsg?: string }) {
  const [copied, setCopied] = useState(false);

  const handleCopy = async (e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await navigator.clipboard.writeText(value);
      setCopied(true);
      toast.success(successMsg || "Copied to clipboard");
      setTimeout(() => setCopied(false), 2000);
    } catch {
      toast.error(errorMsg || "Failed to copy");
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

function ProtocolIcon({ protocol }: { protocol?: string }) {
  if (!protocol || protocol === 'ISO8583') {
    return (
      <div className="flex items-center gap-1.5" title="ISO8583">
        <FileCode2 className="h-4 w-4 text-primary" />
        <span className="text-xs font-mono font-medium">ISO</span>
      </div>
    );
  }
  if (protocol === 'HTTP') {
    return (
      <div className="flex items-center gap-1.5" title="HTTP">
        <Globe className="h-4 w-4 text-chart-2" />
        <span className="text-xs font-mono font-medium">HTTP</span>
      </div>
    );
  }
  return <span className="text-muted-foreground text-xs">{protocol}</span>;
}

// Map DV keys to i18n keys for HTTP transactions
const dvKeyTranslationMap: Record<string, 'receipt' | 'package' | 'packages' | 'signature'> = {
  receipt: 'receipt',
  package: 'package',
  packages: 'packages',
  signature: 'signature',
};

export function TransactionRow({ transaction, animationClass }: TransactionRowProps) {
  const [expanded, setExpanded] = useState(false);
  const [requestExpanded, setRequestExpanded] = useState(false);
  const [responseExpanded, setResponseExpanded] = useState(false);
  const [dvExpanded, setDvExpanded] = useState(false);
  const [voucherOpen, setVoucherOpen] = useState(false);
  const [voucherValue, setVoucherValue] = useState("");
  const { t } = useLanguage();

  const isHttp = transaction.protocol === 'HTTP';
  const mtiLabel = isHttp ? t.tx.endpoint : t.tx.mti;

  const requestFields = transaction.requestDataElements
    ? sortIsoFields(transaction.requestDataElements, isoFieldNames)
    : [];

  const responseFields = transaction.responseDataElements
    ? sortIsoFields(transaction.responseDataElements, isoFieldNames)
    : [];

  const getStatusColor = () => {
    switch (transaction.status) {
      case "success": return "bg-success";
      case "pending": return "bg-warning";
      case "failed": return "bg-destructive";
      default: return "bg-muted";
    }
  };

  const getStatusLabel = () => {
    switch (transaction.status) {
      case "success": return t.tx.status === 'Estado' ? 'Exitosa' : 'Success';
      case "pending": return t.tx.status === 'Estado' ? 'Pendiente' : 'Pending';
      case "failed": return t.tx.status === 'Estado' ? 'Fallida' : 'Failed';
      default: return transaction.status;
    }
  };

  // Translate DV element key for display
  const getDvKeyLabel = (key: string) => {
    const lowerKey = key.toLowerCase();
    if (dvKeyTranslationMap[lowerKey]) {
      return t.tx[dvKeyTranslationMap[lowerKey]];
    }
    return key;
  };

  // Separate signature from other DV elements
  const dvEntries = transaction.requestDVElements
    ? Object.entries(transaction.requestDVElements).sort(([a], [b]) => a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' }))
    : [];
  const signatureEntry = dvEntries.find(([key]) => key.toLowerCase() === 'signature');
  const regularDvEntries = dvEntries.filter(([key]) => key.toLowerCase() !== 'signature');

  return (
    <TooltipProvider>
      <div className={`border border-border rounded-lg overflow-hidden bg-card flex ${animationClass || ''}`}>
        <div className={`w-1.5 flex-shrink-0 ${getStatusColor()}`} />
        <div className="flex-1">
          <div className="p-4 hover:bg-muted/50 transition-colors cursor-pointer" onClick={() => setExpanded(!expanded)}>
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 flex-1">
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-8 w-8 p-0"
                  onClick={(e) => { e.stopPropagation(); setExpanded(!expanded); }}
                >
                  {expanded ? <ChevronDown className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />}
                </Button>

                <div className="grid grid-cols-[0.8fr_1fr_1fr_0.8fr_1fr_1.5fr_1.8fr_1.65fr_0.8fr_0.8fr_0.5fr] gap-4 flex-1 text-sm">

                  <div className="min-w-0">
                    <span className="text-muted-foreground text-xs block">{t.tx.protocol}</span>
                    <div className="min-h-10 flex items-center">
                      <ProtocolIcon protocol={transaction.protocol} />
                    </div>
                  </div>

                  <div className="min-w-0">
                    <span className="text-muted-foreground text-xs block">{t.tx.dateLabel}</span>
                    <div className="min-h-10 flex items-center">
                      <span className="font-medium truncate block">
                        {new Date(transaction.txTimestamp).toLocaleDateString()}
                      </span>
                    </div>
                  </div>

                  <div className="min-w-0">
                    <span className="text-muted-foreground text-xs block">{t.tx.time}</span>
                    <div className="min-h-10 flex items-center">
                      <span className="font-medium truncate block">
                        {new Date(transaction.txTimestamp).toLocaleTimeString()}
                      </span>
                    </div>
                  </div>

                  <div className="min-w-0">
                    <span className="text-muted-foreground text-xs block">{t.tx.terminal}</span>
                    <div className="min-h-10 flex items-center">
                      <span className="font-mono font-medium truncate block">{transaction.terminal}</span>
                    </div>
                  </div>

                  <div className="min-w-0">
                    <span className="text-muted-foreground text-xs block">{t.tx.amount}</span>
                    <div className="min-h-10 flex items-center">
                      <span className="font-semibold text-foreground truncate block">
                        {(() => {
                          if (!transaction) return "—";
                          const amount = Number(transaction.amount);
                          if (isNaN(amount)) return "—";
                          return `$${amount.toLocaleString("en-US", { minimumFractionDigits: 2 })}`;
                        })()}
                      </span>
                    </div>
                  </div>

                  <div className="min-w-0">
                    <span className="text-muted-foreground text-xs block">{t.tx.franchise}</span>
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
                    <span className="text-muted-foreground text-xs block">{t.tx.type}</span>
                    <div className="min-h-10 flex items-center">
                      <span className="font-medium truncate block">{transaction.transactionType}</span>
                    </div>
                  </div>

                  <div className="min-w-0">
                    <span className="text-muted-foreground text-xs block">{mtiLabel}</span>
                    <div className="min-h-10 flex items-center">
                      <Tooltip>
                        <TooltipTrigger asChild>
                          <span className="font-mono font-medium truncate block cursor-default">{transaction.mti}</span>
                        </TooltipTrigger>
                        <TooltipContent side="top" className="max-w-xs">
                          <p className="font-mono text-xs break-all">{transaction.mti}</p>
                        </TooltipContent>
                      </Tooltip>
                    </div>
                  </div>

                  <div className="min-w-0">
                    <span className="text-muted-foreground text-xs block">{t.tx.authCode}</span>
                    <div className="min-h-10 flex items-center">
                      <span className="font-mono font-medium truncate block">{transaction.authCode || "-"}</span>
                    </div>
                  </div>

                  <div className="min-w-0">
                    <span className="text-muted-foreground text-xs block">{t.tx.rrn}</span>
                    <div className="min-h-10 flex items-center">
                      <span className="font-mono font-medium truncate block">{transaction.rrn || "-"}</span>
                    </div>
                  </div>

                  <div className="min-w-0">
                    <span className="text-muted-foreground text-xs block">{t.tx.response}</span>
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

          {expanded && (
            <div className="border-t border-border bg-muted/30 p-6 space-y-6">
              {/* Summary cards: ordered, hide empty, all copyable */}
              {(() => {
                const amountStr = (() => {
                  const amount = Number(transaction.amount);
                  if (isNaN(amount)) return "";
                  return `$${amount.toLocaleString("en-US", { minimumFractionDigits: 2 })}`;
                })();
                const franchiseVal = transaction.franchise && transaction.franchise.toLowerCase() !== "none" ? transaction.franchise : "";
                const fields: { label: string; value: string; render?: React.ReactNode; copyValue?: string }[] = [
                  { label: t.tx.status, value: getStatusLabel(), render: <Badge className={`${getStatusColor()}/10 border-${getStatusColor()}/20`}>{getStatusLabel()}</Badge> },
                  { label: t.tx.protocol, value: transaction.protocol || "ISO8583", render: <ProtocolIcon protocol={transaction.protocol} />, copyValue: transaction.protocol || "ISO8583" },
                  { label: t.tx.dateLabel, value: new Date(transaction.txTimestamp).toLocaleDateString() },
                  { label: t.tx.time, value: new Date(transaction.txTimestamp).toLocaleTimeString() },
                  { label: t.tx.terminal, value: transaction.terminal },
                  { label: t.tx.amount, value: amountStr },
                  { label: t.tx.franchise, value: franchiseVal, render: franchiseVal ? (
                    <div className="flex items-center gap-2">
                      <img src={getFranchiseLogo(transaction.franchiseLogo)} alt={franchiseVal} className="h-6 w-8 object-scale-down" onError={(e) => { (e.currentTarget as HTMLImageElement).src = '/franchises/logo_unknown.svg'; }} />
                      <span className="text-sm font-medium">{franchiseVal}</span>
                    </div>
                  ) : undefined },
                  { label: t.tx.type, value: transaction.transactionType },
                  { label: mtiLabel, value: transaction.mti },
                  { label: t.tx.authCode, value: transaction.authCode || "" },
                  { label: t.tx.rrn, value: transaction.rrn || "" },
                  { label: t.tx.response, value: transaction.responseCode, render: (
                    <Badge className={transaction.responseCode === "00" ? "bg-success/10 text-success border-success/20 hover:bg-success/10" : "bg-destructive/10 text-destructive border-destructive/20 hover:bg-destructive/10"}>
                      {transaction.responseCode}
                    </Badge>
                  ) },
                ];
                const visibleFields = fields.filter(f => f.value && f.value.trim() !== "");
                return (
                  <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-4">
                    {visibleFields.map((field) => (
                      <div key={field.label} className="bg-card p-3 rounded border border-border">
                        <div className="flex items-center justify-between mb-1">
                          <p className="text-xs text-muted-foreground font-semibold">{field.label}</p>
                          <CopyButton value={field.copyValue || field.value} successMsg={t.tx.copiedToClipboard} errorMsg={t.tx.failedToCopy} />
                        </div>
                        {field.render || <p className="font-mono text-sm text-foreground break-all">{field.value}</p>}
                      </div>
                    ))}
                  </div>
                );
              })()}
              {/* Bitmap cards */}
              <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-4">
                {transaction.bitmapPrimary && (
                  <div className="bg-card p-3 rounded border border-border">
                    <div className="flex items-center justify-between mb-1">
                      <p className="text-xs text-muted-foreground font-semibold">{t.tx.bitmapPrimary}</p>
                      <CopyButton value={transaction.bitmapPrimary} successMsg={t.tx.copiedToClipboard} errorMsg={t.tx.failedToCopy} />
                    </div>
                    <p className="font-mono text-sm text-foreground break-all">{transaction.bitmapPrimary}</p>
                  </div>
                )}
                {transaction.bitmapSecondary && (
                  <div className="bg-card p-3 rounded border border-border">
                    <div className="flex items-center justify-between mb-1">
                      <p className="text-xs text-muted-foreground font-semibold">{t.tx.bitmapSecondary}</p>
                      <CopyButton value={transaction.bitmapSecondary} successMsg={t.tx.copiedToClipboard} errorMsg={t.tx.failedToCopy} />
                    </div>
                    <p className="font-mono text-sm text-foreground break-all">{transaction.bitmapSecondary}</p>
                  </div>
                )}
              </div>

              {requestFields.length > 0 && (
                <div>
                  <div
                    className="flex items-center gap-2 cursor-pointer mb-2 p-2 rounded border-t border-border hover:bg-muted/100 transition-colors"
                    onClick={() => setRequestExpanded(!requestExpanded)}
                  >
                    <p className="text-sm font-semibold text-foreground">{t.tx.requestDataElements}</p>
                    <ChevronDown className={`h-5 w-5 text-primary transition-transform ${requestExpanded ? "rotate-180" : ""}`} />
                  </div>
                  {requestExpanded && (
                    <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-3">
                      {requestFields.map((field) => (
                        <div key={field.key} className="bg-card p-3 rounded border border-border">
                          <div className="flex items-center justify-between mb-1">
                            <p className="text-xs font-semibold text-muted-foreground">{field.key}</p>
                            <CopyButton value={field.value} successMsg={t.tx.copiedToClipboard} errorMsg={t.tx.failedToCopy} />
                          </div>
                          {isoFieldNames[field.key] && (
                            <p className="text-[9px] leading-tight text-muted-foreground truncate">{isoFieldNames[field.key]}</p>
                          )}
                          <p className="font-mono text-sm text-foreground break-all">{field.value}</p>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {responseFields.length > 0 && (
                <div>
                  <div
                    className="flex items-center gap-2 cursor-pointer mb-2 p-2 rounded border-t border-border hover:bg-muted/100 transition-colors"
                    onClick={() => setResponseExpanded(!responseExpanded)}
                  >
                    <p className="text-sm font-semibold text-foreground">{t.tx.responseDataElements}</p>
                    <ChevronDown className={`h-5 w-5 text-primary transition-transform ${responseExpanded ? "rotate-180" : ""}`} />
                  </div>
                  {responseExpanded && (
                    <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-3">
                      {responseFields.map((field) => (
                        <div key={field.key} className="bg-card p-3 rounded border border-border">
                          <div className="flex items-center justify-between mb-1">
                            <p className="text-xs font-semibold text-muted-foreground">{field.key}</p>
                            <CopyButton value={field.value} successMsg={t.tx.copiedToClipboard} errorMsg={t.tx.failedToCopy} />
                          </div>
                          {isoFieldNames[field.key] && (
                            <p className="text-[9px] leading-tight text-muted-foreground truncate">{isoFieldNames[field.key]}</p>
                          )}
                          <p className="font-mono text-sm text-foreground break-all">{field.value}</p>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {dvEntries.length > 0 && (
                <div>
                  <div
                    className="flex items-center gap-2 cursor-pointer mb-2 p-2 rounded border-t border-border hover:bg-muted/100 transition-colors"
                    onClick={() => setDvExpanded(!dvExpanded)}
                  >
                    <p className="text-sm font-semibold text-foreground">{t.tx.requestDVElements}</p>
                    <ChevronDown className={`h-5 w-5 text-primary transition-transform ${dvExpanded ? "rotate-180" : ""}`} />
                  </div>
                  {dvExpanded && (
                    <div className="space-y-3">
                      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-3">
                        {regularDvEntries.map(([key, value]) => (
                          <div key={key} className="bg-card p-3 rounded border border-border">
                            <div className="flex items-center justify-between mb-1">
                              <p className="text-xs font-semibold text-muted-foreground">{getDvKeyLabel(key)}</p>
                              <div className="flex items-center gap-1">
                                {key === "98" && (
                                  <Button
                                    variant="ghost"
                                    size="sm"
                                    className="h-6 w-6 p-0 opacity-60 hover:opacity-100"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      setVoucherValue(value);
                                      setVoucherOpen(true);
                                    }}
                                  >
                                    <Eye className="h-3 w-3" />
                                  </Button>
                                )}
                                <CopyButton value={value} successMsg={t.tx.copiedToClipboard} errorMsg={t.tx.failedToCopy} />
                              </div>
                            </div>
                            <p className="font-mono text-sm text-foreground break-all">{value}</p>
                          </div>
                        ))}
                      </div>

                      {/* Signature field: full width at bottom */}
                      {signatureEntry && (
                        <div className="bg-card p-4 rounded border border-border">
                          <div className="flex items-center justify-between mb-2">
                            <p className="text-sm font-semibold text-foreground">{getDvKeyLabel(signatureEntry[0])}</p>
                            <CopyButton value={signatureEntry[1]} successMsg={t.tx.copiedToClipboard} errorMsg={t.tx.failedToCopy} />
                          </div>
                          <div className="overflow-x-auto">
                            <p className="font-mono text-xs text-foreground break-all">{signatureEntry[1]}</p>
                          </div>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )}

              <div className="space-y-4">
                {transaction.hexRequest && (
                  <div className="bg-card p-4 rounded border border-border">
                    <div className="flex items-center justify-between mb-2">
                      <p className="text-sm font-semibold text-foreground">{t.tx.hexRequest}</p>
                      <CopyButton value={transaction.hexRequest} successMsg={t.tx.copiedToClipboard} errorMsg={t.tx.failedToCopy} />
                    </div>
                    <div className="overflow-x-auto">
                      <p className="font-mono text-xs text-foreground break-all">{transaction.hexRequest}</p>
                    </div>
                  </div>
                )}
                {transaction.hexResponse && (
                  <div className="bg-card p-4 rounded border border-border">
                    <div className="flex items-center justify-between mb-2">
                      <p className="text-sm font-semibold text-foreground">{t.tx.hexResponse}</p>
                      <CopyButton value={transaction.hexResponse} successMsg={t.tx.copiedToClipboard} errorMsg={t.tx.failedToCopy} />
                    </div>
                    <div className="overflow-x-auto">
                      <p className="font-mono text-xs text-foreground break-all">{transaction.hexResponse}</p>
                    </div>
                  </div>
                )}
              </div>

              <VoucherPreview open={voucherOpen} onOpenChange={setVoucherOpen} value={voucherValue} />
            </div>
          )}
        </div>
      </div>
    </TooltipProvider>
  );
}
