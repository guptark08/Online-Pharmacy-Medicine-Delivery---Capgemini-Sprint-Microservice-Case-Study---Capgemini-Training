import { useEffect, useState } from "react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"

import { loginOtpSchema, type LoginOtpInput } from "@/features/auth/schemas"
import { useVerifyOtp } from "@/features/auth/api/useVerifyOtp"
import { useRequestOtp } from "@/features/auth/api/useRequestOtp"
import { getErrorMessage } from "@/shared/lib/errors"
import type { components } from "@/shared/types/api/auth"

type AuthResponse = components["schemas"]["AuthResponse"]

interface OtpStepProps {
  identifier: string
  maskedEmail: string
  passwordForResend: string
  onSuccess: (auth: AuthResponse) => void
  onBack: () => void
}

export default function OtpStep({ identifier, maskedEmail, passwordForResend, onSuccess, onBack }: OtpStepProps) {
  const { mutate: verifyOtp, isPending, error } = useVerifyOtp()
  const { mutate: requestOtp, isPending: isResending, error: resendError } = useRequestOtp()

  const [resendFeedback, setResendFeedback] = useState<string | null>(null)
  const [cooldown, setCooldown] = useState(30)

  useEffect(() => {
    if (cooldown <= 0) return
    const timer = setTimeout(() => setCooldown((c) => c - 1), 1000)
    return () => clearTimeout(timer)
  }, [cooldown])

  const { register, handleSubmit, formState: { errors } } = useForm<LoginOtpInput>({
    resolver: zodResolver(loginOtpSchema),
    mode: "onBlur",
    defaultValues: { identifier },
  })

  const onSubmit = (input: LoginOtpInput) => {
    verifyOtp({ identifier, otpCode: input.otpCode }, { onSuccess })
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
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-5" noValidate>

      {/* Info banner */}
      <div className="bg-blue-50 border border-blue-200 rounded-xl px-4 py-3 flex items-start gap-3">
        <div className="w-8 h-8 rounded-lg bg-blue-100 flex items-center justify-center shrink-0 mt-0.5">
          <svg className="w-4 h-4 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75" />
          </svg>
        </div>
        <div>
          <p className="text-sm font-bold text-blue-800">Check your email</p>
          <p className="text-xs text-blue-600 mt-0.5">
            We sent a 6-digit code to <span className="font-bold">{maskedEmail}</span>
          </p>
        </div>
      </div>

      {/* OTP input */}
      <div className="space-y-1.5">
        <label className="block text-xs font-bold text-slate-600 uppercase tracking-wide">
          Verification code
        </label>
        <input
          inputMode="numeric"
          autoComplete="one-time-code"
          maxLength={6}
          autoFocus
          placeholder="Enter 6-digit code"
          {...register("otpCode")}
          className="w-full px-4 py-3 border-2 border-slate-200 rounded-xl text-center text-xl font-extrabold text-slate-800 tracking-[0.4em] placeholder:text-slate-300 placeholder:text-base placeholder:tracking-normal focus:outline-none focus:border-green-500 bg-white transition-colors"
        />
        {errors.otpCode && (
          <p className="text-xs text-red-600 flex items-center gap-1">
            <svg className="w-3 h-3 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z" />
            </svg>
            {errors.otpCode.message}
          </p>
        )}
      </div>

      {error && (
        <div className="flex items-center gap-2 bg-red-50 border border-red-200 rounded-xl px-3 py-2.5 text-sm text-red-700">
          <svg className="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z" />
          </svg>
          {getErrorMessage(error)}
        </div>
      )}

      <button
        type="submit"
        disabled={isPending}
        className="w-full py-3 bg-green-600 hover:bg-green-700 disabled:opacity-60 disabled:cursor-not-allowed text-white font-bold rounded-xl text-sm transition-colors shadow-sm"
      >
        {isPending
          ? <span className="flex items-center justify-center gap-2">
              <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
              </svg>
              Verifying…
            </span>
          : "Verify & Log in"
        }
      </button>

      {/* Back + resend */}
      <div className="flex items-center justify-between pt-1">
        <button
          type="button"
          onClick={onBack}
          className="inline-flex items-center gap-1 text-sm font-semibold text-slate-500 hover:text-slate-800 transition-colors"
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" />
          </svg>
          Back
        </button>

        <button
          type="button"
          onClick={handleResend}
          disabled={isResending || cooldown > 0}
          className={`text-sm font-semibold transition-colors ${
            cooldown > 0 || isResending
              ? "text-slate-400 cursor-not-allowed"
              : "text-green-600 hover:text-green-700"
          }`}
        >
          {cooldown > 0 ? `Resend in ${cooldown}s` : isResending ? "Sending…" : "Resend code"}
        </button>
      </div>

      {resendFeedback && (
        <div className="flex items-center gap-2 bg-green-50 border border-green-200 rounded-xl px-3 py-2.5 text-sm text-green-700">
          <svg className="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          {resendFeedback}
        </div>
      )}
      {resendError && (
        <div className="flex items-center gap-2 bg-red-50 border border-red-200 rounded-xl px-3 py-2.5 text-sm text-red-700">
          <svg className="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z" />
          </svg>
          {getErrorMessage(resendError)}
        </div>
      )}
    </form>
  )
}
