import { useQuery } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { adminKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/admin"

type PrescriptionResponseDto = components["schemas"]["PrescriptionResponseDto"]

export function useAdminPrescriptions() {
  return useQuery({
    queryKey: adminKeys.prescriptions.list(),
    queryFn: async (): Promise<PrescriptionResponseDto[]> => {
      const response = await api.get("/api/admin/prescriptions")
      return response.data.data ?? []
    },
    staleTime: 30_000,
  })
}

export function useAdminPendingPrescriptions() {
  return useQuery({
    queryKey: adminKeys.prescriptions.pending(),
    queryFn: async (): Promise<PrescriptionResponseDto[]> => {
      const response = await api.get("/api/admin/prescriptions/pending")
      return response.data.data ?? []
    },
    staleTime: 30_000,
  })
}
