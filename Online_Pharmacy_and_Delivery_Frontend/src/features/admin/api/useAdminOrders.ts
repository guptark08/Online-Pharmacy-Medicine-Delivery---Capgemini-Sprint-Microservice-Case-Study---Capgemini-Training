import { useQuery } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { adminKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/admin"

type OrderResponseDto = components["schemas"]["OrderResponseDto"]

export function useAdminOrders() {
  return useQuery({
    queryKey: adminKeys.orders.list(),
    queryFn: async (): Promise<OrderResponseDto[]> => {
      const response = await api.get("/api/admin/orders")
      return response.data.data ?? []
    },
    staleTime: 0,
    refetchInterval: 20_000,
  })
}
