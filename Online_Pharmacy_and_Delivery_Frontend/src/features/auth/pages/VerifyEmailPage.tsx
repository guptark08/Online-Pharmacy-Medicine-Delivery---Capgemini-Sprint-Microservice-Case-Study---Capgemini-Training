import { useEffect, useRef } from "react"
import { Link, useSearchParams } from "react-router-dom"

import AuthLayout from "@/features/auth/components/AuthLayout"
import { Button } from "@/shared/ui/button"
import { useVerifyEmail } from "@/features/auth/api/useVerifyEmail"
import { getErrorMessage } from "@/shared/lib/errors"

export default function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get("token")

  const { mutate: verify, isPending, isSuccess, isError, error, data } = useVerifyEmail()

  // Fire-once guard — prevents double-call in React StrictMode dev
  const hasFired = useRef(false)

  useEffect(() => {
    if (!token || hasFired.current) return
    hasFired.current = true
    verify(token)
  }, [token, verify])

  if (!token) {
    return (
      <AuthLayout title="Invalid link" subtitle="This verification link is missing a token.">
        <Link to="/login">
          <Button variant="outline" className="w-full">Back to login</Button>
        </Link>
      </AuthLayout>
    )
  }

  if (isPending) {
    return (
      <AuthLayout title="Verifying…" subtitle="Please wait while we confirm your email.">
        <div />
      </AuthLayout>
    )
  }

  if (isSuccess) {
    return (
      <AuthLayout
        title="Email verified"
        subtitle={data ?? "Your account is now active. You can log in."}
      >
        <Link to="/login">
          <Button className="w-full">Continue to login</Button>
        </Link>
      </AuthLayout>
    )
  }

  if (isError) {
    return (
      <AuthLayout
        title="Verification failed"
        subtitle={getErrorMessage(error)}
      >
        <Link to="/login">
          <Button variant="outline" className="w-full">Back to login</Button>
        </Link>
      </AuthLayout>
    )
  }

  return null
}