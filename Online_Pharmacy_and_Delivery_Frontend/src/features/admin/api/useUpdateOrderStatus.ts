import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { adminKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/admin"

type OrderStatusUpdateDto = components["schemas"]["OrderStatusUpdateDto"]
type OrderResponseDto = components["schemas"]["OrderResponseDto"]

export function useUpdateOrderStatus() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async ({
      id,
      ...body
    }: { id: number } & OrderStatusUpdateDto): Promise<OrderResponseDto> => {
      const response = await api.put(`/api/admin/orders/${id}/status`, body)
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminKeys.orders.all() })
    },
  })
}
