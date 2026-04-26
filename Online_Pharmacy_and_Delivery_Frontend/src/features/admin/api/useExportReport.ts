import { useMutation } from "@tanstack/react-query"
import { api } from "@/shared/api/client"

export function useExportReport() {
  return useMutation({
    mutationFn: async ({
      format,
      startDate,
      endDate,
    }: {
      format: "csv" | "pdf"
      startDate: string
      endDate: string
    }): Promise<void> => {
      const response = await api.get("/api/admin/reports/export", {
        params: { format, startDate, endDate },
        responseType: "blob",
      })
      const url = URL.createObjectURL(new Blob([response.data]))
      const a = document.createElement("a")
      a.href = url
      a.download = `sales-report-${startDate}_to_${endDate}.${format}`
      document.body.appendChild(a)
      a.click()
      a.remove()
      URL.revokeObjectURL(url)
    },
  })
}
