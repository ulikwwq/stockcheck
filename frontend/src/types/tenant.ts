export type TenantStatus = "ACTIVE" | "INACTIVE" | "DELETED";

export interface Tenant {
  id: string;
  name: string;
  status: TenantStatus;
  ownerUserId: string | null;
  ownerUsername: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTenantRequest {
  tenantName: string;
  shopName?: string;
  ownerUsername: string;
  ownerPassword: string;
  ownerFirstName?: string;
  ownerLastName?: string;
}

export interface ResetPasswordRequest {
  newPassword: string;
}

export interface ChangeUsernameRequest {
  newUsername: string;
}
