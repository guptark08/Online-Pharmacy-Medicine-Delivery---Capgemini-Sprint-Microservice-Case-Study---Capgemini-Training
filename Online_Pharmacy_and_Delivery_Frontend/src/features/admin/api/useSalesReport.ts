import { useQuery } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { adminKeys } from "./queryKeys"
import type { components } from "@/shared/types/api/admin"

type SalesReportDto = components["schemas"]["SalesReportDto"]

interface SalesReportParams {
  startDate: string
  endDate: string
}

export function useSalesReport(params: SalesReportParams, enabled = true) {
  return useQuery({
    queryKey: adminKeys.reports.sales(params),
    queryFn: async (): Promise<SalesReportDto> => {
      const response = await api.get("/api/admin/reports/sales", { params })
      return response.data.data
    },
    enabled: enabled && !!params.startDate && !!params.endDate,
    staleTime: 5 * 60_000,
  })
}
