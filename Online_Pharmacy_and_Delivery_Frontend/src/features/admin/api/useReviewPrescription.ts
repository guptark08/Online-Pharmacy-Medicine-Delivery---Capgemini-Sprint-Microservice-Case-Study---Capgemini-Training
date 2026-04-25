import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { adminKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/admin"

type PrescriptionReviewDto = components["schemas"]["PrescriptionReviewDto"]
type PrescriptionResponseDto = components["schemas"]["PrescriptionResponseDto"]

export function useReviewPrescription() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async ({
      id,
      ...body
    }: { id: number } & PrescriptionReviewDto): Promise<PrescriptionResponseDto> => {
      const response = await api.put(`/api/admin/prescriptions/${id}/review`, body)
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminKeys.prescriptions.all() })
      queryClient.invalidateQueries({ queryKey: adminKeys.dashboard() })
    },
  })
}
