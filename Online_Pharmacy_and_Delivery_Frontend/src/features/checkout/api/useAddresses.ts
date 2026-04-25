import { useQuery } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { useAuthStore } from "@/shared/stores/authStore"
import { checkoutKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/order"

type UserAddressResponse = components["schemas"]["UserAddressResponse"]

export function useAddresses() {
  const accessToken = useAuthStore((s) => s.accessToken)
  return useQuery({
    queryKey: checkoutKeys.addresses(),
    queryFn: async (): Promise<UserAddressResponse[]> => {
      const response = await api.get("/api/orders/addresses")
      const data = response.data
      return Array.isArray(data) ? data : (data?.data ?? [])
    },
    enabled: !!accessToken,
  })
}
