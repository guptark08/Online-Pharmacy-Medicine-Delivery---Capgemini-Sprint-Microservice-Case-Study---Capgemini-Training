import { Link, useLocation } from "react-router-dom"
import { useState } from "react"

import AuthLayout from "@/features/auth/components/AuthLayout"
import { Button } from "@/shared/ui/button"
import { useResendVerification } from "@/features/auth/api/useResendVerification"
import { getErrorMessage } from "@/shared/lib/errors"

export default function VerifyEmailSentPage() {
  const location = useLocation()
  const email = (location.state as { email?: string } | null)?.email
  const [resendFeedback, setResendFeedback] = useState<string | null>(null)

  const { mutate: resend, isPending, error } = useResendVerification()

  const handleResend = () => {
    if (!email) return
    setResendFeedback(null)
    resend(email, {
      onSuccess: (message) => setResendFeedback(message),
    })
  }

  return (
    <AuthLayout
      title="Check your email"
      subtitle={
        email
          ? `We sent a verification link to ${email}. Click it to activate your account.`
          : "We sent a verification link to your email. Click it to activate your account."
      }
      footer={
        <>
          Already verified?{" "}
          <Link to="/login" className="font-medium text-blue-600 hover:underline">
            Log in
          </Link>
        </>
      }
    >
      <div className="space-y-3">
        {email && (
          <Button
            type="button"
            variant="outline"
            className="w-full"
            onClick={handleResend}
            disabled={isPending}
          >
            {isPending ? "Resending…" : "Resend verification email"}
          </Button>
        )}

        {resendFeedback && (
          <p className="text-sm text-green-600" role="status">{resendFeedback}</p>
        )}
        {error && (
          <p className="text-sm text-red-600" role="alert">{getErrorMessage(error)}</p>
        )}

        <p className="text-xs text-slate-500 text-center pt-2">
          Didn't get the email? Check your spam folder.
        </p>
      </div>
    </AuthLayout>
  )
}