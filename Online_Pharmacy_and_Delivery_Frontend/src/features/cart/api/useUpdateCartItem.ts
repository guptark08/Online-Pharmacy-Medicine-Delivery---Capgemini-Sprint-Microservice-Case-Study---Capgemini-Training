import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { cartKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/order"

type CartResponse = components["schemas"]["CartResponse"]
type CartItemResponse = components["schemas"]["CartItemResponse"]

interface UpdateCartItemVars {
  itemId: number
  quantity: number
}

// Optimistic update — we have the item id, so we can update the cache immediately.
// Pattern: cancel → snapshot → mutate cache → on error, rollback → on settled, refetch.
export function useUpdateCartItem() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async ({ itemId, quantity }: UpdateCartItemVars): Promise<void> => {
      await api.put(`/api/orders/cart/${itemId}`, null, { params: { quantity } })
    },

    onMutate: async ({ itemId, quantity }) => {
      // Cancel any in-flight refetches to avoid overwriting our optimistic update
      await queryClient.cancelQueries({ queryKey: cartKeys.detail() })

      const previous = queryClient.getQueryData<CartResponse>(cartKeys.detail())

      queryClient.setQueryData<CartResponse>(cartKeys.detail(), (old) => {
        if (!old) return old
        const items = (old.items ?? []).map((item: CartItemResponse) =>
          item.id === itemId
            ? { ...item, quantity, lineTotal: (item.unitPrice ?? 0) * quantity }
            : item
        )
        return {
          ...old,
          items,
          subtotal: items.reduce((s: number, i: CartItemResponse) => s + (i.lineTotal ?? 0), 0),
          totalItems: items.reduce((s: number, i: CartItemResponse) => s + (i.quantity ?? 0), 0),
        }
      })

      return { previous }
    },

    onError: (_err, _vars, context) => {
      // Server rejected the mutation — restore the snapshot
      if (context?.previous) {
        queryClient.setQueryData(cartKeys.detail(), context.previous)
      }
    },

    onSettled: () => {
      // Always sync with the server after mutation settles (success or error)
      queryClient.invalidateQueries({ queryKey: cartKeys.detail() })
    },
  })
}
