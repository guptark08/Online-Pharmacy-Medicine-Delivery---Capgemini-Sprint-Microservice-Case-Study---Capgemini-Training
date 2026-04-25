import { useQuery } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { catalogKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/catalog"

type CategoryDTO = components["schemas"]["CategoryDTO"]

export function useCategories() {
  return useQuery({
    queryKey: catalogKeys.categories(),
    queryFn: async (): Promise<CategoryDTO[]> => {
      const response = await api.get("/api/catalog/categories")
      return response.data.data ?? []
    },
    staleTime: 5 * 60_000, // categories rarely change — avoid pointless refetches
  })
}
