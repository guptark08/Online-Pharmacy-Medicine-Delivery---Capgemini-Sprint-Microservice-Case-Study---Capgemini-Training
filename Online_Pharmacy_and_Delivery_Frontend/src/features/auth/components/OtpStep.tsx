import { useEffect, useState } from "react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"

import { loginOtpSchema, type LoginOtpInput } from "@/features/auth/schemas"
import { useVerifyOtp } from "@/features/auth/api/useVerifyOtp"
import { useRequestOtp } from "@/features/auth/api/useRequestOtp"
import { getErrorMessage } from "@/shared/lib/errors"

import { Button } from "@/shared/ui/button"
import { Input } from "@/shared/ui/input"
import { Label } from "@/shared/ui/label"
import type { components } from "@/shared/types/api/auth"

type AuthResponse = components["schemas"]["AuthResponse"]

interface OtpStepProps {
  /** The username/email typed in the password step — sent as `identifier` */
  identifier: string
  /** The masked email for display ("al***@pharmacy.local") */
  maskedEmail: string
  /** The password from step 1, kept in memory so resend works */
  passwordForResend: string
  onSuccess: (auth: AuthResponse) => void
  onBack: () => void
}

export default function OtpStep({
  identifier,
  maskedEmail,
  passwordForResend,
  onSuccess,
  onBack,
}: OtpStepProps) {
  const { mutate: verifyOtp, isPending, error } = useVerifyOtp()
  const {
    mutate: requestOtp,
    isPending: isResending,
    error: resendError,
  } = useRequestOtp()

  // Tracks whether the most recent resend succeeded (for the "Code sent" message)
  const [resendFeedback, setResendFeedback] = useState<string | null>(null)

  // Resend cooldown — prevent the user from spamming the button. 30s after mount.
  const [cooldown, setCooldown] = useState(30)
  useEffect(() => {
    if (cooldown <= 0) return
    const timer = setTimeout(() => setCooldown((c) => c - 1), 1000)
    return () => clearTimeout(timer)
  }, [cooldown])

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginOtpInput>({
    resolver: zodResolver(loginOtpSchema),
    mode: "onBlur",
    defaultValues: { identifier },
  })

  const onSubmit = (input: LoginOtpInput) => {
    verifyOtp(
      { identifier, otpCode: input.otpCode },
      { onSuccess }
    )
  }

  const handleResend = () => {
    if (cooldown > 0) return
    setResendFeedback(null)
    requestOtp(
      { username: identifier, password: passwordForResend },
      {
        onSuccess: () => {
          setResendFeedback(`A new code was sent to ${maskedEmail}.`)
          setCooldown(30)
        },
      }
    )
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
      <p className="text-sm text-slate-600">
        We sent a 6-digit code to <span className="font-medium">{maskedEmail}</span>.
      </p>

      <div className="space-y-1.5">
        <Label>Verification code</Label>
        <Input
          inputMode="numeric"
          autoComplete="one-time-code"
          maxLength={6}
          autoFocus
          {...register("otpCode")}
        />
        {errors.otpCode && (
          <p className="text-sm text-red-600">{errors.otpCode.message}</p>
        )}
      </div>

      {error && (
        <p className="text-sm text-red-600" role="alert">
          {getErrorMessage(error)}
        </p>
      )}

      <Button type="submit" disabled={isPending} className="w-full">
        {isPending ? "Verifying…" : "Verify and log in"}
      </Button>

      <div className="flex items-center justify-between pt-2">
        <button
          type="button"
          onClick={onBack}
          className="text-sm text-slate-600 hover:text-slate-900"
        >
          ← Back
        </button>

        <button
          type="button"
          onClick={handleResend}
          disabled={isResending || cooldown > 0}
          className="text-sm text-blue-600 hover:underline disabled:text-slate-400 disabled:cursor-not-allowed disabled:no-underline"
        >
          {cooldown > 0 ? `Resend in ${cooldown}s` : isResending ? "Sending…" : "Resend code"}
        </button>
      </div>

      {resendFeedback && (
        <p className="text-sm text-green-600" role="status">{resendFeedback}</p>
      )}
      {resendError && (
        <p className="text-sm text-red-600" role="alert">{getErrorMessage(resendError)}</p>
      )}
    </form>
  )
}