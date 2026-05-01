import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { prescriptionKeys } from "./queryKeys"

export function useCancelPrescription() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (prescriptionId: number): Promise<void> => {
      await api.put(`/api/catalog/prescriptions/${prescriptionId}/cancel`)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: prescriptionKeys.myList() })
    },
  })
}
