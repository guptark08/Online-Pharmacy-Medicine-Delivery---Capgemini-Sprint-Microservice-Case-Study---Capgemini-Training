import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { adminKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/admin"

type OrderResponseDto = components["schemas"]["OrderResponseDto"]

export function useCancelAdminOrder() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async ({
      id,
      reason,
    }: {
      id: number
      reason?: string
    }): Promise<void> => {
      await api.put(`/api/admin/orders/${id}/cancel`, null, { params: { reason } })
    },

    onMutate: async ({ id }) => {
      await queryClient.cancelQueries({ queryKey: adminKeys.orders.all() })

      const previousList   = queryClient.getQueryData<OrderResponseDto[]>(adminKeys.orders.list())
      const previousDetail = queryClient.getQueryData<OrderResponseDto>(adminKeys.orders.detail(id))

      queryClient.setQueryData<OrderResponseDto[]>(adminKeys.orders.list(), (old) =>
        old?.map((o) => (o.id === id ? { ...o, status: "ADMIN_CANCELLED" } : o)) ?? []
      )
      queryClient.setQueryData<OrderResponseDto>(adminKeys.orders.detail(id), (old) =>
        old ? { ...old, status: "ADMIN_CANCELLED" } : old
      )

      return { previousList, previousDetail, id }
    },

    onError: (_err, vars, context) => {
      if (context?.previousList) {
        queryClient.setQueryData(adminKeys.orders.list(), context.previousList)
      }
      if (context?.previousDetail) {
        queryClient.setQueryData(adminKeys.orders.detail(vars.id), context.previousDetail)
      }
    },

    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: adminKeys.orders.all() })
      queryClient.invalidateQueries({ queryKey: adminKeys.dashboard() })
    },
  })
}
