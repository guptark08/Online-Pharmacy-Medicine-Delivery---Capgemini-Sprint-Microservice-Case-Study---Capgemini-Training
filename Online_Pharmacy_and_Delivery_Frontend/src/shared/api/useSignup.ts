import { useMutation } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import type { SignupInput } from "@/features/auth/schemas"
import type { components } from "@/shared/types/api/auth"

type UserResponse = components["schemas"]["UserResponse"]
type ApiResponse<T> = { success: boolean; message: string; data: T }

export function useSignup() {
  return useMutation({
    mutationFn: async (input: SignupInput): Promise<UserResponse> => {
      const response = await api.post<ApiResponse<UserResponse>>("/api/auth/signup", input)
      return response.data.data
    },
  })
}