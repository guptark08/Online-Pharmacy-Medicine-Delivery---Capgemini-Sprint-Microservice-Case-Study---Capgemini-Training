import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { adminKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/admin"

type OrderStatusUpdateDto = components["schemas"]["OrderStatusUpdateDto"]
type OrderResponseDto     = components["schemas"]["OrderResponseDto"]

export function useUpdateOrderStatus() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async ({
      id,
      status,
    }: { id: number; status: string }): Promise<OrderResponseDto> => {
      // Backend reads status as a query param, not a request body
      const response = await api.put(`/api/admin/orders/${id}/status`, null, { params: { status } })
      return response.data.data
    },

    onMutate: async ({ id, status }) => {
      // Cancel any in-flight queries so they don't overwrite our optimistic update
      await queryClient.cancelQueries({ queryKey: adminKeys.orders.all() })

      // Snapshot both list and detail caches
      const previousList   = queryClient.getQueryData<OrderResponseDto[]>(adminKeys.orders.list())
      const previousDetail = queryClient.getQueryData<OrderResponseDto>(adminKeys.orders.detail(id))

      // Optimistically update the list cache immediately
      queryClient.setQueryData<OrderResponseDto[]>(adminKeys.orders.list(), (old) =>
        old?.map((o) => (o.id === id ? { ...o, status } : o)) ?? []
      )

      // Optimistically update the detail cache immediately
      queryClient.setQueryData<OrderResponseDto>(adminKeys.orders.detail(id), (old) =>
        old ? { ...old, status } : old
      )

      return { previousList, previousDetail, id }
    },

    onError: (_err, vars, context) => {
      // Rollback on failure
      if (context?.previousList) {
        queryClient.setQueryData(adminKeys.orders.list(), context.previousList)
      }
      if (context?.previousDetail) {
        queryClient.setQueryData(adminKeys.orders.detail(vars.id), context.previousDetail)
      }
    },

    onSettled: () => {
      // Always confirm from server after mutation settles
      queryClient.invalidateQueries({ queryKey: adminKeys.orders.all() })
      queryClient.invalidateQueries({ queryKey: adminKeys.dashboard() })
    },
  })
}
