import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { cartKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/order"

type AddToCartRequest = components["schemas"]["AddToCartRequest"]

// No optimistic update here — POST /cart returns void (Record<string, never>).
// We don't get the server-assigned cart item id back, so we can't merge
// a fake item into the cache cleanly. Invalidate and refetch instead.
// Optimistic updates are applied in useUpdateCartItem and useRemoveCartItem
// where we already have the item id.
export function useAddToCart() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (body: AddToCartRequest): Promise<void> => {
      await api.post("/api/orders/cart", body)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: cartKeys.detail() })
    },
  })
}
