import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { cartKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/order"

type CartResponse = components["schemas"]["CartResponse"]
type CartItemResponse = components["schemas"]["CartItemResponse"]

export function useRemoveCartItem() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (itemId: number): Promise<void> => {
      await api.delete(`/api/orders/cart/${itemId}`)
    },

    onMutate: async (itemId) => {
      await queryClient.cancelQueries({ queryKey: cartKeys.detail() })
      const previous = queryClient.getQueryData<CartResponse>(cartKeys.detail())

      queryClient.setQueryData<CartResponse>(cartKeys.detail(), (old) => {
        if (!old) return old
        const items = (old.items ?? []).filter((i: CartItemResponse) => i.id !== itemId)
        return {
          ...old,
          items,
          subtotal: items.reduce((s: number, i: CartItemResponse) => s + (i.lineTotal ?? 0), 0),
          totalItems: items.reduce((s: number, i: CartItemResponse) => s + (i.quantity ?? 0), 0),
          hasRxItems: items.some((i: CartItemResponse) => i.requiresPrescription),
        }
      })

      return { previous }
    },

    onError: (_err, _vars, context) => {
      if (context?.previous) {
        queryClient.setQueryData(cartKeys.detail(), context.previous)
      }
    },

    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: cartKeys.detail() })
    },
  })
}
