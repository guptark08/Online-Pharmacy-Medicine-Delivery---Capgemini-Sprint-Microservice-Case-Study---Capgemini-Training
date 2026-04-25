import { useQuery } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { adminKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/admin"

type DashboardResponseDto = components["schemas"]["DashboardResponseDto"]

export function useDashboard() {
  return useQuery({
    queryKey: adminKeys.dashboard(),
    queryFn: async (): Promise<DashboardResponseDto> => {
      const response = await api.get("/api/admin/dashboard")
      return response.data.data
    },
    staleTime: 60_000,
  })
}
