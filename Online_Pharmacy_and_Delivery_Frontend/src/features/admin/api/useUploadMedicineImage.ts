import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { adminKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/admin"

type MedicineResponseDto = components["schemas"]["MedicineResponseDto"]

export function useUploadMedicineImage() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async ({ id, file }: { id: number; file: File }): Promise<MedicineResponseDto> => {
      const formData = new FormData()
      formData.append("file", file)
      const response = await api.post(`/api/catalog/medicines/${id}/image`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      })
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminKeys.medicines.all() })
    },
  })
}
