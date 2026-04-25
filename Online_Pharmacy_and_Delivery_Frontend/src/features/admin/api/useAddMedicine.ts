import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { adminKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/admin"

type MedicineRequestDto = components["schemas"]["MedicineRequestDto"]
type MedicineResponseDto = components["schemas"]["MedicineResponseDto"]

export function useAddMedicine() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (body: MedicineRequestDto): Promise<MedicineResponseDto> => {
      const response = await api.post("/api/admin/medicines", body)
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminKeys.medicines.all() })
    },
  })
}
