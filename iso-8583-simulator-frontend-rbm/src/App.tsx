import { Toaster } from '@/components/ui/toaster';
import { Toaster as Sonner } from '@/components/ui/sonner';
import { TooltipProvider } from '@/components/ui/tooltip';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { SidebarProvider, SidebarTrigger } from '@/components/ui/sidebar';
import { AppSidebar } from '@/components/AppSidebar';
import { ThemeProvider } from 'next-themes';
import { ThemeToggle } from '@/components/ThemeToggle';

import { Play, RotateCcw, Square } from 'lucide-react';
import { Button } from '@/components/ui/button';
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/ui/tooltip';

import Home from './pages/HomePage';
import Transactions from './pages/TransactionsPage';
import Settings from './pages/SettingsPage';
import NotFound from './pages/NotFoundPage';

import { startNetty, stopNetty, restartNetty } from '@/lib/nettyApi';
import { useToast } from '@/hooks/use-toast';

const queryClient = new QueryClient();

const App = () => {
  
  const { toast } = useToast();

  const handleStart = async () => {
    try {
      const msg = await startNetty();
      toast({ title: "Server started", description: msg });
    } catch (e: any) {
      toast({ title: "Error", description: e.message, variant: "destructive" });
    }
  };

  const handleStop = async () => {
    try {
      const msg = await stopNetty();
      toast({ title: "Server stopped", description: msg });
    } catch (e: any) {
      toast({ title: "Error", description: e.message, variant: "destructive" });
    }
  };

  const handleRestart = async () => {
    try {
      const msg = await restartNetty();
      toast({ title: "Server restarting", description: msg });
    } catch (e: any) {
      toast({ title: "Error", description: e.message, variant: "destructive" });
    }
  };

  return (
    <ThemeProvider attribute="class" defaultTheme="system" enableSystem>
      <QueryClientProvider client={queryClient}>
        <TooltipProvider>
          <Toaster />
          <Sonner />
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
                      </div>
                      <div className="flex items-center gap-1">
                        <Tooltip>
                          <TooltipTrigger asChild>
                            <Button
                              variant="ghost"
                              size="icon"
                              className="h-8 w-8 text-success hover:text-success hover:bg-success/10"
                              onClick={handleStart}
                            >
                              <Play className="h-4 w-4" />
                            </Button>
                          </TooltipTrigger>
                          <TooltipContent>Start server</TooltipContent>
                        </Tooltip>
                        <Tooltip>
                          <TooltipTrigger asChild>
                            <Button
                              variant="ghost"
                              size="icon"
                              className="h-8 w-8 text-warning hover:text-warning hover:bg-warning/10"
                              onClick={handleRestart}
                            >
                              <RotateCcw className="h-4 w-4" />
                            </Button>
                          </TooltipTrigger>
                          <TooltipContent>Restart server</TooltipContent>
                        </Tooltip>
                        <Tooltip>
                          <TooltipTrigger asChild>
                            <Button
                              variant="ghost"
                              size="icon"
                              className="h-8 w-8 text-destructive hover:text-destructive hover:bg-destructive/10"
                              onClick={handleStop}
                            >
                              <Square className="h-4 w-4" />
                            </Button>
                          </TooltipTrigger>
                          <TooltipContent>Stop server</TooltipContent>
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
        </TooltipProvider>
      </QueryClientProvider>
    </ThemeProvider>
  );
};

export default App;
