import { useQuery } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { useAuthStore } from "@/shared/stores/authStore"
import { cartKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/order"

type CartResponse = components["schemas"]["CartResponse"]

export function useCart() {
  const accessToken = useAuthStore((s) => s.accessToken)

  return useQuery({
    queryKey: cartKeys.detail(),
    queryFn: async (): Promise<CartResponse> => {
      // Order service returns CartResponse directly — no ApiResponse wrapper
      const response = await api.get("/api/orders/cart")
      return response.data
    },
    // Don't run if unauthenticated — prevents a pointless 401 on every page load
    enabled: !!accessToken,
    staleTime: 30_000,
  })
}
