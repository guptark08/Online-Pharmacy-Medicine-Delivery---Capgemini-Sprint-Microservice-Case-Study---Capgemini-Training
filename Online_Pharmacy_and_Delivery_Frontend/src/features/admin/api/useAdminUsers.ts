import { useQuery } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import type { components } from "@/shared/types/api/auth"

type UserResponse = components["schemas"]["UserResponse"]

export function useAdminUsers() {
  return useQuery({
    queryKey: ["admin", "users"],
    queryFn: async (): Promise<UserResponse[]> => {
      const response = await api.get("/api/auth/all")
      return response.data.data ?? []
    },
    staleTime: 60_000,
  })
}
