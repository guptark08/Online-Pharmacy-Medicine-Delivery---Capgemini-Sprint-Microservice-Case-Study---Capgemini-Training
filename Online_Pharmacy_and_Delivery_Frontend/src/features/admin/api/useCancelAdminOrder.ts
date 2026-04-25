import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { adminKeys } from "./queryKeys"

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
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminKeys.orders.all() })
      queryClient.invalidateQueries({ queryKey: adminKeys.dashboard() })
    },
  })
}
