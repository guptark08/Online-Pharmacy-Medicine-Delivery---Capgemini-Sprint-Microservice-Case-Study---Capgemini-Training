import { useQuery } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { orderKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/order"

type OrderResponse = components["schemas"]["OrderResponse"]

export function useOrder(id: number) {
  return useQuery({
    queryKey: orderKeys.detail(id),
    queryFn: async (): Promise<OrderResponse> => {
      const response = await api.get(`/api/orders/${id}`)
      const data = response.data
      // Handle both raw and wrapped
      return data?.data ?? data
    },
    enabled: Number.isFinite(id) && id > 0,
  })
}
