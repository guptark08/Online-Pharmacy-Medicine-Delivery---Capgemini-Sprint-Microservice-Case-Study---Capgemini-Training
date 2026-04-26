import { useState } from "react"
import { NavLink, Outlet, Link } from "react-router-dom"
import { useLogout } from "@/features/auth/api/useLogout"
import { useAuthStore } from "@/shared/stores/authStore"

const NAV_ITEMS = [
  { to: "/admin/dashboard",      label: "Dashboard",      icon: "📊" },
  { to: "/admin/orders",         label: "Orders",         icon: "📦" },
  { to: "/admin/prescriptions",  label: "Prescriptions",  icon: "📋" },
  { to: "/admin/medicines",      label: "Medicines",      icon: "💊" },
  { to: "/admin/users",          label: "Users",          icon: "👤" },
  { to: "/admin/reports",        label: "Reports",        icon: "📈" },
]

function Sidebar({ onClose }: { onClose?: () => void }) {
  const user   = useAuthStore((s) => s.user)
  const logout = useLogout()

  return (
    <aside className="w-56 shrink-0 bg-white border-r flex flex-col shadow-sm h-full">
      <div className="px-4 py-4 border-b flex items-center justify-between">
        <div>
          <Link to="/" className="font-bold text-green-700 text-base">💊 PharmaCare</Link>
          <p className="text-xs text-slate-400 mt-0.5 font-medium uppercase tracking-wider">Admin</p>
        </div>
        {onClose && (
          <button onClick={onClose} aria-label="Close menu"
            className="text-slate-400 hover:text-slate-700 text-lg leading-none">✕</button>
        )}
      </div>

      <nav className="flex-1 px-3 py-4 space-y-0.5 overflow-y-auto">
        {NAV_ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            onClick={onClose}
            className={({ isActive }) =>
              `flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                isActive
                  ? "bg-green-50 text-green-700"
                  : "text-slate-600 hover:bg-slate-50 hover:text-slate-900"
              }`
            }
          >
            <span aria-hidden="true">{item.icon}</span>
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div className="px-4 py-4 border-t space-y-1">
        <p className="text-xs font-medium text-slate-700">{user?.username}</p>
        <p className="text-xs text-slate-400">Administrator</p>
        <button
          onClick={() => logout.mutate()}
          disabled={logout.isPending}
          className="mt-2 text-xs text-red-500 hover:text-red-700 disabled:opacity-50 transition-colors"
        >
          {logout.isPending ? "Logging out…" : "Log out"}
        </button>
      </div>
    </aside>
  )
}

export default function AdminLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false)

  return (
    <div className="flex min-h-screen bg-slate-50">
      {/* Desktop sidebar — always visible on md+ */}
      <div className="hidden md:flex">
        <Sidebar />
      </div>

      {/* Mobile sidebar overlay */}
      {sidebarOpen && (
        <div className="fixed inset-0 z-40 md:hidden">
          <div
            className="absolute inset-0 bg-black/40"
            onClick={() => setSidebarOpen(false)}
          />
          <div className="absolute left-0 top-0 h-full z-50 flex">
            <Sidebar onClose={() => setSidebarOpen(false)} />
          </div>
        </div>
      )}

      {/* Main content */}
      <main className="flex-1 min-w-0 overflow-auto">
        {/* Mobile top-bar with hamburger — hidden on md+ */}
        <div className="md:hidden sticky top-0 z-30 bg-white border-b px-4 h-12 flex items-center gap-3 shadow-sm">
          <button
            onClick={() => setSidebarOpen(true)}
            aria-label="Open navigation"
            className="text-slate-600 hover:text-slate-900 text-xl leading-none"
          >
            ☰
          </button>
          <span className="font-bold text-green-700 text-sm">💊 PharmaCare Admin</span>
        </div>

        <Outlet />
      </main>
    </div>
  )
}
