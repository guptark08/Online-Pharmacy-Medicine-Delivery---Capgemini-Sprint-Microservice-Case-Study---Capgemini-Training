import { useMutation } from "@tanstack/react-query"
import { api } from "@/shared/api/client"

export function useResendVerification() {
  return useMutation({
    mutationFn: async (email: string): Promise<string> => {
      const response = await api.post("/api/auth/resend-verification", null, {
        params: { email },
      })
      return response.data.message
    },
  })
}