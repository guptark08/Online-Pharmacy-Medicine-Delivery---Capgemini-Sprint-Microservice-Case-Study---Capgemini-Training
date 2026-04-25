import { useQuery } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { prescriptionKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/catalog"

type PrescriptionResponseDTO = components["schemas"]["PrescriptionResponseDTO"]

export function useMyPrescriptions() {
  return useQuery({
    queryKey: prescriptionKeys.myList(),
    queryFn: async (): Promise<PrescriptionResponseDTO[]> => {
      const response = await api.get("/api/catalog/prescriptions/my")
      return response.data.data ?? []
    },
  })
}
