import { createBrowserRouter } from "react-router-dom"
import RootLayout from "@/shared/components/layouts/RootLayout"
import RequireAuth from "@/shared/components/RequireAuth"
import NotFoundPage from "@/shared/components/NotFoundPage"

import { lazy, Suspense } from "react"

const CatalogPage        = lazy(() => import("@/features/catalog/pages/CatalogPage"))
const MedicineDetailPage = lazy(() => import("@/features/catalog/pages/MedicineDetailPage"))
const CartPage           = lazy(() => import("@/features/cart/pages/CartPage"))
const CheckoutPage       = lazy(() => import("@/features/checkout/pages/CheckoutPage"))
const OrderListPage      = lazy(() => import("@/features/orders/pages/OrderListPage"))
const OrderDetailPage    = lazy(() => import("@/features/orders/pages/OrderDetailPage"))

import HomePage from "@/features/home/pages/HomePage"
import SignupPage from "@/features/auth/pages/SignupPage"
import LoginPage from "@/features/auth/pages/LoginPage"
import VerifyEmailSentPage from "@/features/auth/pages/VerifyEmailSentPage"
import VerifyEmailPage from "@/features/auth/pages/VerifyEmailPage"
import ForgotPasswordPage from "@/features/auth/pages/ForgotPasswordPage"
import ResetPasswordPage from "@/features/auth/pages/ResetPasswordPage"

function PageLoader() {
  return (
    <div className="flex items-center justify-center h-40 text-slate-400 text-sm">
      Loading…
    </div>
  )
}

export const router = createBrowserRouter([
  {
    path: "/",
    element: <RootLayout />,
    errorElement: <NotFoundPage />,
    children: [
      {
        index: true,
        element: (
          <RequireAuth>
            <HomePage />
          </RequireAuth>
        ),
      },
      // Public auth routes
      { path: "signup", element: <SignupPage /> },
      { path: "login", element: <LoginPage /> },
      { path: "auth/email-sent", element: <VerifyEmailSentPage /> },
      { path: "verify-email", element: <VerifyEmailPage /> },
      { path: "forgot-password", element: <ForgotPasswordPage /> },
      { path: "reset-password", element: <ResetPasswordPage /> },

      // Public catalog
      { path: "catalog", element: <Suspense fallback={<PageLoader />}><CatalogPage /></Suspense> },
      { path: "catalog/:id", element: <Suspense fallback={<PageLoader />}><MedicineDetailPage /></Suspense> },

      // Auth-gated
      {
        path: "cart",
        element: (
          <RequireAuth>
            <Suspense fallback={<PageLoader />}>
              <CartPage />
            </Suspense>
          </RequireAuth>
        ),
      },
      {
        path: "checkout",
        element: (
          <RequireAuth>
            <Suspense fallback={<PageLoader />}>
              <CheckoutPage />
            </Suspense>
          </RequireAuth>
        ),
      },
      {
        path: "orders",
        element: (
          <RequireAuth>
            <Suspense fallback={<PageLoader />}>
              <OrderListPage />
            </Suspense>
          </RequireAuth>
        ),
      },
      {
        path: "orders/:id",
        element: (
          <RequireAuth>
            <Suspense fallback={<PageLoader />}>
              <OrderDetailPage />
            </Suspense>
          </RequireAuth>
        ),
      },

      // Wildcard 404
      { path: "*", element: <NotFoundPage /> },
    ],
  },
])
