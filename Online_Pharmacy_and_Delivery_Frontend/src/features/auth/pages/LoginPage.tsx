import { useState } from "react"
import { Link, useNavigate, useLocation } from "react-router-dom"

import AuthLayout from "@/features/auth/components/AuthLayout"
import PasswordStep from "@/features/auth/components/PasswordStep"
import OtpStep from "@/features/auth/components/OtpStep"
import type { LoginStep } from "@/features/auth/types"

export default function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()

  // Remember where the user came from (set by RequireAuth when it bounced them here)
  const from = (location.state as { from?: string } | null)?.from ?? "/"

  const [step, setStep] = useState<LoginStep>({ phase: "password" })
  // We need to hold the password briefly so "resend OTP" in step 2 can re-submit it.
  // Stored in component state, never persisted. Dies the moment the user leaves the page.
  const [passwordForResend, setPasswordForResend] = useState("")

  return (
    <AuthLayout
      title={step.phase === "password" ? "Log in" : "Verify your identity"}
      subtitle={
        step.phase === "password"
          ? "Welcome back. Enter your credentials to continue."
          : undefined
      }
      footer={
        step.phase === "password" ? (
          <>
            Don't have an account?{" "}
            <Link to="/signup" className="font-medium text-blue-600 hover:underline">
              Sign up
            </Link>
          </>
        ) : undefined
      }
    >
      {step.phase === "password" && (
        <PasswordStep
          onSuccess={(result, password) => { // <-- 1. Add password parameter here
            const form = document.activeElement as HTMLInputElement | null
            form?.blur() 
            
            setPasswordForResend(password)   // <-- 2. Use it directly here!
            
            setStep({
              phase: "otp",
              email: result.identifier,
              maskedEmail: result.maskedEmail,
            })
          }}
        />
      )}
      
      {step.phase === "otp" && (
        <OtpStep
          identifier={step.email}
          maskedEmail={step.maskedEmail}
          passwordForResend={passwordForResend}
          onBack={() => {
            setPasswordForResend("")
            setStep({ phase: "password" })
          }}
          onSuccess={() => {
            // useVerifyOtp.onSuccess already wrote to the auth store.
            setPasswordForResend("")
            navigate(from, { replace: true })
          }}
        />
      )}
    </AuthLayout>
  )
}