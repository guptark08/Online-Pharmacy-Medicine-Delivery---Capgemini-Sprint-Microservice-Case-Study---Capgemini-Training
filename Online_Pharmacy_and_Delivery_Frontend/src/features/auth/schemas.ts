import { z } from "zod"

// ─── shared primitives ────────────────────────────────────────────────

// Matches backend Pattern: 1 uppercase, 1 lowercase, 1 digit, 1 special (@$!%*?&)
const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/

const passwordField = z
  .string()
  .min(8, "Password must be at least 8 characters")
  .max(100, "Password cannot exceed 100 characters")
  .regex(
    PASSWORD_REGEX,
    "Must contain uppercase, lowercase, number, and special character (@$!%*?&)"
  )

// Backend email regex — mirrored exactly
const emailField = z
  .string()
  .min(1, "Email is required")
  .email("Please provide a valid email")
  .max(180, "Email cannot exceed 180 characters")

// ─── signup ───────────────────────────────────────────────────────────

export const signupSchema = z.object({
  name: z
    .string()
    .min(1, "Name is required")
    .max(120, "Name cannot exceed 120 characters"),
  email: emailField,
  username: z
    .string()
    .min(4, "Username must be at least 4 characters")
    .max(60, "Username cannot exceed 60 characters"),
  mobile: z
    .string()
    .regex(/^[0-9]{10,15}$/, "Mobile must be 10–15 digits"),
  password: passwordField,
})

export type SignupInput = z.infer<typeof signupSchema>

// ─── login step A: password ───────────────────────────────────────────

export const loginPasswordSchema = z.object({
  username: z.string().min(1, "Username or email is required"),
  password: z.string().min(1, "Password is required"),
})

export type LoginPasswordInput = z.infer<typeof loginPasswordSchema>

// ─── login step B: OTP ────────────────────────────────────────────────

export const loginOtpSchema = z.object({
  identifier: z.string().min(1, "Email is required"),
  otpCode: z
    .string()
    .length(6, "OTP must be 6 digits")
    .regex(/^\d{6}$/, "OTP must be numeric"),
})

export type LoginOtpInput = z.infer<typeof loginOtpSchema>

// ─── forgot password ──────────────────────────────────────────────────

export const forgotPasswordSchema = z.object({
  email: emailField,
})

export type ForgotPasswordInput = z.infer<typeof forgotPasswordSchema>

// ─── reset password (with client-side confirm) ────────────────────────

export const resetPasswordSchema = z
  .object({
    token: z.string().min(1, "Reset token is required"),
    newPassword: passwordField,
    confirmPassword: z.string().min(1, "Please confirm your password"),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
  })

export type ResetPasswordInput = z.infer<typeof resetPasswordSchema>

// Server payload (no confirmPassword — strip it before sending)
export type ResetPasswordPayload = Pick<ResetPasswordInput, "token" | "newPassword">