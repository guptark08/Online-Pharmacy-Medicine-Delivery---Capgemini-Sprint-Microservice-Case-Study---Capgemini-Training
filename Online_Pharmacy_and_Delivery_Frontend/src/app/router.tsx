import {
  createBrowserRouter,
  createRoutesFromElements,
  Route,
} from "react-router-dom"

import RootLayout from "@/shared/components/layouts/RootLayout"
import NotFoundPage from "@/shared/components/NotFoundPage"
import RequireAuth from "@/shared/components/RequireAuth"

import HomePage from "@/features/home/pages/HomePage"
import SignupPage from "@/features/auth/pages/SignupPage"
import LoginPage from "@/features/auth/pages/LoginPage"
import VerifyEmailSentPage from "@/features/auth/pages/VerifyEmailSentPage"
import VerifyEmailPage from "@/features/auth/pages/VerifyEmailPage"
import ForgotPasswordPage from "@/features/auth/pages/ForgotPasswordPage"
import ResetPasswordPage from "@/features/auth/pages/ResetPasswordPage"

export const router = createBrowserRouter(
  createRoutesFromElements(
    <Route path="/" element={<RootLayout />} errorElement={<NotFoundPage />}>
      {/* Public auth routes */}
      <Route path="signup" element={<SignupPage />} />
      <Route path="login" element={<LoginPage />} />
      <Route path="auth/email-sent" element={<VerifyEmailSentPage />} />
      <Route path="verify-email" element={<VerifyEmailPage />} />
      <Route path="forgot-password" element={<ForgotPasswordPage />} />
      <Route path="reset-password" element={<ResetPasswordPage />} />

      {/* Protected routes — wrap anything that needs auth */}
      <Route
        index
        element={
          <RequireAuth>
            <HomePage />
          </RequireAuth>
        }
      />

      {/* Wildcard 404 */}
      <Route path="*" element={<NotFoundPage />} />
    </Route>
  )
)