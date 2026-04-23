import { useMutation } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import type { ResetPasswordPayload } from "@/features/auth/schemas"

export function useResetPassword() {
  return useMutation({
    mutationFn: async (input: ResetPasswordPayload): Promise<string> => {
      const response = await api.post("/api/auth/reset-password", input)
      return response.data.message
    },
  })
}