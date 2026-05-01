import { useQuery } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { adminKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/admin"

type OrderResponseDto = components["schemas"]["OrderResponseDto"]

export function useAdminOrderDetail(id: number) {
  return useQuery({
    queryKey: adminKeys.orders.detail(id),
    queryFn: async (): Promise<OrderResponseDto> => {
      const response = await api.get(`/api/admin/orders/${id}`)
      return response.data.data
    },
    enabled: id > 0,
    staleTime: 0,
    refetchInterval: 15_000,
  })
}
