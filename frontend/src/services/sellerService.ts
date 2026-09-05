import { apiRequest } from "./apiClient";
import type { CreateSellerRequest, ManagedUser, UpdateSellerRequest } from "../types/user";

export const sellerService = {
  list(): Promise<ManagedUser[]> {
    return apiRequest<ManagedUser[]>("/sellers");
  },

  create(request: CreateSellerRequest): Promise<ManagedUser> {
    return apiRequest<ManagedUser>("/sellers", { method: "POST", body: request });
  },

  update(id: string, request: UpdateSellerRequest): Promise<ManagedUser> {
    return apiRequest<ManagedUser>(`/sellers/${id}`, { method: "PUT", body: request });
  },

  activate(id: string): Promise<ManagedUser> {
    return apiRequest<ManagedUser>(`/sellers/${id}/activate`, { method: "PATCH" });
  },

  deactivate(id: string): Promise<ManagedUser> {
    return apiRequest<ManagedUser>(`/sellers/${id}/deactivate`, { method: "PATCH" });
  },
};
