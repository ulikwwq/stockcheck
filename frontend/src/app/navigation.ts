import type { RoleName } from "../types/auth";

export interface NavItem {
  to: string;
  label: string;
  icon: "products" | "sellers" | "history" | "profit" | "business";
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
 * Deliberately short. Per spec the administrator gets exactly four
 * sections; the seller gets a single one (just the product list — selling
 * happens by tapping a product, not via a separate nav item). Technical
 * concepts (shops, stock movements, categories) are never surfaced here.
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
    ];
  }

  // SELLER
  return [{ to: "/products", label: "Товары", icon: "products" }];
}

export function getHomeRoute(roles: RoleName[]): string {
  if (isSuperAdmin(roles)) return "/admin/tenants";
  return "/products";
}
