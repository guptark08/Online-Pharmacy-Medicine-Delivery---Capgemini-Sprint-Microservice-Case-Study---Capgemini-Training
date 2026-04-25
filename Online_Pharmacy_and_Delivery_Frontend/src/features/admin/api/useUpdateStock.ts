import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { adminKeys } from "./queryKeys"

export function useUpdateStock() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async ({ id, stock }: { id: number; stock: number }): Promise<void> => {
      await api.patch(`/api/admin/medicines/${id}/stock`, null, { params: { stock } })
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminKeys.medicines.all() })
      queryClient.invalidateQueries({ queryKey: adminKeys.dashboard() })
    },
  })
}
