import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { UserCircle, LogOut, ChevronDown, Menu, X } from "lucide-react";

import { ThemeToggle } from "../theme-toggle";
import { Button } from "../ui/button";
import { Avatar, AvatarFallback, AvatarImage } from "../ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "../ui/dropdown-menu";
import { Separator } from "../ui/separator";
import { cn } from "@/lib/utils";

type NavItem = {
  label: string;
  href: string;
  icon: React.ElementType;
};

interface UserNavProps {
  userName: string;
  userEmail: string;
  userRole: string;
  userImage?: string;
}

export function UserNav({
  userName,
  userEmail,
  userRole,
  userImage,
}: UserNavProps) {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("authToken");
    localStorage.clear();

    navigate("/auth/login");
  };

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" className="relative h-8 w-8 rounded-full">
          <Avatar className="h-8 w-8">
            <AvatarImage src={userImage} alt={userName} />
            <AvatarFallback>{userName.charAt(0).toUpperCase()}</AvatarFallback>
          </Avatar>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-56" align="end" forceMount>
        <div className="flex flex-col space-y-1 p-2">
          <p className="text-sm font-medium leading-none">{userName}</p>
          <p className="text-xs leading-none text-muted-foreground">
            {userEmail}
          </p>
          <p className="text-xs text-muted-foreground">{userRole}</p>
        </div>
        <DropdownMenuSeparator />
        <DropdownMenuItem asChild>
          <Link to="/admin/profile" className="cursor-pointer">
            <UserCircle className="mr-2 h-4 w-4" />
            <span>Profile</span>
          </Link>
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem className="cursor-pointer" onClick={handleLogout}>
          <LogOut className="mr-2 h-4 w-4" />
          <span>Log out</span>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

interface MobileSidebarProps {
  isOpen: boolean;
  onClose: () => void;
  navItems: NavItem[];
  currentPath: string;
}

export function MobileSidebar({
  isOpen,
  onClose,
  navItems,
  currentPath,
}: MobileSidebarProps) {
  return (
    <>
      {/* Mobile Sidebar Overlay */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-background/80 backdrop-blur-sm md:hidden"
          onClick={onClose}
        />
      )}

      {/* Mobile Sidebar */}
      <div
        className={`fixed top-0 bottom-0 left-0 z-50 w-3/4 max-w-xs bg-sidebar transform transition-transform duration-300 ease-in-out md:hidden ${
          isOpen ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        <div className="flex h-16 items-center justify-between px-4 border-b border-sidebar-border">
          <div className="font-bold text-lg">EV Spark</div>
          <Button variant="ghost" size="icon" onClick={onClose}>
            <X className="h-5 w-5" />
          </Button>
        </div>

        <div className="py-4 px-2 space-y-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = currentPath === item.href;

            return (
              <Link
                key={item.href}
                to={item.href}
                className={cn(
                  "flex items-center space-x-2 px-3 py-2 rounded-md text-sm font-medium transition-colors",
                  isActive
                    ? "bg-sidebar-accent text-sidebar-accent-foreground"
                    : "text-sidebar-foreground hover:bg-sidebar-accent/50"
                )}
                onClick={onClose}
              >
                <Icon className="h-5 w-5" />
                <span>{item.label}</span>
              </Link>
            );
          })}
        </div>
      </div>
    </>
  );
}

interface HeaderProps {
  title: string;
  onMobileMenuClick: () => void;
  userName: string;
  userEmail: string;
  userRole: string;
  userImage?: string;
}

export function Header({
  title,
  onMobileMenuClick,
  userName,
  userEmail,
  userRole,
  userImage,
}: HeaderProps) {
  return (
    <header className="sticky top-0 z-30 flex h-16 items-center justify-between border-b bg-background px-4 md:px-6">
      <div className="flex items-center gap-2">
        <Button
          variant="ghost"
          size="icon"
          className="md:hidden"
          onClick={onMobileMenuClick}
        >
          <Menu className="h-5 w-5" />
        </Button>
        <h1 className="text-lg font-semibold">{title}</h1>
      </div>
      <div className="flex items-center gap-3">
        <ThemeToggle />
        <UserNav
          userName={userName}
          userEmail={userEmail}
          userRole={userRole}
          userImage={userImage}
        />
      </div>
    </header>
  );
}

interface SidebarProps {
  navItems: NavItem[];
  currentPath: string;
}

export function Sidebar({ navItems, currentPath }: SidebarProps) {
  const [expandedSection, setExpandedSection] = useState<string | null>(null);

  const toggleExpand = (section: string) => {
    if (expandedSection === section) {
      setExpandedSection(null);
    } else {
      setExpandedSection(section);
    }
  };

  return (
    <aside className="hidden border-r bg-sidebar md:block w-64 shrink-0">
      <div className="flex h-16 items-center border-b border-sidebar-border px-6">
        <Link to="/" className="flex items-center gap-2 font-semibold">
          <span className="text-lg font-bold">EV Spark</span>
        </Link>
      </div>
      <div className="py-4 px-3 space-y-1">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = currentPath === item.href;

          if (item.label.includes("Section")) {
            return (
              <div key={item.href} className="space-y-1">
                <button
                  onClick={() => toggleExpand(item.label)}
                  className="flex w-full items-center justify-between px-3 py-2 text-sm font-medium text-sidebar-foreground"
                >
                  <div className="flex items-center gap-2">
                    <Icon className="h-5 w-5" />
                    <span>{item.label}</span>
                  </div>
                  <ChevronDown
                    className={`h-4 w-4 transition-transform ${
                      expandedSection === item.label ? "rotate-180" : ""
                    }`}
                  />
                </button>
                {expandedSection === item.label && (
                  <div className="ml-4 border-l border-sidebar-border pl-2 space-y-1">
                    {/* This is where nested items would go */}
                    <div className="flex items-center space-x-2 px-3 py-2 rounded-md text-sm">
                      Nested Item Example
                    </div>
                  </div>
                )}
              </div>
            );
          }

          return (
            <Link
              key={item.href}
              to={item.href}
              className={cn(
                "flex items-center space-x-2 px-3 py-2 rounded-md text-sm font-medium transition-colors",
                isActive
                  ? "bg-sidebar-accent text-sidebar-accent-foreground"
                  : "text-sidebar-foreground hover:bg-sidebar-accent/50"
              )}
            >
              <Icon className="h-5 w-5" />
              <span>{item.label}</span>
            </Link>
          );
        })}
      </div>
    </aside>
  );
}

interface MobileNavProps {
  navItems: NavItem[];
  currentPath: string;
}

export function MobileNav({ navItems, currentPath }: MobileNavProps) {
  return (
    <nav className="md:hidden fixed bottom-0 left-0 right-0 z-30 bg-sidebar border-t border-sidebar-border">
      <div className="grid grid-cols-4">
        {navItems.slice(0, 4).map((item) => {
          const Icon = item.icon;
          const isActive = currentPath === item.href;

          return (
            <Link
              key={item.href}
              to={item.href}
              className={cn(
                "flex flex-col items-center justify-center h-16 w-full text-xs font-medium",
                isActive ? "text-primary" : "text-muted-foreground"
              )}
            >
              <Icon className="h-5 w-5 mb-1" />
              <span>{item.label}</span>
            </Link>
          );
        })}
      </div>
    </nav>
  );
}

interface LayoutProps {
  children: React.ReactNode;
  navItems: NavItem[];
  title: string;
  userName: string;
  userEmail: string;
  userRole: string;
  userImage?: string;
}

export function DashboardLayout({
  children,
  navItems,
  title,
  userName,
  userEmail,
  userRole,
  userImage,
}: LayoutProps) {
  const [isMobileSidebarOpen, setIsMobileSidebarOpen] = useState(false);
  const location = useLocation();

  return (
    <div className="flex min-h-screen flex-col">
      <Header
        title={title}
        onMobileMenuClick={() => setIsMobileSidebarOpen(true)}
        userName={userName}
        userEmail={userEmail}
        userRole={userRole}
        userImage={userImage}
      />

      <div className="flex flex-1">
        <Sidebar navItems={navItems} currentPath={location.pathname} />

        <MobileSidebar
          isOpen={isMobileSidebarOpen}
          onClose={() => setIsMobileSidebarOpen(false)}
          navItems={navItems}
          currentPath={location.pathname}
        />

        <main className="flex-1 overflow-auto p-4 md:p-6 pb-20 md:pb-6">
          {children}
        </main>
      </div>

      <MobileNav navItems={navItems} currentPath={location.pathname} />
    </div>
  );
}
