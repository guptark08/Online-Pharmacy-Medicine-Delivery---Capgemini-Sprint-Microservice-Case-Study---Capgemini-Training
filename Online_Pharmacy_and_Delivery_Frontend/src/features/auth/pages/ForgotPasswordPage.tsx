import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { Link } from "react-router-dom"

import {
  forgotPasswordSchema,
  type ForgotPasswordInput,
} from "@/features/auth/schemas"
import { useForgotPassword } from "@/features/auth/api/useForgotPassword"
import { getErrorMessage } from "@/shared/lib/errors"

import AuthLayout from "@/features/auth/components/AuthLayout"
import { Button } from "@/shared/ui/button"
import { Input } from "@/shared/ui/input"
import { Label } from "@/shared/ui/label"

export default function ForgotPasswordPage() {
  const {
    mutate: requestReset,
    isPending,
    isSuccess,
    error,
    data: successMessage,
  } = useForgotPassword()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ForgotPasswordInput>({
    resolver: zodResolver(forgotPasswordSchema),
    mode: "onBlur",
  })

  const onSubmit = (input: ForgotPasswordInput) => requestReset(input)

  if (isSuccess) {
    return (
      <AuthLayout
        title="Check your email"
        subtitle={
          successMessage ??
          "If an account exists with this email, a password reset link has been sent."
        }
        footer={
          <Link to="/login" className="font-medium text-blue-600 hover:underline">
            Back to login
          </Link>
        }
      >
        <p className="text-xs text-slate-500 text-center">
          Didn't get the email? Check your spam folder or try again in a minute.
        </p>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout
      title="Forgot your password?"
      subtitle="Enter your email and we'll send you a reset link."
      footer={
        <>
          Remembered it?{" "}
          <Link to="/login" className="font-medium text-blue-600 hover:underline">
            Log in
          </Link>
        </>
      }
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <Field label="Email" error={errors.email?.message}>
          <Input
            type="email"
            autoComplete="email"
            autoFocus
            {...register("email")}
          />
        </Field>

        {error && (
          <p className="text-sm text-red-600" role="alert">
            {getErrorMessage(error)}
          </p>
        )}

        <Button type="submit" disabled={isPending} className="w-full">
          {isPending ? "Sending…" : "Send reset link"}
        </Button>
      </form>
    </AuthLayout>
  )
}

function Field({
  label,
  error,
  children,
}: {
  label: string
  error?: string
  children: React.ReactNode
}) {
  return (
    <div className="space-y-1.5">
      <Label>{label}</Label>
      {children}
      {error && <p className="text-sm text-red-600">{error}</p>}
    </div>
  )
}