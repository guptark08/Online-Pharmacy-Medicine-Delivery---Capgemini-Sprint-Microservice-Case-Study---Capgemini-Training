import { useQuery } from "@tanstack/react-query"
import { useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { adminKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/admin"

type CategoryResponseDto = components["schemas"]["CategoryResponseDto"]
type CategoryRequestDto = components["schemas"]["CategoryRequestDto"]

export function useAdminCategories() {
  return useQuery({
    queryKey: adminKeys.categories(),
    queryFn: async (): Promise<CategoryResponseDto[]> => {
      const response = await api.get("/api/admin/categories")
      return response.data.data ?? []
    },
    staleTime: 0,
  })
}

export function useAddCategory() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (body: CategoryRequestDto): Promise<CategoryResponseDto> => {
      const response = await api.post("/api/admin/categories", body)
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminKeys.categories() })
    },
  })
}

export function useUpdateCategory() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async ({ id, ...body }: { id: number } & CategoryRequestDto): Promise<CategoryResponseDto> => {
      const response = await api.put(`/api/admin/categories/${id}`, body)
      return response.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminKeys.categories() })
    },
  })
}

export function useDeleteCategory() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (id: number): Promise<void> => {
      await api.delete(`/api/admin/categories/${id}`)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminKeys.categories() })
    },
  })
}
