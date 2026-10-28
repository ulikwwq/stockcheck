import type { RoleName } from "../types/auth";

export interface NavItem {
  to: string;
  label: string;
  icon:
    "products" | "sellers" | "history" | "profit" | "business" | "analytics";
}

export function isOwner(roles: RoleName[]): boolean {
  return roles.includes("ADMINISTRATOR");
}

export function isSuperAdmin(roles: RoleName[]): boolean {
  return roles.includes("SUPER_ADMIN");
}

export function isSeller(roles: RoleName[]): boolean {
  return roles.includes("SELLER");
}

/**
 * The administrator gets five sections.
 * The seller gets only the product list.
 * The super administrator gets the tenant management section.
 */
export function getNavItems(roles: RoleName[]): NavItem[] {
  if (isSuperAdmin(roles)) {
    return [{ to: "/admin/tenants", label: "Бизнесы", icon: "business" }];
  }

  if (isOwner(roles)) {
    return [
      { to: "/products", label: "Товары", icon: "products" },
      { to: "/sellers", label: "Продавцы", icon: "sellers" },
      { to: "/history", label: "История", icon: "history" },
      { to: "/profit", label: "Прибыль", icon: "profit" },
      { to: "/analytics", label: "Аналитика", icon: "analytics" },
    ];
  }

  // SELLER
  return [{ to: "/products", label: "Товары", icon: "products" }];
}

export function getHomeRoute(roles: RoleName[]): string {
  if (isSuperAdmin(roles)) return "/admin/tenants";
  return "/products";
}
