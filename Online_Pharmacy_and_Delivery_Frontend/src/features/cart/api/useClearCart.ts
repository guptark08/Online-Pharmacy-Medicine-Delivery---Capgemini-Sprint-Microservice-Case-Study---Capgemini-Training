import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { cartKeys } from "./queryKeys"

export function useClearCart() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (): Promise<void> => {
      await api.delete("/api/orders/cart")
    },
    onSuccess: () => {
      // Set to empty rather than invalidate to avoid a pointless refetch
      queryClient.setQueryData(cartKeys.detail(), {
        items: [],
        totalItems: 0,
        subtotal: 0,
        hasRxItems: false,
      })
    },
  })
}
