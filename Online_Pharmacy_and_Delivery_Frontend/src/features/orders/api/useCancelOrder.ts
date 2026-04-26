import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { orderKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/order"

type OrderResponse = components["schemas"]["OrderResponse"]

export function useCancelOrder() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async ({ id, reason }: { id: number; reason?: string }): Promise<OrderResponse> => {
      const response = await api.put(`/api/orders/${id}/cancel`, null, {
        params: reason ? { reason } : undefined,
      })
      return response.data?.data ?? response.data
    },
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: orderKeys.detail(id) })
      queryClient.invalidateQueries({ queryKey: orderKeys.list() })
    },
  })
}
