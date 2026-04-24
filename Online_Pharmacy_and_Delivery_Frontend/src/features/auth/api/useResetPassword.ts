import { useMutation } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import type { ResetPasswordPayload } from "@/features/auth/schemas"

type ApiResponse<T> = { success: boolean; message: string; data: T }

export function useResetPassword() {
  return useMutation({
    mutationFn: async (input: ResetPasswordPayload): Promise<string> => {
      const response = await api.post<ApiResponse<string>>(
        "/api/auth/reset-password",
        input
      )
      return response.data.data
    },
  })
}