import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { Link, useSearchParams, useNavigate } from "react-router-dom"

import {
  resetPasswordSchema,
  type ResetPasswordInput,
} from "@/features/auth/schemas"
import { useResetPassword } from "@/features/auth/api/useResetPassword"
import { getErrorMessage } from "@/shared/lib/errors"

import AuthLayout from "@/features/auth/components/AuthLayout"
import { Button } from "@/shared/ui/button"
import { Input } from "@/shared/ui/input"
import { Label } from "@/shared/ui/label"

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get("token")

  const { mutate: resetPassword, isPending, error } = useResetPassword()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ResetPasswordInput>({
    resolver: zodResolver(resetPasswordSchema),
    mode: "onBlur",
    defaultValues: { token: token ?? "" },
  })

  // Missing/invalid token link — dead end, bail.
  if (!token) {
    return (
      <AuthLayout
        title="Invalid reset link"
        subtitle="This link is missing a token. Request a new one."
      >
        <Link to="/forgot-password">
          <Button variant="outline" className="w-full">
            Request new link
          </Button>
        </Link>
      </AuthLayout>
    )
  }

  const onSubmit = (input: ResetPasswordInput) => {
    // Strip confirmPassword — backend doesn't want it
    const { token: t, newPassword } = input
    
    resetPassword(
      { token: t, newPassword },
      {
        onSuccess: () => {
          // Immediately navigate, passing state so LoginPage can show a success message
          navigate("/login", { 
            replace: true, 
            state: { resetSuccess: true } 
          })
        },
      }
    )
  }

  return (
    <AuthLayout
      title="Set a new password"
      subtitle="Choose a strong password you haven't used before."
      footer={
        <Link to="/login" className="font-medium text-blue-600 hover:underline">
          Back to login
        </Link>
      }
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <input type="hidden" {...register("token")} />

        <Field label="New password" error={errors.newPassword?.message}>
          <Input
            type="password"
            autoComplete="new-password"
            autoFocus
            {...register("newPassword")}
          />
        </Field>

        <Field
          label="Confirm new password"
          error={errors.confirmPassword?.message}
        >
          <Input
            type="password"
            autoComplete="new-password"
            {...register("confirmPassword")}
          />
        </Field>

        {error && (
          <p className="text-sm text-red-600" role="alert">
            {getErrorMessage(error)}
          </p>
        )}

        <Button type="submit" disabled={isPending} className="w-full">
          {isPending ? "Updating…" : "Update password"}
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