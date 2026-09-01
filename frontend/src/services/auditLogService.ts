import { apiRequest } from "./apiClient";
import type { AuditLog } from "../types/auditLog";

export const auditLogService = {
  list(): Promise<AuditLog[]> {
    return apiRequest<AuditLog[]>("/audit-logs");
  },
};
