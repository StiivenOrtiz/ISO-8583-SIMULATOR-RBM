import { useState, useEffect } from "react";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Input } from "@/components/ui/input";
import { Search } from "lucide-react";
import { useLanguage } from "@/i18n/LanguageContext";

interface Terminal {
  idDB: string;
  terminalID: string;
}

interface TerminalsTableProps {
  terminals: Terminal[];
  delayConfig: { terminalDelays: { [terminalId: string]: number } };
  responseConfig: { terminalResponses: { [terminalId: string]: string } };
  onTerminalSelect: (terminalId: string) => void;
}

export const TerminalsTable = ({ terminals, delayConfig, responseConfig, onTerminalSelect }: TerminalsTableProps) => {
  const { t } = useLanguage();
  const [delayFilter, setDelayFilter] = useState(false);
  const [responseFilter, setResponseFilter] = useState(false);
  const [searchText, setSearchText] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 5;

  const filteredTerminals = terminals.filter((terminal) => {
    const delay = delayConfig.terminalDelays[terminal.terminalID] ?? 0;
    const response = responseConfig.terminalResponses[terminal.terminalID] ?? "";
    if (searchText && !terminal.terminalID.toLowerCase().includes(searchText.toLowerCase())) return false;
    if (delayFilter && delay === 0) return false;
    if (responseFilter && response === "") return false;
    return true;
  });

  const totalPages = Math.max(1, Math.ceil(filteredTerminals.length / itemsPerPage));

  useEffect(() => {
    if (currentPage > totalPages) setCurrentPage(totalPages);
  }, [currentPage, totalPages]);

  const validPage = Math.min(currentPage, totalPages);
  const startIndex = (validPage - 1) * itemsPerPage;
  const currentTerminals = filteredTerminals.slice(startIndex, startIndex + itemsPerPage);

  return (
    <div className="space-y-4">
      <div className="relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input placeholder={t.settings.searchTerminalIdPlaceholder} value={searchText} onChange={(e) => { setSearchText(e.target.value); setCurrentPage(1); }} className="pl-9" />
      </div>

      <div className="flex items-center gap-6 p-4 border-2 border-border rounded-lg bg-background">
        <div className="flex items-center gap-3">
          <Label className="text-sm font-medium">{t.settings.delayActive}</Label>
          <Switch checked={delayFilter} onCheckedChange={(c) => { setDelayFilter(c); setCurrentPage(1); }} />
        </div>
        <div className="flex items-center gap-3">
          <Label className="text-sm font-medium">{t.settings.responseActive}</Label>
          <Switch checked={responseFilter} onCheckedChange={(c) => { setResponseFilter(c); setCurrentPage(1); }} />
        </div>
      </div>

      <div className="border-2 border-border rounded-lg overflow-hidden bg-background">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/30">
              <TableHead className="font-semibold">{t.settings.terminalId}</TableHead>
              <TableHead className="text-center font-semibold">{t.settings.delayActive}</TableHead>
              <TableHead className="text-center font-semibold">{t.settings.delayValue}</TableHead>
              <TableHead className="text-center font-semibold">{t.settings.responseActive}</TableHead>
              <TableHead className="text-center font-semibold">{t.settings.responseValue}</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {currentTerminals.length > 0 ? (
              currentTerminals.map((terminal) => {
                const delay = delayConfig.terminalDelays[terminal.terminalID] ?? 0;
                const response = responseConfig.terminalResponses[terminal.terminalID] ?? "";
                return (
                  <TableRow key={terminal.idDB} className="cursor-pointer hover:bg-muted/50 transition-colors" onClick={() => onTerminalSelect(terminal.terminalID)}>
                    <TableCell className="font-medium">{terminal.terminalID}</TableCell>
                    <TableCell className="text-center">
                      <span className={`inline-block w-3 h-3 rounded-full ${delay !== 0 ? "bg-primary" : "bg-muted"}`} />
                    </TableCell>
                    <TableCell className="text-center">{delay}s</TableCell>
                    <TableCell className="text-center">
                      <span className={`inline-block w-3 h-3 rounded-full ${response !== "" ? "bg-primary" : "bg-muted"}`} />
                    </TableCell>
                    <TableCell className="text-center">{response || "—"}</TableCell>
                  </TableRow>
                );
              })
            ) : (
              <TableRow>
                <TableCell colSpan={5} className="text-center text-muted-foreground py-8">{t.settings.noTerminalsFound}</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      {filteredTerminals.length > 0 && (
        <div className="flex items-center justify-center gap-4">
          <Button variant="outline" size="sm" onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))} disabled={currentPage === 1}>
            {t.tx.previous}
          </Button>
          <span className="text-sm text-muted-foreground">
            {t.tx.page} {Math.min(currentPage, totalPages)} {t.tx.of} {totalPages}
          </span>
          <Button variant="outline" size="sm" onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages))} disabled={currentPage === totalPages}>
            {t.tx.next}
          </Button>
        </div>
      )}
    </div>
  );
};
