import { Button } from "@/components/ui/button";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
import { useLanguage } from "@/i18n/LanguageContext";

export function LanguageToggle() {
  const { language, setLanguage } = useLanguage();

  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <Button
          variant="ghost"
          size="icon"
          className="h-8 w-8 text-xs font-bold"
          onClick={() => setLanguage(language === 'es' ? 'en' : 'es')}
        >
          {language === 'es' ? 'EN' : 'ES'}
        </Button>
      </TooltipTrigger>
      <TooltipContent>
        {language === 'es' ? 'Switch to English' : 'Cambiar a Español'}
      </TooltipContent>
    </Tooltip>
  );
}
