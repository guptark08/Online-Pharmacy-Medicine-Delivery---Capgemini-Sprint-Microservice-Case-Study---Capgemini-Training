import { useMutation } from "@tanstack/react-query"
import { api } from "@/shared/api/client"
import type { LoginPasswordInput } from "@/features/auth/schemas"

// Shape we return to the component so it can transition to the OTP step
export interface OtpRequested {
  // the identifier the user typed — needed for step 2
  identifier: string
  // masked email for display, e.g., "al***@pharmacy.local"
  maskedEmail: string
}

export function useRequestOtp() {
  return useMutation({
    mutationFn: async (input: LoginPasswordInput): Promise<OtpRequested> => {
      const response = await api.post("/api/auth/verify-password-then-send-otp", input)
      // Backend sends "OTP sent to al***@pharmacy.local" as the data field.
      // Parse out the masked email. Ugly but the response shape is what it is.
      const data: string = response.data.data ?? ""
      const match = data.match(/OTP sent to (.+)$/)
      const maskedEmail = match?.[1] ?? data

      return {
        identifier: input.username,
        maskedEmail,
      }
    },
  })
}