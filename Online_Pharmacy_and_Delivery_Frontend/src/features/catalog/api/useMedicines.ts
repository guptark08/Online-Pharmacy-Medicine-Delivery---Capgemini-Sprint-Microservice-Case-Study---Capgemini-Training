import { useQuery } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { catalogKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/catalog"

type PageMedicineDTO = components["schemas"]["PageMedicineDTO"]
type MedicineDTO = components["schemas"]["MedicineDTO"]

export interface MedicinesParams {
  keyword?: string
  categoryId?: number
  requiresPrescription?: boolean
  page?: number   // 0-indexed — the API is 0-based; useCatalogParams handles the conversion
  size?: number
  sortBy?: string
}

export interface MedicinesResult {
  medicines: MedicineDTO[]
  totalPages: number
  totalElements: number
  currentPage: number  // 0-indexed, matches backend
}

export function useMedicines(params: MedicinesParams = {}) {
  return useQuery({
    queryKey: catalogKeys.medicinesList(params as Record<string, unknown>),
    queryFn: async (): Promise<PageMedicineDTO> => {
      // Catalog service wraps in ApiResponse<T> — real data lives at response.data.data
      const response = await api.get("/api/catalog/medicines", { params })
      return response.data.data
    },
    select: (data): MedicinesResult => ({
      medicines: data.content ?? [],
      totalPages: data.totalPages ?? 0,
      totalElements: data.totalElements ?? 0,
      currentPage: data.number ?? 0,
    }),
    // Keep previous page data visible while the next page loads — no layout flash
    placeholderData: (prev) => prev,
  })
}
