import { useQuery } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { checkoutKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/order"

type UserAddressResponse = components["schemas"]["UserAddressResponse"]

export function useAddresses() {
  return useQuery({
    queryKey: checkoutKeys.addresses(),
    queryFn: async (): Promise<UserAddressResponse[]> => {
      const response = await api.get("/api/orders/addresses")
      // Handles both wrapped ApiResponse and raw array
      const data = response.data
      return Array.isArray(data) ? data : (data?.data ?? [])
    },
  })
}
