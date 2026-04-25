import { useQuery } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { catalogKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/catalog"

type MedicineDTO = components["schemas"]["MedicineDTO"]

export function useMedicine(id: number) {
  return useQuery({
    queryKey: catalogKeys.medicine(id),
    queryFn: async (): Promise<MedicineDTO> => {
      const response = await api.get(`/api/catalog/medicines/${id}`)
      return response.data.data
    },
    enabled: Number.isFinite(id) && id > 0,
  })
}
