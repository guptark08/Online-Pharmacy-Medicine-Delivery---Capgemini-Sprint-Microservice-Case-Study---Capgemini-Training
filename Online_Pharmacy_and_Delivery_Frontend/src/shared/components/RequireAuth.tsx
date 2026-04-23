import { Navigate, useLocation } from "react-router-dom"
import { useAuthStore } from "@/shared/stores/authStore"
import type { Role } from "@/features/auth/types"
import type { ReactNode } from "react"

interface RequireAuthProps {
  children: ReactNode
  /** If set, the user must have this role. Otherwise they're sent to `/`. */
  role?: Role
}

export default function RequireAuth({ children, role }: RequireAuthProps) {
  const location = useLocation()
  const accessToken = useAuthStore((s) => s.accessToken)
  const user = useAuthStore((s) => s.user)
  const status = useAuthStore((s) => s.status)

  // Mid-refresh: hold the UI steady, do NOT redirect to login.
  if (status === "refreshing") {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-slate-500">Loading…</p>
      </div>
    )
  }

  // No token → not logged in. Remember where they wanted to go so login can bounce back.
  if (!accessToken || !user) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />
  }

  // Token exists but user lacks the required role → refuse.
  if (role && user.role !== role) {
    return <Navigate to="/" replace />
  }

  return <>{children}</>
}