import { Toaster } from '@/components/ui/toaster';
import { Toaster as Sonner } from '@/components/ui/sonner';
import { TooltipProvider } from '@/components/ui/tooltip';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { SidebarProvider, SidebarTrigger } from '@/components/ui/sidebar';
import { AppSidebar } from '@/components/AppSidebar';
import { ThemeProvider } from 'next-themes';
import { ThemeToggle } from '@/components/ThemeToggle';
import { LanguageToggle } from '@/components/LanguageToggle';
import { LanguageProvider, useLanguage } from '@/i18n/LanguageContext';

import { Play, RotateCcw, Square, Activity } from 'lucide-react';
import { Button } from '@/components/ui/button';
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/ui/tooltip';
import { Badge } from '@/components/ui/badge';

import Home from './pages/HomePage';
import Transactions from './pages/TransactionsPage';
import Settings from './pages/SettingsPage';
import NotFound from './pages/NotFoundPage';

import { startNetty, stopNetty, restartNetty } from '@/lib/nettyApi';
import { useNettyStatus } from '@/hooks/useNettyStatus';
import { useToast } from '@/hooks/use-toast';

const queryClient = new QueryClient();

const AppContent = () => {
  const { toast } = useToast();
  const { t } = useLanguage();
  const { status, error: statusError, refetch } = useNettyStatus(5000);

  const isRunning = status?.running ?? false;

  const handleStart = async () => {
    try {
      const res = await startNetty();
      toast({
        title: t.header.serverStarted,
        description: `${t.header.connections}: ${res.connections}`,
      });
      refetch();
    } catch (e: any) {
      toast({ title: "Error", description: e.message, variant: "destructive" });
    }
  };

  const handleStop = async () => {
    try {
      const res = await stopNetty();
      toast({
        title: t.header.serverStopped,
        description: `${t.header.connections}: ${res.connections}`,
      });
      refetch();
    } catch (e: any) {
      toast({ title: "Error", description: e.message, variant: "destructive" });
    }
  };

  const handleRestart = async () => {
    try {
      const res = await restartNetty();
      toast({
        title: t.header.serverRestarting,
        description: `${t.header.connections}: ${res.connections}`,
      });
      refetch();
    } catch (e: any) {
      toast({ title: "Error", description: e.message, variant: "destructive" });
    }
  };

  return (
    <BrowserRouter>
      <SidebarProvider>
        <div className="min-h-screen flex w-full">
          <AppSidebar />
          <div className="flex-1 flex flex-col">
            <main className="flex-1 p-6 bg-background">
              <header className="h-10 border-b border-border bg-card flex items-center justify-between px-6 sticky top-0 z-0">
                <div className="flex items-center gap-2">
                  <SidebarTrigger />
                  <ThemeToggle />
                  <LanguageToggle />
                </div>

                {/* Server status indicator */}
                <div className="flex items-center gap-2">
                  <Activity className="h-3.5 w-3.5 text-muted-foreground" />
                  {statusError ? (
                    <Badge variant="outline" className="text-xs font-normal text-muted-foreground border-muted">
                      {t.header.statusUnknown}
                    </Badge>
                  ) : isRunning ? (
                    <Badge variant="outline" className="text-xs font-normal text-success border-success/30 bg-success/10">
                      <span className="mr-1.5 h-1.5 w-1.5 rounded-full bg-success inline-block animate-pulse" />
                      {t.header.serverOnline}
                      {status && ` · ${status.connections} ${t.header.connections.toLowerCase()}`}
                    </Badge>
                  ) : (
                    <Badge variant="outline" className="text-xs font-normal text-destructive border-destructive/30 bg-destructive/10">
                      <span className="mr-1.5 h-1.5 w-1.5 rounded-full bg-destructive inline-block" />
                      {t.header.serverOffline}
                    </Badge>
                  )}
                </div>

                <div className="flex items-center gap-1">
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8 text-success hover:text-success hover:bg-success/10 disabled:opacity-40"
                        onClick={handleStart}
                        disabled={isRunning}
                      >
                        <Play className="h-4 w-4" />
                      </Button>
                    </TooltipTrigger>
                    <TooltipContent>{t.header.startServer}</TooltipContent>
                  </Tooltip>
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8 text-warning hover:text-warning hover:bg-warning/10 disabled:opacity-40"
                        onClick={handleRestart}
                        disabled={!isRunning}
                      >
                        <RotateCcw className="h-4 w-4" />
                      </Button>
                    </TooltipTrigger>
                    <TooltipContent>{t.header.restartServer}</TooltipContent>
                  </Tooltip>
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8 text-destructive hover:text-destructive hover:bg-destructive/10 disabled:opacity-40"
                        onClick={handleStop}
                        disabled={!isRunning}
                      >
                        <Square className="h-4 w-4" />
                      </Button>
                    </TooltipTrigger>
                    <TooltipContent>{t.header.stopServer}</TooltipContent>
                  </Tooltip>
                </div>
              </header>
              <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/transactions" element={<Transactions />} />
                <Route path="/settings" element={<Settings />} />
                <Route path="*" element={<NotFound />} />
              </Routes>
            </main>
          </div>
        </div>
      </SidebarProvider>
    </BrowserRouter>
  );
};

const App = () => {
  return (
    <ThemeProvider attribute="class" defaultTheme="system" enableSystem>
      <LanguageProvider>
        <QueryClientProvider client={queryClient}>
          <TooltipProvider>
            <Toaster />
            <Sonner />
            <AppContent />
          </TooltipProvider>
        </QueryClientProvider>
      </LanguageProvider>
    </ThemeProvider>
  );
};

export default App;
