import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { Link, useNavigate } from "react-router-dom"

import { signupSchema, type SignupInput } from "@/features/auth/schemas"
import { useSignup } from "@/features/auth/api/useSignup"
import { getErrorMessage } from "@/shared/lib/errors"

import AuthLayout from "@/features/auth/components/AuthLayout"
import { Button } from "@/shared/ui/button"
import { Input } from "@/shared/ui/input"
import { Label } from "@/shared/ui/label"

export default function SignupPage() {
  const navigate = useNavigate()
  const { mutate: signup, isPending, error } = useSignup()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<SignupInput>({
    resolver: zodResolver(signupSchema),
    mode: "onBlur",
  })

  const onSubmit = (input: SignupInput) => {
    signup(input, {
      onSuccess: () => {
        navigate("/auth/email-sent", { state: { email: input.email } })
      },
    })
  }

  return (
    <AuthLayout
      title="Create your account"
      subtitle="Get started by filling in your details"
      footer={
        <>
          Already have an account?{" "}
          <Link to="/login" className="font-medium text-blue-600 hover:underline">
            Log in
          </Link>
        </>
      }
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <Field label="Full name" error={errors.name?.message}>
          <Input {...register("name")} autoComplete="name" />
        </Field>

        <Field label="Email" error={errors.email?.message}>
          <Input type="email" {...register("email")} autoComplete="email" />
        </Field>

        <Field label="Username" error={errors.username?.message}>
          <Input {...register("username")} autoComplete="username" />
        </Field>

        <Field label="Mobile" error={errors.mobile?.message}>
          <Input {...register("mobile")} autoComplete="tel" inputMode="numeric" />
        </Field>

        <Field label="Password" error={errors.password?.message}>
          <Input type="password" {...register("password")} autoComplete="new-password" />
        </Field>

        {error && (
          <p className="text-sm text-red-600" role="alert">
            {getErrorMessage(error)}
          </p>
        )}

        <Button type="submit" disabled={isPending} className="w-full">
          {isPending ? "Creating account…" : "Create account"}
        </Button>
      </form>
    </AuthLayout>
  )
}

// Tiny local component — shared label + input + error triplet.
// Lives here because it's only used on auth forms.
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