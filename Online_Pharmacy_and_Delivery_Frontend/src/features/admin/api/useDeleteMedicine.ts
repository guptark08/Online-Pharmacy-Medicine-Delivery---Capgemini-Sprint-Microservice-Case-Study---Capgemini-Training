import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { adminKeys } from "./queryKeys"

export function useDeleteMedicine() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (id: number): Promise<void> => {
      await api.delete(`/api/admin/medicines/${id}`)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminKeys.medicines.all() })
    },
  })
}
