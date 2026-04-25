import { Link, useLocation } from "react-router-dom"
import { useAuthStore } from "@/shared/stores/authStore"
import { useLogout } from "@/features/auth/api/useLogout"
import { useCart } from "@/features/cart/api/useCart"

export default function Navbar() {
  const location  = useLocation()
  const user      = useAuthStore((s) => s.user)
  const logout    = useLogout()
  const { data: cart } = useCart()

  const cartCount = cart?.totalItems ?? 0

  const navLink = (to: string, label: string) => (
    <Link
      to={to}
      className={`text-sm font-medium transition-colors ${
        location.pathname.startsWith(to)
          ? "text-green-700"
          : "text-slate-600 hover:text-slate-900"
      }`}
    >
      {label}
    </Link>
  )

  return (
    <header className="sticky top-0 z-50 bg-white border-b shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 h-14 flex items-center justify-between gap-4">

        {/* Logo */}
        <Link to="/" className="font-bold text-green-700 text-lg tracking-tight shrink-0">
          💊 PharmaCare
        </Link>

        {/* Nav links */}
        <nav className="flex items-center gap-5">
          {navLink("/catalog", "Catalog")}
          {user && navLink("/orders", "Orders")}
        </nav>

        {/* Right side: cart + user */}
        <div className="flex items-center gap-3">
          {user && (
            <Link
              to="/cart"
              className="relative p-2 rounded-full hover:bg-slate-100 transition-colors"
              aria-label="Cart"
            >
              <span className="text-xl leading-none">🛒</span>
              {cartCount > 0 && (
                <span
                  className="absolute -top-0.5 -right-0.5 min-w-[18px] h-[18px] flex items-center justify-center
                    bg-green-600 text-white text-[10px] font-bold rounded-full px-1 leading-none"
                >
                  {cartCount > 99 ? "99+" : cartCount}
                </span>
              )}
            </Link>
          )}

          {user ? (
            <div className="flex items-center gap-2">
              <span className="text-sm text-slate-600 hidden sm:inline">{user.username}</span>
              <button
                onClick={() => logout.mutate()}
                disabled={logout.isPending}
                className="text-sm text-slate-500 hover:text-slate-900 transition-colors disabled:opacity-50"
              >
                {logout.isPending ? "…" : "Log out"}
              </button>
            </div>
          ) : (
            <Link
              to="/login"
              className="text-sm font-medium bg-green-600 text-white px-4 py-1.5 rounded-lg hover:bg-green-700 transition-colors"
            >
              Log in
            </Link>
          )}
        </div>
      </div>
    </header>
  )
}
