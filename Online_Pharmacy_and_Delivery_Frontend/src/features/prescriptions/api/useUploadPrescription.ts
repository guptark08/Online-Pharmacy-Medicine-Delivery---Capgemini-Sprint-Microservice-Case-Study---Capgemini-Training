import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { prescriptionKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/catalog"

type PrescriptionResponseDTO = components["schemas"]["PrescriptionResponseDTO"]

export function useUploadPrescription() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (file: File): Promise<PrescriptionResponseDTO> => {
      const formData = new FormData()
      formData.append("file", file)
      const response = await api.post("/api/catalog/prescriptions/upload", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      })
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: prescriptionKeys.myList() })
    },
  })
}
