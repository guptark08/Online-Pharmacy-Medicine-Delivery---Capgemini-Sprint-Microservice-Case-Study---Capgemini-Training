import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { Link } from "react-router-dom"

import { loginPasswordSchema, type LoginPasswordInput } from "@/features/auth/schemas"
import { useRequestOtp, type OtpRequested } from "@/features/auth/api/useRequestOtp"
import { getErrorMessage } from "@/shared/lib/errors"

import { Button } from "@/shared/ui/button"
import { Input } from "@/shared/ui/input"
import { Label } from "@/shared/ui/label"

interface PasswordStepProps {
  onSuccess: (result: OtpRequested, password: string) => void
}

export default function PasswordStep({ onSuccess }: PasswordStepProps) {
  const { mutate: requestOtp, isPending, error } = useRequestOtp()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginPasswordInput>({
    resolver: zodResolver(loginPasswordSchema),
    mode: "onBlur",
  })

 const onSubmit = (input: LoginPasswordInput) => {
    requestOtp(input, { 
      onSuccess: (result) => onSuccess(result, input.password) 
    })
  }
  
  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
      <Field label="Username or email" error={errors.username?.message}>
        <Input {...register("username")} autoComplete="username" autoFocus />
      </Field>

      <Field label="Password" error={errors.password?.message}>
        <Input type="password" {...register("password")} autoComplete="current-password" />
      </Field>

      {error && (
        <p className="text-sm text-red-600" role="alert">
          {getErrorMessage(error)}
        </p>
      )}

      <Button type="submit" disabled={isPending} className="w-full">
        {isPending ? "Sending code…" : "Continue"}
      </Button>

      <div className="text-right">
        <Link
          to="/forgot-password"
          className="text-sm text-blue-600 hover:underline"
        >
          Forgot your password?
        </Link>
      </div>
    </form>
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