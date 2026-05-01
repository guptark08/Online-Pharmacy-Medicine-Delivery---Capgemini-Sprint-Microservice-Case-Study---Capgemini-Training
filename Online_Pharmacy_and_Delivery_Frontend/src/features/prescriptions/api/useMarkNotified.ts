import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { prescriptionKeys } from "./queryKeys"

export function useMarkNotified() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (prescriptionId: number) => {
      await api.put(`/api/catalog/prescriptions/${prescriptionId}/mark-notified`)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [...prescriptionKeys.myList(), "approved-unnotified"] })
    },
  })
}
