import { NavLink, Outlet, Link } from "react-router-dom"
import { useLogout } from "@/features/auth/api/useLogout"
import { useAuthStore } from "@/shared/stores/authStore"

const NAV_ITEMS = [
  { to: "/admin/dashboard",      label: "Dashboard",      icon: "📊" },
  { to: "/admin/orders",         label: "Orders",         icon: "📦" },
  { to: "/admin/prescriptions",  label: "Prescriptions",  icon: "📋" },
  { to: "/admin/medicines",      label: "Medicines",      icon: "💊" },
  { to: "/admin/reports",        label: "Reports",        icon: "📈" },
]

export default function AdminLayout() {
  const user   = useAuthStore((s) => s.user)
  const logout = useLogout()

  return (
    <div className="flex min-h-screen bg-slate-50">
      {/* Sidebar */}
      <aside className="w-56 shrink-0 bg-white border-r flex flex-col shadow-sm">
        <div className="px-4 py-4 border-b">
          <Link to="/" className="font-bold text-green-700 text-base">💊 PharmaCare</Link>
          <p className="text-xs text-slate-400 mt-0.5 font-medium uppercase tracking-wider">Admin</p>
        </div>

        <nav className="flex-1 px-3 py-4 space-y-0.5">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? "bg-green-50 text-green-700"
                    : "text-slate-600 hover:bg-slate-50 hover:text-slate-900"
                }`
              }
            >
              <span>{item.icon}</span>
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

      {/* Main content */}
      <main className="flex-1 min-w-0 overflow-auto">
        <Outlet />
      </main>
    </div>
  )
}
