import { useState, useEffect } from "react";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Input } from "@/components/ui/input";
import { Search } from "lucide-react";

interface Terminal {
  idDB: string;
  terminalID: string;
}

interface TerminalsTableProps {
  terminals: Terminal[];
  delayConfig: {
    terminalDelays: { [terminalId: string]: number };
  };
  responseConfig: {
    terminalResponses: { [terminalId: string]: string };
  };
  onTerminalSelect: (terminalId: string) => void;
}

export const TerminalsTable = ({
  terminals,
  delayConfig,
  responseConfig,
  onTerminalSelect,
}: TerminalsTableProps) => {
  const [delayFilter, setDelayFilter] = useState(false);
  const [responseFilter, setResponseFilter] = useState(false);
  const [searchText, setSearchText] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 5;

  // Apply filters
  const filteredTerminals = terminals.filter((terminal) => {
    const delay = delayConfig.terminalDelays[terminal.terminalID] ?? 0;
    const response = responseConfig.terminalResponses[terminal.terminalID] ?? "";

    // Search filter
    if (searchText && !terminal.terminalID.toLowerCase().includes(searchText.toLowerCase())) {
      return false;
    }

    // Delay filter
    if (delayFilter && delay === 0) return false;
    
    // Response filter
    if (responseFilter && response === "") return false;
    
    return true;
  });

  // Calculate pagination
  const totalPages = Math.max(1, Math.ceil(filteredTerminals.length / itemsPerPage));
  
  // Ensure currentPage is within valid range
  useEffect(() => {
    if (currentPage > totalPages) {
      setCurrentPage(totalPages);
    }
  }, [currentPage, totalPages]);
  
  const validPage = Math.min(currentPage, totalPages);
  const startIndex = (validPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const currentTerminals = filteredTerminals.slice(startIndex, endIndex);

  // Reset to page 1 when filters change
  const handleDelayFilterChange = (checked: boolean) => {
    setDelayFilter(checked);
    setCurrentPage(1);
  };

  const handleResponseFilterChange = (checked: boolean) => {
    setResponseFilter(checked);
    setCurrentPage(1);
  };

  const handleSearchChange = (value: string) => {
    setSearchText(value);
    setCurrentPage(1);
  };

  return (
    <div className="space-y-4">
      {/* Search Input */}
      <div className="relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Search terminal ID..."
          value={searchText}
          onChange={(e) => handleSearchChange(e.target.value)}
          className="pl-9"
        />
      </div>

      {/* Filters */}
      <div className="flex items-center gap-6 p-4 border-2 border-border rounded-lg bg-background">
        <div className="flex items-center gap-3">
          <Label className="text-sm font-medium">Delay active</Label>
          <Switch checked={delayFilter} onCheckedChange={handleDelayFilterChange} />
        </div>
        <div className="flex items-center gap-3">
          <Label className="text-sm font-medium">Response active</Label>
          <Switch checked={responseFilter} onCheckedChange={handleResponseFilterChange} />
        </div>
      </div>

      {/* Table */}
      <div className="border-2 border-border rounded-lg overflow-hidden bg-background">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/30">
              <TableHead className="font-semibold">Terminal ID</TableHead>
              <TableHead className="text-center font-semibold">Delay active</TableHead>
              <TableHead className="text-center font-semibold">Delay value</TableHead>
              <TableHead className="text-center font-semibold">Response active</TableHead>
              <TableHead className="text-center font-semibold">Response value</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {currentTerminals.length > 0 ? (
              currentTerminals.map((terminal) => {
                const delay = delayConfig.terminalDelays[terminal.terminalID] ?? 0;
                const response = responseConfig.terminalResponses[terminal.terminalID] ?? "";

                const isDelayActive = delay !== 0;
                const isResponseActive = response !== "";

                return (
                  <TableRow
                    key={terminal.idDB}
                    className="cursor-pointer hover:bg-muted/50 transition-colors"
                    onClick={() => onTerminalSelect(terminal.terminalID)}
                  >
                    <TableCell className="font-medium">{terminal.terminalID}</TableCell>
                    <TableCell className="text-center">
                      <span
                        className={`inline-block w-3 h-3 rounded-full ${
                          isDelayActive ? "bg-primary" : "bg-muted"
                        }`}
                      />
                    </TableCell>
                    <TableCell className="text-center">{delay}s</TableCell>
                    <TableCell className="text-center">
                      <span
                        className={`inline-block w-3 h-3 rounded-full ${
                          isResponseActive ? "bg-primary" : "bg-muted"
                        }`}
                      />
                    </TableCell>
                    <TableCell className="text-center">{response || "—"}</TableCell>
                  </TableRow>
                );
              })
            ) : (
              <TableRow>
                <TableCell colSpan={5} className="text-center text-muted-foreground py-8">
                  No terminals found with the selected filters
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      {/* Pagination */}
      {filteredTerminals.length > 0 && (
        <div className="flex items-center justify-center gap-4">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
            disabled={currentPage === 1}
          >
            Previous
          </Button>
          <span className="text-sm text-muted-foreground">
            Page {Math.min(currentPage, totalPages)} of {totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages))}
            disabled={currentPage === totalPages}
          >
            Next
          </Button>
        </div>
      )}
    </div>
  );
};
