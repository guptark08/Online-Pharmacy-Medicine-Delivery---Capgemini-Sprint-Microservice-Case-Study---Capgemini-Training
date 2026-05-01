import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { adminKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/admin"

type PrescriptionReviewDto  = components["schemas"]["PrescriptionReviewDto"]
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

    onMutate: async ({ id, decision }) => {
      await queryClient.cancelQueries({ queryKey: adminKeys.prescriptions.all() })

      const previousPending = queryClient.getQueryData<PrescriptionResponseDto[]>(adminKeys.prescriptions.pending())
      const previousAll     = queryClient.getQueryData<PrescriptionResponseDto[]>(adminKeys.prescriptions.list())

      const newStatus = decision === "APPROVED" ? "APPROVED" : "REJECTED"

      // Remove from pending list immediately
      queryClient.setQueryData<PrescriptionResponseDto[]>(
        adminKeys.prescriptions.pending(),
        (old) => old?.filter((p) => p.id !== id) ?? []
      )

      // Update status in all list
      queryClient.setQueryData<PrescriptionResponseDto[]>(
        adminKeys.prescriptions.list(),
        (old) => old?.map((p) => (p.id === id ? { ...p, status: newStatus } : p)) ?? []
      )

      return { previousPending, previousAll }
    },

    onError: (_err, _vars, context) => {
      if (context?.previousPending) {
        queryClient.setQueryData(adminKeys.prescriptions.pending(), context.previousPending)
      }
      if (context?.previousAll) {
        queryClient.setQueryData(adminKeys.prescriptions.list(), context.previousAll)
      }
    },

    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: adminKeys.prescriptions.all() })
      queryClient.invalidateQueries({ queryKey: adminKeys.dashboard() })
    },
  })
}
