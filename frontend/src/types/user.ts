import type { RoleName } from "./auth";

export interface ManagedUser {
  id: string;
  tenantId: string;
  username: string;
  firstName: string | null;
  lastName: string | null;
  active: boolean;
  roles: RoleName[];
}

export interface CreateSellerRequest {
  username: string;
  password: string;
  firstName?: string;
  lastName?: string;
}

export interface UpdateSellerRequest {
  firstName?: string;
  lastName?: string;
  newPassword?: string;
}
