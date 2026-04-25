import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { Link } from "react-router-dom"
import { useState } from "react"

import { forgotPasswordSchema, type ForgotPasswordInput } from "@/features/auth/schemas"
import { useForgotPassword } from "@/features/auth/api/useForgotPassword"
import { getErrorMessage } from "@/shared/lib/errors"

import AuthLayout from "@/features/auth/components/AuthLayout"
import { Button } from "@/shared/ui/button"
import { Input } from "@/shared/ui/input"
import { Label } from "@/shared/ui/label"

export default function ForgotPasswordPage() {
  const { mutate: forgot, isPending, error } = useForgotPassword()
  const [successMessage, setSuccessMessage] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ForgotPasswordInput>({
    resolver: zodResolver(forgotPasswordSchema),
    mode: "onBlur",
  })

  const onSubmit = (input: ForgotPasswordInput) => {
    setSuccessMessage(null)
    forgot(input, {
      onSuccess: (message: string) => setSuccessMessage(message),
    })
  }

  return (
    <AuthLayout
      title="Forgot password"
      subtitle="Enter your email and we'll send you a link to reset your password."
      footer={
        <>
          Remembered it?{" "}
          <Link to="/login" className="font-medium text-blue-600 hover:underline">
            Log in
          </Link>
        </>
      }
    >
      {successMessage ? (
        <p className="text-sm text-green-600" role="status">{successMessage}</p>
      ) : (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
          <div className="space-y-1.5">
            <Label>Email</Label>
            <Input type="email" {...register("email")} autoComplete="email" autoFocus />
            {errors.email && (
              <p className="text-sm text-red-600">{errors.email.message}</p>
            )}
          </div>

          {error && (
            <p className="text-sm text-red-600" role="alert">
              {getErrorMessage(error)}
            </p>
          )}

          <Button type="submit" disabled={isPending} className="w-full">
            {isPending ? "Sending…" : "Send reset link"}
          </Button>
        </form>
      )}
    </AuthLayout>
  )
}