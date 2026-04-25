import { useQuery } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { adminKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/admin"

type InventoryReportDto = components["schemas"]["InventoryReportDto"]

export function useInventoryReport() {
  return useQuery({
    queryKey: adminKeys.reports.inventory(),
    queryFn: async (): Promise<InventoryReportDto> => {
      const response = await api.get("/api/admin/reports/inventory")
      return response.data.data
    },
    staleTime: 5 * 60_000,
  })
}
