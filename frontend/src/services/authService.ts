import { apiRequest } from "./apiClient";
import type { AuthenticatedUser, AuthResponse, LoginRequest } from "../types/auth";

export const authService = {
  login(request: LoginRequest): Promise<AuthResponse> {
    return apiRequest<AuthResponse>("/auth/login", {
      method: "POST",
      body: request,
      skipAuth: true,
    });
  },

  me(): Promise<AuthenticatedUser> {
    return apiRequest<AuthenticatedUser>("/auth/me");
  },
};
