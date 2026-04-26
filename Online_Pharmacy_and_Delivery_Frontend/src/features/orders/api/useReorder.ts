import { useMutation, useQueryClient } from "@tanstack/react-query"
import { useNavigate } from "react-router-dom"
import { api } from "@/shared/api/client"
import { cartKeys } from "@/features/cart/api/queryKeys"

export function useReorder() {
  const queryClient = useQueryClient()
  const navigate = useNavigate()

  return useMutation({
    mutationFn: async (orderId: number): Promise<void> => {
      await api.post(`/api/orders/${orderId}/reorder`)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: cartKeys.detail() })
      navigate("/cart")
    },
  })
}
