import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from "@/components/ui/command";
import { X, Check, ChevronsUpDown } from "lucide-react";
import { TransactionFilter } from "@/types/transaction";
import { useState } from "react";
import { cn } from "@/lib/utils";
import { Checkbox } from "@/components/ui/checkbox";

interface TransactionFiltersProps {
  filters: TransactionFilter;
  terminals: string[];
  franchises: string[];
  transactionTypes: string[];
  mtis: string[];
  statusValues: string[];
  loading?: boolean;
  onFiltersChange: (filters: TransactionFilter) => void;
  onReset: () => void;
}

export const TransactionFilters = ({
  filters,
  terminals,
  franchises,
  transactionTypes,
  mtis,
  statusValues,
  loading,
  onFiltersChange,
  onReset,
}: TransactionFiltersProps) => {


  const [terminalOpen, setTerminalOpen] = useState(false);

  const updateFilter = (key: keyof TransactionFilter, value: any) => {
    // If value is "all", treat it as undefined to clear the filter
    const actualValue = value === "all" ? undefined : value;
    onFiltersChange({ ...filters, [key]: actualValue });
  };

  return (
    <Card className="p-6 shadow-card">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-foreground">Filters</h3>
        <Button variant="ghost" size="sm" onClick={onReset}>
          <X className="h-4 w-4 mr-2" />
          Clear All
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Terminal - Combobox */}
        <div className="space-y-2">
          <Label>Terminal</Label>
          <Popover open={terminalOpen} onOpenChange={setTerminalOpen}>
            <PopoverTrigger asChild>
              <Button
                variant="outline"
                role="combobox"
                aria-expanded={terminalOpen}
                className="w-full justify-between font-normal"
              >
                {filters.terminal || "All terminals"}
                <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-[200px] p-0 z-50" align="start">
              <Command>
                <CommandInput placeholder="Search terminal..." />
                <CommandList>
                  <CommandEmpty>No terminal found.</CommandEmpty>
                  <CommandGroup>
                    <CommandItem
                      value="all"
                      onSelect={() => {
                        updateFilter("terminal", undefined);
                        setTerminalOpen(false);
                      }}
                    >
                      <Check
                        className={cn(
                          "mr-2 h-4 w-4",
                          !filters.terminal ? "opacity-100" : "opacity-0"
                        )}
                      />
                      All terminals
                    </CommandItem>
                    {terminals.map((terminal) => (
                      <CommandItem
                        key={terminal}
                        value={terminal}
                        onSelect={() => {
                          updateFilter("terminal", terminal);
                          setTerminalOpen(false);
                        }}
                      >
                        <Check
                          className={cn(
                            "mr-2 h-4 w-4",
                            filters.terminal === terminal ? "opacity-100" : "opacity-0"
                          )}
                        />
                        {terminal}
                      </CommandItem>
                    ))}
                  </CommandGroup>
                </CommandList>
              </Command>
            </PopoverContent>
          </Popover>
        </div>

        {/* Franchise */}
        <div className="space-y-2">
          <Label htmlFor="franchise">Franchise</Label>
          <Select
            value={filters.franchise || "all"}
            onValueChange={(value) => updateFilter("franchise", value)}
            disabled={loading}
          >
            <SelectTrigger id="franchise">
              <SelectValue placeholder="All franchises" />
            </SelectTrigger>
            <SelectContent className="bg-popover z-50">
              <SelectItem value="all">All franchises</SelectItem>
              {franchises.map((franchise) => (
                <SelectItem key={franchise} value={franchise}>
                  {franchise}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        {/* Transaction Type */}
        <div className="space-y-2">
          <Label htmlFor="transactionType">Transaction Type</Label>
          <Select
            value={filters.transactionType || "all"}
            onValueChange={(value) => updateFilter("transactionType", value)}
            disabled={loading}
          >
            <SelectTrigger>
              <SelectValue placeholder="All types" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All types</SelectItem>
              {transactionTypes.map((type) => (
                <SelectItem key={type} value={type}>
                  {type}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        {/* MTI */}
        <div className="space-y-2">
          <Label htmlFor="mti">MTI</Label>
          <Select value={filters.mti || "all"} onValueChange={(value) => updateFilter("mti", value)}>
            <SelectTrigger id="mti">
              <SelectValue placeholder="All MTIs" />
            </SelectTrigger>
            <SelectContent className="bg-popover z-50">
              <SelectItem value="all">All MTIs</SelectItem>
              {mtis.map((mti) => (
                <SelectItem key={mti} value={mti}>
                  {mti}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        {/* Status */}
        <div className="space-y-2">
          <Label htmlFor="status">Status</Label>
          <Select value={filters.status || "all"} onValueChange={(value) => updateFilter("status", value)}>
            <SelectTrigger id="status">
              <SelectValue placeholder="All statuses" />
            </SelectTrigger>
            <SelectContent className="bg-popover z-50">
              <SelectItem value="all">All statuses</SelectItem>
              {statusValues.map((status) => (
                <SelectItem key={status} value={status}>
                  {status}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        {/* Response Code P39 */}
        <div className="space-y-2">
          <Label htmlFor="responseCode">Response (P39)</Label>

          <Input
            id="responseCode"
            type="text"
            placeholder="example: 00"
            disabled={!!filters.responseCodeEmpty}
            value={filters.responseCode || ""}
            onChange={(e) =>
              updateFilter("responseCode", e.target.value || undefined)
            }
          />

          <div className="flex items-center gap-2">
            <Checkbox
              checked={!!filters.responseCodeEmpty}
              onCheckedChange={(checked) => {
                const isChecked = checked === true;

                if (isChecked) {
                  onFiltersChange({
                    ...filters,
                    responseCodeEmpty: true,
                    responseCode: undefined, // 🔥 limpia valor
                  });
                } else {
                  onFiltersChange({
                    ...filters,
                    responseCodeEmpty: undefined,
                  });
                }
              }}
            />
            <Label>Only empty</Label>
          </div>
        </div>

        {/* Auth Code P38 */}
        <div className="space-y-2">
          <Label htmlFor="authCode">Auth Code (P38)</Label>
          <Input
            disabled={filters.authCodeEmpty}
            id="authCode"
            type="text"
            placeholder="example: 123456"
            value={filters.authCode || ""}
            onChange={(e) => updateFilter("authCode", e.target.value || undefined)}
          />
          <div className="flex items-center gap-2">
            <Checkbox
              checked={!!filters.authCodeEmpty}
              onCheckedChange={(checked) => {
                const isChecked = checked === true;

                if (isChecked) {
                  onFiltersChange({
                    ...filters,
                    authCodeEmpty: true,
                    authCode: undefined, // limpia el valor
                  });
                } else {
                  onFiltersChange({
                    ...filters,
                    authCodeEmpty: undefined,
                  });
                }
              }}
            />
            <Label>Only empty</Label>
          </div>
        </div>

        {/* RRN P37 */}
        <div className="space-y-2">
          <Label htmlFor="rrn">RRN (P37)</Label>

          <Input
            id="rrn"
            type="text"
            placeholder="example: 123456789012"
            disabled={!!filters.rrnEmpty}
            value={filters.rrn || ""}
            onChange={(e) =>
              updateFilter("rrn", e.target.value || undefined)
            }
          />

          <div className="flex items-center gap-2">
            <Checkbox
              checked={!!filters.rrnEmpty}
              onCheckedChange={(checked) => {
                const isChecked = checked === true;

                if (isChecked) {
                  onFiltersChange({
                    ...filters,
                    rrnEmpty: true,
                    rrn: undefined, // 🔥 limpia valor
                  });
                } else {
                  onFiltersChange({
                    ...filters,
                    rrnEmpty: undefined,
                  });
                }
              }}
            />
            <Label>Only empty</Label>
          </div>
        </div>


        {/* Date From */}
        <div className="space-y-2">
          <Label htmlFor="dateFrom">Date From</Label>
          <Input
            id="dateFrom"
            type="date"
            value={filters.dateFrom || ""}
            onChange={(e) => updateFilter("dateFrom", e.target.value)}
          />
        </div>

        {/* Date To */}
        <div className="space-y-2">
          <Label htmlFor="dateTo">Date To</Label>
          <Input
            id="dateTo"
            type="date"
            value={filters.dateTo || ""}
            onChange={(e) => updateFilter("dateTo", e.target.value)}
          />
        </div>

        {/* Amount From */}
        <div className="space-y-2">
          <Label htmlFor="amountFrom">Amount From</Label>
          <Input
            id="amountFrom"
            type="number"
            placeholder="0.00"
            value={filters.amountFrom || ""}
            onChange={(e) => updateFilter("amountFrom", parseFloat(e.target.value) || undefined)}
          />
        </div>

        {/* Amount To */}
        <div className="space-y-2">
          <Label htmlFor="amountTo">Amount To</Label>
          <Input
            id="amountTo"
            type="number"
            placeholder="0.00"
            value={filters.amountTo || ""}
            onChange={(e) => updateFilter("amountTo", parseFloat(e.target.value) || undefined)}
          />
        </div>

        {/* Search Text */}
        <div className="space-y-2 md:col-span-2 lg:col-span-4">
          <Label htmlFor="searchText">Search (ID, Data Elements, etc.)</Label>
          <Input
            id="searchText"
            type="text"
            placeholder="Search transactions..."
            value={filters.searchText || ""}
            onChange={(e) => updateFilter("searchText", e.target.value)}
          />
        </div>
      </div>
    </Card>
  );
}
