import { useState } from "react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";
import { Plus, Trash2, Save, Check, ChevronsUpDown } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from "@/components/ui/command";
import { cn } from "@/lib/utils";
import { TerminalsTable } from "@/components/TerminalsTable";
import { Skeleton } from "@/components/ui/skeleton";
import { useEffect } from "react";
import { apiGet, apiPost, ApiTerminal, ApiConfigResponse } from "@/lib/api";

interface Terminal {
  idDB: string;
  terminalID: string;
}

interface DelayConfig {
  globalEnabled: boolean;
  globalValue: number;
  terminalDelays: { [terminalId: string]: number };
}

interface ResponseConfig {
  globalEnabled: boolean;
  globalValue: string;
  terminalResponses: { [terminalId: string]: string };
}

const Settings = () => {
  const { toast } = useToast();
  
  // Terminals state
  const [terminals, setTerminals] = useState<Terminal[]>([]);
  const [loadingSettings, setLoadingSettings] = useState(true);

  // Delay configuration state
  const [delayConfig, setDelayConfig] = useState<DelayConfig>({
    globalEnabled: false,
    globalValue: 0,
    terminalDelays: {},
  });

  // Response configuration state
  const [responseConfig, setResponseConfig] = useState<ResponseConfig>({
    globalEnabled: false,
    globalValue: "",
    terminalResponses: {},
  });

  const loadTerminals = async () => {
    try {
      setLoadingSettings(true);
      const data = await apiGet<ApiConfigResponse>("/terminals/getallconfig");

      console.log("Loaded terminals data:", data);

      const parsedTerminals: Terminal[] = data.terminals.map(t => ({
        idDB: t.idDB,
        terminalID: t.terminalID,
      }));

      setTerminals(parsedTerminals);

      const terminalDelays: Record<string, number> = {};
      data.terminals.forEach(t => {
        terminalDelays[t.terminalID] = t.delay;
      });

      const terminalResponses: Record<string, string> = {};
      data.terminals.forEach(t => {
        terminalResponses[t.terminalID] = t.responseCode;
      });

      setDelayConfig({
        globalEnabled: data.globalDelay,
        globalValue: data.globalDelayValue,
        terminalDelays,
      });

      setResponseConfig({
        globalEnabled: data.globalResponseCode,
        globalValue: data.globalResponseCodeValue,
        terminalResponses,
      });

    } catch (error) {
      console.error("Error loading terminals", error);
      toast({
        title: "Error",
        description: "Failed to load terminals",
        variant: "destructive",
      });
    } finally {
      setLoadingSettings(false);
    }
  };


  useEffect(() => {
    loadTerminals();
  }, []);

  const [newTerminalId, setNewTerminalId] = useState("");
  const [selectedTerminalForBrowse, setSelectedTerminalForBrowse] = useState<string>("");
  const [openBrowse, setOpenBrowse] = useState(false);

  const [selectedTerminalForDelay, setSelectedTerminalForDelay] = useState<string>("");
  const [openDelay, setOpenDelay] = useState(false);

  
  const [selectedTerminalForResponse, setSelectedTerminalForResponse] = useState<string>("");
  const [openResponse, setOpenResponse] = useState(false);

  const [draftDelay, setDraftDelay] = useState<number | "">("");
  const [draftResponse, setDraftResponse] = useState<string>("");

  const hasUnsavedDelay =
    !!selectedTerminalForDelay && draftDelay !== (delayConfig.terminalDelays[selectedTerminalForDelay] ?? 0);

  const hasUnsavedResponse =
    !!selectedTerminalForResponse && draftResponse !== (responseConfig.terminalResponses[selectedTerminalForResponse] ?? "");

  const handleSelectTerminalForDelay = (terminalID: string) => {
    setSelectedTerminalForDelay(terminalID);

    setDraftDelay(
      delayConfig.terminalDelays[terminalID] ?? 0
    );
  };

  const handleSelectTerminalForResponse = (terminalID: string) => {
    setSelectedTerminalForResponse(terminalID);

    setDraftResponse(
      responseConfig.terminalResponses[terminalID] ?? ""
    );
  };

  // Terminal actions (immediate)
  const addTerminal = async () => {
    if (!newTerminalId.trim()) return;

    try {
      await apiPost(`/terminals/add?terminal=${newTerminalId.trim()}`);
      await loadTerminals();

      setNewTerminalId("");

      toast({
        title: "Terminal added",
        description: "Terminal created successfully",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to create terminal",
        variant: "destructive",
      });
    }
  };


  const removeTerminal = async (id: string) => {
    const idDB = terminals.find(t => t.terminalID === id)?.idDB;

    try {
      await apiPost(`/terminals/remove?id=${idDB}`);
      await loadTerminals();

      toast({
        title: "Terminal removed",
        description: `Terminal ${idDB} removed.`,
      });
    } catch (error) {
      toast({
        title: "Error",
        description: `Failed to remove terminal ${idDB}`,
        variant: "destructive",
      });
    }
  };


  // Delay configuration handlers
  const saveDelayConfig = async () => {
    try {
      if (delayConfig.globalEnabled) {
        await apiPost(
          `/terminals/globaldelay?seconds=${delayConfig.globalValue}`
        );

        toast({
          title: "Delay configuration updated",
          description: `Global delay set to ${delayConfig.globalValue} seconds`,
        });

        return;
      }

      if (!selectedTerminalForDelay || draftDelay === "") return;

      await apiPost(
        `/terminals/delay?terminal=${selectedTerminalForDelay}&seconds=${draftDelay}`
      );

      setDelayConfig(prev => ({
        ...prev,
        terminalDelays: {
          ...prev.terminalDelays,
          [selectedTerminalForDelay]: Number(draftDelay),
        },
      }));

      toast({
        title: "Delay configuration updated",
        description: `Terminal ${selectedTerminalForDelay} set with ${draftDelay} seconds of delay`,
      });
    } catch {
      toast({
        title: "Error",
        description: "Failed to save delay configuration",
        variant: "destructive",
      });
    }
  };




  // Response configuration handlers
  const saveResponseConfig = async () => {
    try {
      if (responseConfig.globalEnabled) {
        await apiPost(
          `/terminals/globalresponsecode?code=${responseConfig.globalValue}`
        );

        toast({
          title: "Response configuration updated",
          description: `Global response set to ${responseConfig.globalValue}`,
        });

        return;
      }

      if (!selectedTerminalForResponse || !draftResponse) return;

      await apiPost(
        `/terminals/responsecode?terminal=${selectedTerminalForResponse}&code=${draftResponse}`
      );

      setResponseConfig(prev => ({
        ...prev,
        terminalResponses: {
          ...prev.terminalResponses,
          [selectedTerminalForResponse]: draftResponse,
        },
      }));

      toast({
        title: "Response configuration updated",
        description: `Terminal ${selectedTerminalForResponse} set with response code ${draftResponse}`,
      });
    } catch {
      toast({
        title: "Error",
        description: "Failed to save response configuration",
        variant: "destructive",
      });
    }
  };

  // Handle terminal selection from table
  const handleTerminalSelectFromTable = (terminalId: string) => {
    handleSelectTerminalForDelay(terminalId);
    handleSelectTerminalForResponse(terminalId);

    setSelectedTerminalForBrowse(terminalId);
    setSelectedTerminalForDelay(terminalId);
    setSelectedTerminalForResponse(terminalId);
  };

  return (
    <div className="space-y-6 pb-8">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Settings</h1>
        <p className="text-muted-foreground mt-1">Configure terminals and transaction behavior</p>
      </div>

      {loadingSettings ? (
        <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
          {Array.from({ length: 3 }).map((_, i) => (
            <Card key={i} className="shadow-sm p-6 space-y-4">
              <Skeleton className="h-6 w-32 mx-auto" />
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-3/4" />
            </Card>
          ))}
        </div>
      ) : (
      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        {/* Terminals Section */}
        <Card className="shadow-sm">
          <CardHeader className="border-b bg-muted/30">
            <CardTitle className="text-lg text-center">Terminals</CardTitle>
          </CardHeader>
          <CardContent className="p-6 space-y-6">
            {/* Add Terminal */}
            <div className="p-4 border-2 border-border rounded-lg">
              <div className="flex items-center gap-2">
                <Input
                  placeholder="Enter Terminal ID"
                  value={newTerminalId}
                  maxLength={8}
                  onChange={(e) => {
                    const value = e.target.value;
                    if (/^[a-zA-Z0-9]*$/.test(value)) {
                      setNewTerminalId(value);
                    }
                  }}
                  onKeyDown={(e) => e.key === "Enter" && addTerminal()}
                  className="flex-1"
                />
                <Button
                  onClick={addTerminal}
                  size="icon"
                  className="flex-shrink-0 h-10 w-10 rounded-full"
                >
                  <Plus className="h-5 w-5" />
                </Button>
              </div>
            </div>

            {/* Browse Terminals */}
            <div className="p-4 border-2 border-border rounded-lg">
              <div className="flex items-center gap-2 min-w-0">
                <Popover open={openBrowse} onOpenChange={setOpenBrowse}>
                  <PopoverTrigger asChild>
                    <Button
                      variant="outline"
                      role="combobox"
                      aria-expanded={openBrowse}
                      className="flex-1 justify-between min-w-0"
                    >
                      <span className="truncate">
                        {selectedTerminalForBrowse
                          ? terminals.find((terminal) => terminal.terminalID === selectedTerminalForBrowse)?.terminalID
                          : "Search Terminal ID"}
                      </span>
                      <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-[var(--radix-popover-trigger-width)] max-w-[300px] p-0" align="start" sideOffset={4}>
                    <Command>
                      <CommandInput placeholder="Search terminal..." />
                      <CommandList>
                        <CommandEmpty>No terminal found.</CommandEmpty>
                        <CommandGroup>
                          {terminals.map((terminal) => (
                            <CommandItem
                              key={terminal.idDB}
                              value={terminal.terminalID} 
                              onSelect={(currentValue) => {
                                setSelectedTerminalForBrowse(currentValue === selectedTerminalForBrowse ? "" : currentValue);
                                setOpenBrowse(false);
                              }}
                            >
                              <Check
                                className={cn(
                                  "mr-2 h-4 w-4",
                                  selectedTerminalForBrowse === terminal.terminalID
                                    ? "opacity-100"
                                    : "opacity-0"
                                )}
                              />
                              {terminal.terminalID}
                            </CommandItem>
                          ))}
                        </CommandGroup>
                      </CommandList>
                    </Command>
                  </PopoverContent>
                </Popover>
                <Button
                  onClick={() =>
                    selectedTerminalForBrowse && removeTerminal(selectedTerminalForBrowse)
                  }
                  size="icon"
                  variant="destructive"
                  disabled={!selectedTerminalForBrowse}
                  className="flex-shrink-0 h-10 w-10"
                >
                  <Trash2 className="h-5 w-5" />
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Transaction Delays Section */}
        <Card className="shadow-sm">
          <CardHeader className="border-b bg-muted/30">
            <CardTitle className="text-lg text-center">Transaction Delays</CardTitle>
          </CardHeader>
          <CardContent className="p-6 space-y-6">
            {/* Global Delay */}
            <div className="p-4 border-2 border-border rounded-lg">
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <Label className="text-base font-medium">Global</Label>
                  <div className="flex items-center gap-3">
                    <Switch
                      checked={delayConfig.globalEnabled}
                      onCheckedChange={async (checked) => {
                        try {
                          if (checked) {
                            // 🔹 Activar global inmediatamente
                            await apiPost(
                              `/terminals/globaldelay?seconds=${delayConfig.globalValue}`
                            );

                            toast({
                              title: "Global delay enabled",
                              description: `All transactions will be delayed ${delayConfig.globalValue}s`,
                            });
                          } else {
                            // 🔹 Apagar global inmediatamente
                            await apiPost(
                              `/terminals/turnoffglobaldelay?lastSeconds=${delayConfig.globalValue}`
                            );

                            toast({
                              title: "Global delay disabled",
                              description: "Per-terminal delay configuration is now active",
                            });
                          }

                          setDelayConfig(prev => ({
                            ...prev,
                            globalEnabled: checked,
                          }));
                        } catch {
                          toast({
                            title: "Error",
                            description: "Failed to update global delay",
                            variant: "destructive",
                          });
                        }
                      }}
                    />
                    <Input
                      type="number"
                      value={delayConfig.globalValue}
                      min={0}
                      onChange={(e) =>
                        setDelayConfig({
                          ...delayConfig,
                          globalValue: Number(e.target.value),
                        })
                      }
                      className="w-20"
                    />
                    <span className="text-sm text-muted-foreground">s</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Per Terminal Delay */}
            <div className={`p-4 border-2 border-border rounded-lg transition-opacity ${delayConfig.globalEnabled ? 'opacity-50' : ''}`}>
              <div className="flex items-center gap-2 min-w-0">
                <Popover open={openDelay} onOpenChange={setOpenDelay}>
                  <PopoverTrigger asChild>
                    <Button
                      variant="outline"
                      role="combobox"
                      aria-expanded={openDelay}
                      className="flex-1 justify-between min-w-0"
                      disabled={delayConfig.globalEnabled}
                    >
                      <span className="truncate">
                        {selectedTerminalForDelay
                          ? terminals.find((terminal) => terminal.terminalID === selectedTerminalForDelay)?.terminalID
                          : "Search Terminal ID"}
                      </span>
                      <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-[var(--radix-popover-trigger-width)] max-w-[300px] p-0" align="start" sideOffset={4}>
                    <Command>
                      <CommandInput placeholder="Search terminal..." />
                      <CommandList>
                        <CommandEmpty>No terminal found.</CommandEmpty>
                        <CommandGroup>
                          {terminals.map((terminal) => (
                              <CommandItem
                                key={terminal.idDB}
                                value={terminal.terminalID}
                                onSelect={(currentValue) => {
                                  if (currentValue !== selectedTerminalForDelay) {
                                    handleSelectTerminalForDelay(currentValue);
                                  }
                                  setOpenDelay(false);
                                }}
                              >

                              <Check
                                className={cn(
                                  "mr-2 h-4 w-4",
                                  selectedTerminalForDelay === terminal.terminalID ? "opacity-100" : "opacity-0"
                                )}
                              />
                              {terminal.terminalID}
                            </CommandItem>
                          ))}
                        </CommandGroup>
                      </CommandList>
                    </Command>
                  </PopoverContent>
                </Popover>
                <Input
                  type="number"
                  min={0}
                  value={draftDelay}
                  onChange={(e) => setDraftDelay(Number(e.target.value))}
                  className="w-20"
                  disabled={delayConfig.globalEnabled || !selectedTerminalForDelay}
                  placeholder="s"
                />
                <span className="text-sm text-muted-foreground">s</span>
              </div>
            </div>

            {/* Save Button */}
            <div className="flex justify-center pt-2">
              <Button 
              onClick={() => saveDelayConfig()} 
              className="px-8">
                Save
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Transaction Responses Section */}
        <Card className="shadow-sm">
          <CardHeader className="border-b bg-muted/30">
            <CardTitle className="text-lg text-center">Transaction Responses</CardTitle>
          </CardHeader>
          <CardContent className="p-6 space-y-6">
            {/* Global Response */}
            <div className="p-4 border-2 border-border rounded-lg">
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <Label className="text-base font-medium">Global</Label>
                  <div className="flex items-center gap-3">
                    <Switch
                      checked={responseConfig.globalEnabled}
                      onCheckedChange={async (checked) => {
                        try {
                          if (checked) {
                            // 🔹 Activar global inmediatamente
                            await apiPost(
                              `/terminals/globalresponsecode?code=${responseConfig.globalValue}`
                            );

                            toast({
                              title: "Global response enabled",
                              description: `All transactions will respond with code ${responseConfig.globalValue}`,
                            });
                          } else {
                            // 🔹 Apagar global inmediatamente
                            await apiPost(
                              `/terminals/turnoffglobalresponse?lastCode=${responseConfig.globalValue}`
                            );

                            toast({
                              title: "Global response disabled",
                              description: "Per-terminal response configuration is now active",
                            });
                          }

                          setResponseConfig(prev => ({
                            ...prev,
                            globalEnabled: checked,
                          }));
                        } catch {
                          toast({
                            title: "Error",
                            description: "Failed to update global response",
                            variant: "destructive",
                          });
                        }
                      }}
                    />
                    <Input
                      type="text"
                      value={responseConfig.globalValue}
                      onChange={(e) =>{
                        const value = e.target.value;
                        if (/^[a-zA-Z0-9]*$/.test(value)){
                          setResponseConfig({
                            ...responseConfig,
                            globalValue: value,
                          })
                        }
                      }}
                      className="w-20"
                      maxLength={2}
                      placeholder="H"
                    />
                    <span className="text-sm text-muted-foreground">H</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Per Terminal Response */}
            <div className={`p-4 border-2 border-border rounded-lg transition-opacity ${responseConfig.globalEnabled ? 'opacity-50' : ''}`}>
              <div className="flex items-center gap-2 min-w-0">
                <Popover open={openResponse} onOpenChange={setOpenResponse}>
                  <PopoverTrigger asChild>
                    <Button
                      variant="outline"
                      role="combobox"
                      aria-expanded={openResponse}
                      className="flex-1 justify-between min-w-0"
                      disabled={responseConfig.globalEnabled}
                    >
                      <span className="truncate">
                        {selectedTerminalForResponse
                          ? terminals.find((terminal) => terminal.terminalID === selectedTerminalForResponse)?.terminalID
                          : "Search Terminal ID"}
                      </span>
                      <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-[var(--radix-popover-trigger-width)] max-w-[300px] p-0" align="start" sideOffset={4}>
                    <Command>
                      <CommandInput placeholder="Search terminal..." />
                      <CommandList>
                        <CommandEmpty>No terminal found.</CommandEmpty>
                        <CommandGroup>
                          {terminals.map((terminal) => (
                            <CommandItem
                              key={terminal.idDB}
                              value={terminal.terminalID}
                              onSelect={(currentValue) => {
                                if (currentValue !== selectedTerminalForResponse) {
                                  handleSelectTerminalForResponse(currentValue);
                                }
                                setOpenResponse(false);
                              }}
                            >
                              <Check
                                className={cn(
                                  "mr-2 h-4 w-4",
                                  selectedTerminalForResponse === terminal.terminalID ? "opacity-100" : "opacity-0"
                                )}
                              />
                              {terminal.terminalID}
                            </CommandItem>
                          ))}
                        </CommandGroup>
                      </CommandList>
                    </Command>
                  </PopoverContent>
                </Popover>
                <Input
                  type="text"
                  value={draftResponse}
                  onChange={(e) => {
                    const value = e.target.value;
                    if (/^[a-zA-Z0-9]*$/.test(value)) {
                      setDraftResponse(value);
                    }
                  }}
                  className="w-20"
                  disabled={responseConfig.globalEnabled || !selectedTerminalForResponse}
                  maxLength={2}
                  placeholder="H"
                />
                <span className="text-sm text-muted-foreground">H</span>
              </div>
            </div>

            {/* Save Button */}
            <div className="flex justify-center pt-2">
              <Button onClick={saveResponseConfig} className="px-8">
                Save
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
      )}

      {/* Terminals Table */}
      <Card className="shadow-sm">
        <CardHeader className="border-b bg-muted/30">
          <CardTitle className="text-lg text-center">Terminals Overview</CardTitle>
        </CardHeader>
        <CardContent className="p-6">
          <TerminalsTable
            terminals={terminals}
            delayConfig={delayConfig}
            responseConfig={responseConfig}
            onTerminalSelect={handleTerminalSelectFromTable}
          />
        </CardContent>
      </Card>
    </div>
  );
};

export default Settings;
