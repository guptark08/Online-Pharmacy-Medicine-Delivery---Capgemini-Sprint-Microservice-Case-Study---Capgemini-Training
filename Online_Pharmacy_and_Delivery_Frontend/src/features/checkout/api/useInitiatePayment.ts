import { useMutation } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import type { components } from "@/shared/types/api/order"

type PaymentRequest = components["schemas"]["PaymentRequest"]
type PaymentResponse = components["schemas"]["PaymentResponse"]

export function useInitiatePayment() {
  return useMutation({
    mutationFn: async (body: PaymentRequest): Promise<PaymentResponse> => {
      const response = await api.post("/api/orders/payments/initiate", body)
      return response.data
    },
  })
}
