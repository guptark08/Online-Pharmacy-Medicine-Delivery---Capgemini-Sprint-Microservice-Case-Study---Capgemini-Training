import { useMutation } from "@tanstack/react-query"
import { api } from "@/shared/api/client"

export function useVerifyEmail() {
  return useMutation({
    mutationFn: async (token: string): Promise<string> => {
      const response = await api.get("/api/auth/verify-email", {
        params: { token },
      })
      return response.data.message
    },
  })
}