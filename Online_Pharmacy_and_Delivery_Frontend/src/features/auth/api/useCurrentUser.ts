import { useMutation } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import type { components } from "@/shared/types/api/auth"
import type { AuthUser, Role } from "@/features/auth/types"

type UserResponse = components["schemas"]["UserResponse"]
type ApiResponse<T> = { success: boolean; message: string; data: T }

function normalizeRole(role: string | undefined): Role {
  if (!role) return "CUSTOMER"
  const cleaned = role.trim().toUpperCase().replace(/^ROLE_/, "")
  return cleaned === "ADMIN" ? "ADMIN" : "CUSTOMER"
}

// Used by boot reconciliation. Mutation (not query) because we trigger it
// imperatively on app boot, not bind it to component lifecycle.
export function useFetchCurrentUser() {
  return useMutation({
    mutationFn: async (): Promise<AuthUser> => {
      const response = await api.get<ApiResponse<UserResponse>>("/api/auth/me")
      const data = response.data.data
      return {
        userId: data.id ?? 0,
        username: data.username ?? "",
        email: data.email ?? "",
        role: normalizeRole(data.role),
      }
    },
  })
}