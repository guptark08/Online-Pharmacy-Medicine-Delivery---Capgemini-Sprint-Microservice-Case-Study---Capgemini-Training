import { useMutation } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import { useAuthStore } from "@/shared/stores/authStore"
import type { LoginOtpInput } from "@/features/auth/schemas"
import type { components } from "@/shared/types/api/auth"

type AuthResponse = components["schemas"]["AuthResponse"]

export function useVerifyOtp() {
  const setSession = useAuthStore((s) => s.setSession)

  return useMutation({
    mutationFn: async (input: LoginOtpInput): Promise<AuthResponse> => {
      const response = await api.post("/api/auth/verify-login-otp", input)
      return response.data.data
    },
    onSuccess: (data) => {
      setSession(data)
    },
  })
}