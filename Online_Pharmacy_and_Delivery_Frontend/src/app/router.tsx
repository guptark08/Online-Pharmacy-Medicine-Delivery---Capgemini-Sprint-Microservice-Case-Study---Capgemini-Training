import { createBrowserRouter, Navigate } from "react-router-dom"
import RootLayout from "@/shared/components/layouts/RootLayout"
import RequireAuth from "@/shared/components/RequireAuth"
import NotFoundPage from "@/shared/components/NotFoundPage"
import { ErrorBoundary } from "@/shared/components/ErrorBoundary"

import { lazy, Suspense } from "react"

// ── Customer pages (lazy) ──────────────────────────────────────────────────
const CatalogPage        = lazy(() => import("@/features/catalog/pages/CatalogPage"))
const MedicineDetailPage = lazy(() => import("@/features/catalog/pages/MedicineDetailPage"))
const CartPage           = lazy(() => import("@/features/cart/pages/CartPage"))
const CheckoutPage       = lazy(() => import("@/features/checkout/pages/CheckoutPage"))
const OrderListPage      = lazy(() => import("@/features/orders/pages/OrderListPage"))
const OrderDetailPage    = lazy(() => import("@/features/orders/pages/OrderDetailPage"))
const PrescriptionsPage  = lazy(() => import("@/features/prescriptions/pages/PrescriptionsPage"))

// ── Admin pages (lazy) ────────────────────────────────────────────────────
const AdminLayout             = lazy(() => import("@/features/admin/components/AdminLayout"))
const AdminDashboardPage      = lazy(() => import("@/features/admin/pages/AdminDashboardPage"))
const AdminOrdersPage         = lazy(() => import("@/features/admin/pages/AdminOrdersPage"))
const AdminOrderDetailPage    = lazy(() => import("@/features/admin/pages/AdminOrderDetailPage"))
const AdminPrescriptionsPage  = lazy(() => import("@/features/admin/pages/AdminPrescriptionsPage"))
const AdminMedicinesPage      = lazy(() => import("@/features/admin/pages/AdminMedicinesPage"))
const AdminReportsPage        = lazy(() => import("@/features/admin/pages/AdminReportsPage"))
const AdminUsersPage          = lazy(() => import("@/features/admin/pages/AdminUsersPage"))

// ── Auth pages (eager — on the critical login path) ───────────────────────
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

function Lazy({ children }: { children: React.ReactNode }) {
  return (
    <ErrorBoundary>
      <Suspense fallback={<PageLoader />}>{children}</Suspense>
    </ErrorBoundary>
  )
}

export const router = createBrowserRouter([
  {
    path: "/",
    element: <RootLayout />,
    errorElement: <NotFoundPage />,
    children: [
      // Home — role-based redirect (admin → /admin/dashboard, customer → landing)
      {
        index: true,
        element: (
          <RequireAuth>
            <HomePage />
          </RequireAuth>
        ),
      },

      // ── Public auth routes ─────────────────────────────────────────────
      { path: "signup",          element: <SignupPage /> },
      { path: "login",           element: <LoginPage /> },
      { path: "auth/email-sent", element: <VerifyEmailSentPage /> },
      { path: "verify-email",    element: <VerifyEmailPage /> },
      { path: "forgot-password", element: <ForgotPasswordPage /> },
      { path: "reset-password",  element: <ResetPasswordPage /> },

      // ── Public catalog ────────────────────────────────────────────────
      { path: "catalog",     element: <Lazy><CatalogPage /></Lazy> },
      { path: "catalog/:id", element: <Lazy><MedicineDetailPage /></Lazy> },

      // ── Auth-gated customer routes ─────────────────────────────────────
      {
        path: "cart",
        element: <RequireAuth><Lazy><CartPage /></Lazy></RequireAuth>,
      },
      {
        path: "checkout",
        element: <RequireAuth><Lazy><CheckoutPage /></Lazy></RequireAuth>,
      },
      {
        path: "orders",
        element: <RequireAuth><Lazy><OrderListPage /></Lazy></RequireAuth>,
      },
      {
        path: "orders/:id",
        element: <RequireAuth><Lazy><OrderDetailPage /></Lazy></RequireAuth>,
      },
      {
        path: "prescriptions",
        element: <RequireAuth><Lazy><PrescriptionsPage /></Lazy></RequireAuth>,
      },

      // Wildcard 404
      { path: "*", element: <NotFoundPage /> },
    ],
  },

  // ── Admin section (own layout, ADMIN role required) ───────────────────
  {
    path: "/admin",
    element: (
      <RequireAuth role="ADMIN">
        <Lazy><AdminLayout /></Lazy>
      </RequireAuth>
    ),
    children: [
      // /admin → redirect to dashboard
      { index: true, element: <Navigate to="/admin/dashboard" replace /> },
      { path: "dashboard",     element: <Lazy><AdminDashboardPage /></Lazy> },
      { path: "orders",        element: <Lazy><AdminOrdersPage /></Lazy> },
      { path: "orders/:id",    element: <Lazy><AdminOrderDetailPage /></Lazy> },
      { path: "prescriptions", element: <Lazy><AdminPrescriptionsPage /></Lazy> },
      { path: "medicines",     element: <Lazy><AdminMedicinesPage /></Lazy> },
      { path: "reports",       element: <Lazy><AdminReportsPage /></Lazy> },
      { path: "users",         element: <Lazy><AdminUsersPage /></Lazy> },
    ],
  },
])
