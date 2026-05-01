import { useState } from "react"
import { Link, useLocation, useNavigate } from "react-router-dom"
import { useAuthStore } from "@/shared/stores/authStore"
import { useLogout } from "@/features/auth/api/useLogout"
import { useCart } from "@/features/cart/api/useCart"

export default function Navbar() {
  const location  = useLocation()
  const navigate  = useNavigate()
  const user      = useAuthStore((s) => s.user)
  const logout    = useLogout()
  const { data: cart } = useCart()
  const cartCount = cart?.totalItems ?? 0

  const [query, setQuery] = useState("")

  function handleSearch(e: React.FormEvent) {
    e.preventDefault()
    const q = query.trim()
    if (q) navigate(`/catalog?keyword=${encodeURIComponent(q)}`)
    else navigate("/catalog")
  }

  if (user?.role === "ADMIN") {
    return (
      <header className="sticky top-0 z-50 bg-white border-b shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 h-14 flex items-center justify-between gap-4">
          <Link to="/admin/dashboard" className="font-bold text-green-700 text-lg tracking-tight">
            PharmaCare
          </Link>
          <div className="flex items-center gap-4">
            <Link to="/admin/dashboard" className="text-sm font-medium text-slate-600 hover:text-slate-900">
              Admin Panel
            </Link>
            <span className="text-sm text-slate-500 hidden sm:inline">{user.name || user.username}</span>
            <button
              onClick={() => logout.mutate()}
              disabled={logout.isPending}
              className="text-sm text-slate-500 hover:text-slate-900 transition-colors disabled:opacity-50"
            >
              {logout.isPending ? "…" : "Log out"}
            </button>
          </div>
        </div>
      </header>
    )
  }

  const isActive = (to: string) => location.pathname.startsWith(to)

  return (
    <header className="sticky top-0 z-50 bg-white shadow-md">
      {/* ── Top info bar ─────────────────────────────────────────── */}
      <div className="bg-green-700 text-white text-[11px] font-medium">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 h-7 flex items-center justify-between">
          <span className="hidden sm:flex items-center gap-1.5">
            <svg className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z" />
            </svg>
            100% Genuine Medicines Guaranteed
          </span>
          <span className="flex items-center gap-4">
            <span className="flex items-center gap-1">
              <svg className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              Fast Delivery
            </span>
            <span className="flex items-center gap-1">
              <svg className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 6.75c0 8.284 6.716 15 15 15h2.25a2.25 2.25 0 002.25-2.25v-1.372c0-.516-.351-.966-.852-1.091l-4.423-1.106c-.44-.11-.902.055-1.173.417l-.97 1.293c-.282.376-.769.542-1.21.38a12.035 12.035 0 01-7.143-7.143c-.162-.441.004-.928.38-1.21l1.293-.97c.363-.271.527-.734.417-1.173L6.963 3.102a1.125 1.125 0 00-1.091-.852H4.5A2.25 2.25 0 002.25 4.5v2.25z" />
              </svg>
              24/7 Support
            </span>
          </span>
        </div>
      </div>

      {/* ── Main header ──────────────────────────────────────────── */}
      <div className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 h-16 flex items-center gap-4">

          {/* Logo */}
          <Link to="/" className="flex items-center gap-2 shrink-0">
            <div className="w-8 h-8 bg-green-600 rounded-lg flex items-center justify-center">
              <svg className="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M9.75 3.104v5.714a2.25 2.25 0 01-.659 1.591L5 14.5M9.75 3.104c-.251.023-.501.05-.75.082m.75-.082a24.301 24.301 0 014.5 0m0 0v5.714c0 .597.237 1.17.659 1.591L19.8 15.3M14.25 3.104c.251.023.501.05.75.082M19.8 15.3l-1.57.393A9.065 9.065 0 0112 15a9.065 9.065 0 00-6.23-.693L5 14.5m14.8.8l1.402 1.402c1.232 1.232 1.232 3.23 0 4.462l-.678.678a3.158 3.158 0 01-4.462 0L12 17.672" />
              </svg>
            </div>
            <span className="font-extrabold text-green-700 text-xl tracking-tight leading-none">
              Pharma<span className="text-green-500">Care</span>
            </span>
          </Link>

          {/* Search bar */}
          <form onSubmit={handleSearch} className="flex-1 max-w-2xl hidden sm:flex">
            <div className="flex w-full rounded-xl overflow-hidden border-2 border-green-500 focus-within:border-green-600 bg-white transition-colors">
              <input
                type="text"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Search medicines, health products..."
                className="flex-1 px-4 py-2.5 text-sm text-slate-700 placeholder:text-slate-400 outline-none bg-transparent"
              />
              <button
                type="submit"
                className="px-5 bg-green-600 hover:bg-green-700 transition-colors flex items-center justify-center"
              >
                <svg className="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
                </svg>
              </button>
            </div>
          </form>

          {/* Right actions */}
          <div className="flex items-center gap-2 sm:gap-3 ml-auto sm:ml-0">
            {/* Upload Rx */}
            {user && (
              <Link
                to="/prescriptions"
                className="hidden md:flex items-center gap-1.5 px-3 py-2 rounded-lg border border-green-200 text-green-700 text-xs font-semibold hover:bg-green-50 transition-colors"
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
                </svg>
                Upload Rx
              </Link>
            )}

            {/* Cart */}
            {user && (
              <Link
                to="/cart"
                className="relative flex items-center gap-1.5 px-3 py-2 rounded-lg bg-green-600 hover:bg-green-700 text-white transition-colors"
                aria-label="Cart"
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 3h1.386c.51 0 .955.343 1.087.835l.383 1.437M7.5 14.25a3 3 0 00-3 3h15.75m-12.75-3h11.218c1.121-2.3 2.1-4.684 2.924-7.138a60.114 60.114 0 00-16.536-1.84M7.5 14.25L5.106 5.272M6 20.25a.75.75 0 11-1.5 0 .75.75 0 011.5 0zm12.75 0a.75.75 0 11-1.5 0 .75.75 0 011.5 0z" />
                </svg>
                <span className="text-xs font-bold hidden sm:inline">Cart</span>
                {cartCount > 0 && (
                  <span className="absolute -top-1.5 -right-1.5 min-w-[18px] h-[18px] flex items-center justify-center bg-orange-500 text-white text-[10px] font-bold rounded-full px-1">
                    {cartCount > 99 ? "99+" : cartCount}
                  </span>
                )}
              </Link>
            )}

            {/* Account */}
            {user ? (
              <div className="flex items-center gap-2 pl-1 border-l border-slate-200">
                <div className="hidden sm:flex flex-col items-end">
                  <span className="text-[11px] text-slate-400 leading-none">Hello,</span>
                  <span className="text-sm font-semibold text-slate-700 leading-none mt-0.5 max-w-[100px] truncate">
                    {user.name?.trim() || user.username}
                  </span>
                </div>
                <button
                  onClick={() => logout.mutate()}
                  disabled={logout.isPending}
                  className="flex items-center justify-center w-8 h-8 rounded-full bg-slate-100 hover:bg-red-50 hover:text-red-600 text-slate-500 transition-colors disabled:opacity-50"
                  title="Log out"
                >
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15M12 9l-3 3m0 0l3 3m-3-3h12.75" />
                  </svg>
                </button>
              </div>
            ) : (
              <Link
                to="/login"
                className="flex items-center gap-1.5 px-4 py-2 rounded-lg bg-green-600 hover:bg-green-700 text-white text-sm font-semibold transition-colors"
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z" />
                </svg>
                Login
              </Link>
            )}
          </div>
        </div>
      </div>

      {/* ── Category nav bar ─────────────────────────────────────── */}
      <div className="bg-white border-b border-slate-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6">
          <nav className="flex items-center gap-0 overflow-x-auto scrollbar-hide">
            {[
              { to: "/catalog", label: "Medicines", icon: "M9.75 3.104v5.714a2.25 2.25 0 01-.659 1.591L5 14.5M9.75 3.104c-.251.023-.501.05-.75.082m.75-.082a24.301 24.301 0 014.5 0m0 0v5.714c0 .597.237 1.17.659 1.591L19.8 15.3M14.25 3.104c.251.023.501.05.75.082M19.8 15.3l-1.57.393A9.065 9.065 0 0112 15a9.065 9.065 0 00-6.23-.693L5 14.5m14.8.8l1.402 1.402c1.232 1.232 1.232 3.23 0 4.462l-.678.678a3.158 3.158 0 01-4.462 0L12 17.672" },
              { to: "/prescriptions", label: "Upload Prescription", icon: "M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" },
              { to: "/orders", label: "My Orders", icon: "M8.25 18.75a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h6m-9 0H3.375a1.125 1.125 0 01-1.125-1.125V14.25m17.25 4.5a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h1.125c.621 0 1.129-.504 1.09-1.124a17.902 17.902 0 00-3.213-9.193 2.056 2.056 0 00-1.58-.86H14.25M16.5 18.75h-2.25m0-11.177v-.958c0-.568-.422-1.048-.987-1.106a48.554 48.554 0 00-10.026 0 1.106 1.106 0 00-.987 1.106v7.635m12-6.677v6.677m0 4.5v-4.5m0 0h-12" },
            ].filter(l => l.to !== "/orders" && l.to !== "/prescriptions" || !!user).map(({ to, label, icon }) => (
              <Link
                key={to}
                to={to}
                className={`flex items-center gap-1.5 px-4 py-3 text-xs font-semibold whitespace-nowrap border-b-2 transition-colors ${
                  isActive(to)
                    ? "border-green-600 text-green-700"
                    : "border-transparent text-slate-600 hover:text-green-700 hover:border-green-300"
                }`}
              >
                <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d={icon} />
                </svg>
                {label}
              </Link>
            ))}
          </nav>
        </div>
      </div>
    </header>
  )
}
