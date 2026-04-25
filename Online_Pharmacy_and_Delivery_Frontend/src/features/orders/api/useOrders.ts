import { useQuery } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { orderKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/order"

type OrderResponse = components["schemas"]["OrderResponse"]

export function useOrders() {
  return useQuery({
    queryKey: orderKeys.list(),
    queryFn: async (): Promise<OrderResponse[]> => {
      const response = await api.get("/api/orders")
      const data = response.data
      // Handle both raw array and paginated/wrapped responses
      if (Array.isArray(data)) return data
      if (Array.isArray(data?.data)) return data.data
      if (Array.isArray(data?.data?.content)) return data.data.content
      return []
    },
  })
}
