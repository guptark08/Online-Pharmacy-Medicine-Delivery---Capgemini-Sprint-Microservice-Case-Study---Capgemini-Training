import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { cartKeys } from "@/features/cart/api/queryKeys"
import type { components } from "@/shared/types/api/order"

type CheckoutRequest = components["schemas"]["CheckoutRequest"]
type OrderResponse = components["schemas"]["OrderResponse"]

export function useCheckout() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (body: CheckoutRequest): Promise<OrderResponse> => {
      const response = await api.post("/api/orders/checkout/start", body)
      // Order service returns OrderResponse directly (no wrapper)
      return response.data
    },
    onSuccess: () => {
      // Cart is consumed by the order — clear it immediately
      queryClient.setQueryData(cartKeys.detail(), {
        items: [],
        totalItems: 0,
        subtotal: 0,
        hasRxItems: false,
      })
    },
  })
}
