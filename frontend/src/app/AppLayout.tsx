import type { ReactNode } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "./AuthContext";
import { getNavItems } from "./navigation";
import type { NavItem } from "./navigation";

function NavIcon({ icon }: { icon: NavItem["icon"] }) {
  const common = "h-6 w-6";
  switch (icon) {
    case "products":
      return (
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className={common}>
          <path d="M3 7.5 12 3l9 4.5-9 4.5-9-4.5Z" strokeLinejoin="round" />
          <path d="M3 7.5V16.5L12 21l9-4.5V7.5" strokeLinejoin="round" />
          <path d="M12 12v9" />
        </svg>
      );
    case "sellers":
      return (
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className={common}>
          <circle cx="9" cy="8" r="3.2" />
          <path d="M2.8 20c.7-3.4 3.3-5.5 6.2-5.5s5.5 2.1 6.2 5.5" strokeLinecap="round" />
          <path d="M16.5 5.2A3.2 3.2 0 1 1 16 11.5" strokeLinecap="round" />
          <path d="M17 14.7c2.5.5 4.4 2.4 5 5.3" strokeLinecap="round" />
        </svg>
      );
    case "history":
      return (
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className={common}>
          <path d="M3 12a9 9 0 1 0 3-6.7" strokeLinecap="round" />
          <path d="M3 4v4.5H7.5" strokeLinecap="round" strokeLinejoin="round" />
          <path d="M12 7.5V12l3 2" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      );
    case "profit":
      return (
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className={common}>
          <path d="M3 20h18" strokeLinecap="round" />
          <path d="M5 20V11M11 20V6M17 20v-7" strokeLinecap="round" />
          <path d="M14 4h5v5" strokeLinecap="round" strokeLinejoin="round" />
          <path d="M19 4 12 11l-3-3-5 5" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      );
    case "business":
      return (
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className={common}>
          <path d="M4 21V8l8-4 8 4v13" strokeLinejoin="round" />
          <path d="M9 21v-6h6v6" strokeLinejoin="round" />
        </svg>
      );
  }
}

export function AppLayout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  if (!user) return null;

  const navItems = getNavItems(user.roles);
  const displayName = [user.firstName, user.lastName].filter(Boolean).join(" ") || user.username;

  function handleLogout() {
    logout();
    navigate("/login", { replace: true });
  }

  return (
    <div className="flex min-h-screen flex-col bg-slate-50">
      <header className="sticky top-0 z-30 flex items-center justify-between border-b border-slate-200 bg-white px-4 py-3">
        <div>
          <p className="text-base font-bold text-slate-900">StockCheck</p>
          <p className="text-xs text-slate-500">{displayName}</p>
        </div>
        <button
          type="button"
          onClick={handleLogout}
          className="flex h-10 items-center gap-1.5 rounded-xl px-3 text-sm font-medium text-slate-600 active:bg-slate-100"
        >
          Выйти
        </button>
      </header>

      <main className="flex-1 overflow-y-auto px-4 pb-24 pt-4">{children}</main>

      {navItems.length > 1 && (
        <nav className="fixed inset-x-0 bottom-0 z-30 border-t border-slate-200 bg-white pb-[env(safe-area-inset-bottom)]">
          <div className="mx-auto flex max-w-lg">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `flex flex-1 flex-col items-center gap-0.5 py-2.5 text-xs font-medium transition-colors ${
                    isActive ? "text-slate-900" : "text-slate-400"
                  }`
                }
              >
                <NavIcon icon={item.icon} />
                {item.label}
              </NavLink>
            ))}
          </div>
        </nav>
      )}
    </div>
  );
}
