import { useQuery } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { useAuthStore } from "@/shared/stores/authStore"
import type { components } from "@/shared/types/api/catalog"

type PrescriptionResponseDTO = components["schemas"]["PrescriptionResponseDTO"]

export function usePrescriptionById(id: number | null) {
  const accessToken = useAuthStore((s) => s.accessToken)

  return useQuery({
    queryKey: ["prescriptions", "by-id", id],
    queryFn: async (): Promise<PrescriptionResponseDTO> => {
      const response = await api.get(`/api/catalog/prescriptions/${id}`)
      return response.data.data
    },
    enabled: !!accessToken && id != null,
    staleTime: 10_000,
    refetchInterval: (query) => {
      const status = query.state.data?.status
      return status === "PENDING" ? 15_000 : false
    },
  })
}
