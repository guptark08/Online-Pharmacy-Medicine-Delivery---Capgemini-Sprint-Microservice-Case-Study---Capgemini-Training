import { useQuery } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { adminKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/admin"

type MedicineResponseDto = components["schemas"]["MedicineResponseDto"]

export function useAdminMedicines() {
  return useQuery({
    queryKey: adminKeys.medicines.list(),
    queryFn: async (): Promise<MedicineResponseDto[]> => {
      const response = await api.get("/api/admin/medicines")
      return response.data.data ?? []
    },
    staleTime: 0,
    refetchInterval: 30_000,
  })
}
