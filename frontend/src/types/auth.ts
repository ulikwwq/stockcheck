// Mirrors backend com.stockcheck.backend.role.RoleName.
export type RoleName =
  | "BUYER"
  | "SELLER"
  | "ADMINISTRATOR"
  | "MANAGER"
  | "COURIER"
  | "WAREHOUSE_OPERATOR"
  | "ACCOUNTANT"
  | "CONTENT_MANAGER"
  | "SUPER_ADMIN";

export interface AuthenticatedUser {
  id: string;
  tenantId: string | null;
  username: string;
  firstName: string | null;
  lastName: string | null;
  active: boolean;
  roles: RoleName[];
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: AuthenticatedUser;
}
