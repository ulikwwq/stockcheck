import { apiRequest } from "./apiClient";
import type {
  ChangeUsernameRequest,
  CreateTenantRequest,
  ResetPasswordRequest,
  Tenant,
} from "../types/tenant";

export const tenantService = {
  list(): Promise<Tenant[]> {
    return apiRequest<Tenant[]>("/admin/tenants");
  },

  get(id: string): Promise<Tenant> {
    return apiRequest<Tenant>(`/admin/tenants/${id}`);
  },

  create(request: CreateTenantRequest): Promise<Tenant> {
    return apiRequest<Tenant>("/admin/tenants", { method: "POST", body: request });
  },

  activate(id: string): Promise<Tenant> {
    return apiRequest<Tenant>(`/admin/tenants/${id}/activate`, { method: "PATCH" });
  },

  deactivate(id: string): Promise<Tenant> {
    return apiRequest<Tenant>(`/admin/tenants/${id}/deactivate`, { method: "PATCH" });
  },

  resetOwnerPassword(id: string, request: ResetPasswordRequest): Promise<void> {
    return apiRequest<void>(`/admin/tenants/${id}/reset-owner-password`, {
      method: "POST",
      body: request,
    });
  },

  changeOwnerUsername(id: string, request: ChangeUsernameRequest): Promise<Tenant> {
    return apiRequest<Tenant>(`/admin/tenants/${id}/owner-username`, {
      method: "PATCH",
      body: request,
    });
  },

  remove(id: string): Promise<Tenant> {
    return apiRequest<Tenant>(`/admin/tenants/${id}`, { method: "DELETE" });
  },
};
