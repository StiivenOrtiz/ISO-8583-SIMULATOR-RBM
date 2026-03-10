import { Home, ArrowLeftRight, Settings } from "lucide-react";
import { NavLink } from "@/components/NavLink";
import { useLocation } from "react-router-dom";
import { Sidebar, SidebarContent, SidebarGroup, SidebarGroupContent, SidebarGroupLabel, SidebarMenu, SidebarMenuButton, SidebarMenuItem, useSidebar } from "@/components/ui/sidebar";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";
import { useLanguage } from "@/i18n/LanguageContext";
import logoIso from "@/assets/logoIso.png";

export function AppSidebar() {
  const { open } = useSidebar();
  const location = useLocation();
  const { t } = useLanguage();

  const menuItems = [
    { title: t.sidebar.home, url: "/", icon: Home },
    { title: t.sidebar.transactions, url: "/transactions", icon: ArrowLeftRight },
    { title: t.sidebar.settings, url: "/settings", icon: Settings },
  ];

  const isActive = (path: string) => {
    if (path === "/") return location.pathname === "/";
    return location.pathname.startsWith(path);
  };

  return (
    <Sidebar className={open ? "w-60" : "w-14"}>
      <SidebarContent>
        <div className="px-6 py-6 border-b border-sidebar-border">
          {open ? (
            <div className="flex flex-col items-center gap-3">
              <Avatar className="h-16 w-16">
                <AvatarImage src={logoIso} alt="Transaction Simulator RBM" />
                <AvatarFallback>RBM</AvatarFallback>
              </Avatar>
              <div className="text-center">
                <h2 className="text-lg font-bold text-sidebar-foreground">{t.sidebar.title}</h2>
                <p className="text-xs text-sidebar-foreground/60">{t.sidebar.subtitle}</p>
              </div>
            </div>
          ) : (
            <div className="flex justify-center">
              <Avatar className="h-10 w-10">
                <AvatarImage src={logoIso} alt="Transaction Simulator RBM" />
                <AvatarFallback>RBM</AvatarFallback>
              </Avatar>
            </div>
          )}
        </div>

        <SidebarGroup className="mt-4 flex-1">
          <SidebarGroupLabel className={!open ? "sr-only" : ""}>{t.sidebar.navigation}</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {menuItems.map(item => (
                <SidebarMenuItem key={item.url}>
                  <SidebarMenuButton asChild>
                    <NavLink to={item.url} className="hover:bg-sidebar-accent transition-colors" activeClassName="bg-sidebar-accent text-sidebar-primary font-medium">
                      <item.icon className={open ? "mr-2 h-4 w-4" : "h-4 w-4"} />
                      {open && <span>{item.title}</span>}
                    </NavLink>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>

        <div className="mt-auto border-t border-sidebar-border px-4 py-4">
          {open && <p className="text-xs text-sidebar-foreground/50 mb-2">{t.sidebar.developedBy}</p>}
          <a
            href="https://github.com/StiivenOrtiz/"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-2 text-sm text-sidebar-foreground/60 hover:text-sidebar-foreground transition-colors"
          >
            <svg className="h-4 w-4" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
            </svg>
            {open && <span>@StiivenOrtiz</span>}
          </a>
        </div>
      </SidebarContent>
    </Sidebar>
  );
}
