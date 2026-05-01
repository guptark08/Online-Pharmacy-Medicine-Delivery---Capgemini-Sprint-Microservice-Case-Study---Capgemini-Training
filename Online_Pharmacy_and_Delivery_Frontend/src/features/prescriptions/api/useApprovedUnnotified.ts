import { useQuery } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { useAuthStore } from "@/shared/stores/authStore"
import { prescriptionKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/catalog"

type PrescriptionResponseDTO = components["schemas"]["PrescriptionResponseDTO"]

export function useApprovedUnnotified() {
  const accessToken = useAuthStore((s) => s.accessToken)
  const role = useAuthStore((s) => s.user?.role)

  return useQuery({
    queryKey: [...prescriptionKeys.myList(), "approved-unnotified"],
    queryFn: async (): Promise<PrescriptionResponseDTO[]> => {
      const response = await api.get("/api/catalog/prescriptions/my/approved-unnotified")
      return response.data.data
    },
    enabled: !!accessToken && role !== "ADMIN",
    staleTime: 0,
  })
}
