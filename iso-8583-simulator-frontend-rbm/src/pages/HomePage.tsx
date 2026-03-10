import { useState, useEffect, useRef } from 'react';
import { StatsCard } from '@/components/StatsCard';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Calendar } from '@/components/ui/calendar';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { Activity, CalendarIcon } from 'lucide-react';
import { Skeleton } from '@/components/ui/skeleton';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { format, isSameDay } from 'date-fns';
import { es } from 'date-fns/locale';
import { DateRange } from 'react-day-picker';
import { cn } from '@/lib/utils';
import { useLanguage } from '@/i18n/LanguageContext';

type StatisticsResponse = {
  totalTransactions: number;
  series: { bucket: string; total: number }[];
};

const Home = () => {
  const { t, language } = useLanguage();
  const [dateRange, setDateRange] = useState<DateRange | undefined>({
    from: new Date(),
    to: new Date(),
  });

  const [chartData, setChartData] = useState<{ label: string; transactions: number }[]>([]);
  const [totalTransactions, setTotalTransactions] = useState(0);
  const [isSingleDay, setIsSingleDay] = useState(true);
  const [loading, setLoading] = useState(true);
  const [changed, setChanged] = useState(false);
  const prevTotalRef = useRef<number | null>(null);
  const changedTimerRef = useRef<NodeJS.Timeout | null>(null);

  const fetchStatistics = async (range: DateRange | undefined) => {
    if (!range?.from) return;
    const from = format(range.from, 'yyyy-MM-dd');
    const to = format(range.to ?? range.from, 'yyyy-MM-dd');
    const singleDay = !range.to || isSameDay(range.from, range.to);
    const groupBy = singleDay ? 'hour' : 'day';
    setIsSingleDay(singleDay);

    try {
      const response = await fetch(`/api/transactions/statistics?from=${from}&to=${to}&groupBy=${groupBy}`);
      const data: StatisticsResponse = await response.json();

      setTotalTransactions((prev) => {
        if (prevTotalRef.current !== null && data.totalTransactions !== prev) {
          if (changedTimerRef.current) clearTimeout(changedTimerRef.current);
          setChanged(true);
          changedTimerRef.current = setTimeout(() => setChanged(false), 1500);
        }
        prevTotalRef.current = data.totalTransactions;
        return data.totalTransactions;
      });

      setChartData(data.series.map((item) => ({ label: item.bucket, transactions: item.total })));
    } catch (error) {
      console.error('Error fetching statistics', error);
    } finally {
      setLoading(false);
    }
  };

  const dateLocale = language === 'es' ? es : undefined;

  const getDateRangeLabel = () => {
    if (!dateRange?.from) return t.home.selectDates;
    if (!dateRange.to || isSameDay(dateRange.from, dateRange.to)) {
      return format(dateRange.from, 'dd MMM yyyy', { locale: dateLocale });
    }
    return `${format(dateRange.from, 'dd MMM', { locale: dateLocale })} - ${format(dateRange.to, 'dd MMM yyyy', { locale: dateLocale })}`;
  };

  useEffect(() => {
    fetchStatistics(dateRange);
    const interval = setInterval(() => fetchStatistics(dateRange), 5000);
    return () => clearInterval(interval);
  }, [dateRange]);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-foreground">{t.home.dashboard}</h1>
          <p className="text-muted-foreground mt-1">{t.home.monitorActivity}</p>
        </div>
        <Popover>
          <PopoverTrigger asChild>
            <Button variant="outline" className={cn('justify-start text-left font-normal min-w-[240px]', !dateRange && 'text-muted-foreground')}>
              <CalendarIcon className="mr-2 h-4 w-4" />
              {getDateRangeLabel()}
            </Button>
          </PopoverTrigger>
          <PopoverContent className="w-auto p-0" align="end">
            <Calendar initialFocus mode="range" defaultMonth={dateRange?.from} selected={dateRange} onSelect={setDateRange} numberOfMonths={2} disabled={(date) => date > new Date()} />
          </PopoverContent>
        </Popover>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {loading ? (
          <Card className="p-6 shadow-card space-y-3">
            <Skeleton className="h-4 w-32" />
            <Skeleton className="h-8 w-20" />
          </Card>
        ) : (
          <StatsCard
            title={isSingleDay ? t.home.totalTransactionsToday : t.home.totalTransactions}
            value={totalTransactions.toLocaleString()}
            icon={Activity}
            changed={changed}
          />
        )}
      </div>

      <Card className="p-6 shadow-card">
        <div className="mb-6">
          <h2 className="text-xl font-semibold text-foreground">{t.home.transactionsOverTime}</h2>
          <p className="text-sm text-muted-foreground mt-1">
            {isSingleDay ? t.home.volumeDay : t.home.volumePerDay}
          </p>
        </div>
        {loading ? (
          <div className="h-80 flex flex-col gap-4 justify-center items-center">
            <Skeleton className="h-full w-full rounded-lg" />
          </div>
        ) : (
          <div className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                <XAxis dataKey="label" stroke="hsl(var(--muted-foreground))" style={{ fontSize: "12px" }} interval={isSingleDay ? 0 : "preserveStartEnd"} padding={{ left: 10, right: 10 }} />
                <YAxis stroke="hsl(var(--muted-foreground))" style={{ fontSize: '12px' }} />
                <Tooltip
                  contentStyle={{ backgroundColor: 'hsl(var(--card))', border: '1px solid hsl(var(--border))', borderRadius: '8px' }}
                  labelFormatter={(label) => isSingleDay ? `${t.home.hour}: ${label}` : `${t.home.date}: ${label}`}
                  formatter={(value: number) => [value.toLocaleString(), t.home.transactions]}
                />
                <Line type="monotone" dataKey="transactions" stroke="hsl(var(--primary))" strokeWidth={3} dot={{ fill: 'hsl(var(--primary))', r: isSingleDay ? 3 : 4 }} activeDot={{ r: 6 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        )}
      </Card>
    </div>
  );
};

export default Home;
