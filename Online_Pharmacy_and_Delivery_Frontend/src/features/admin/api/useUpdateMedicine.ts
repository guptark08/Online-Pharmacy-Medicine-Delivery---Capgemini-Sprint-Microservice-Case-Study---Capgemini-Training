import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { adminKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/admin"

type MedicineRequestDto = components["schemas"]["MedicineRequestDto"]
type MedicineResponseDto = components["schemas"]["MedicineResponseDto"]

export function useUpdateMedicine() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async ({
      id,
      ...body
    }: { id: number } & MedicineRequestDto): Promise<MedicineResponseDto> => {
      const response = await api.put(`/api/admin/medicines/${id}`, body)
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminKeys.medicines.all() })
    },
  })
}
