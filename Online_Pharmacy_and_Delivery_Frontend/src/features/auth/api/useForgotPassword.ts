import { useMutation } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import type { ForgotPasswordInput } from "@/features/auth/schemas"

type ApiResponse<T> = { success: boolean; message: string; data: T }

export function useForgotPassword() {
  return useMutation({
    mutationFn: async (input: ForgotPasswordInput): Promise<string> => {
      const response = await api.post<ApiResponse<string>>(
        "/api/auth/forgot-password",
        input
      )
      return response.data.data
    },
  })
}